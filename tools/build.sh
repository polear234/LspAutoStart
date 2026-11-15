#!/bin/sh
# 手工构建 LspAutoStart APK（arm64 / proot 环境，绕开 x86-64 的 aapt2 与 zipalign）
#
# 关键点：
#   1. 用 arm64 的 aapt(v1) 打包资源，而不是 x86-64 的 aapt2
#   2. classes.dex 用纯 Java 的 d8 生成
#   3. 签名前必须 zipalign：resources.arsc 要 STORED 且 4 字节对齐，
#      否则 Android R+ 安装报 -124
#   4. 用纯 Java 的 apksigner 签名
#
# 用法：tools/build.sh
set -e

PROJ="$(cd "$(dirname "$0")/.." && pwd)"
SDK="${ANDROID_HOME:-/root/Android/Sdk}"
BT="$SDK/build-tools/34.0.0"
AJAR="$SDK/platforms/android-34/android.jar"
SRC="$PROJ/app/src/main"
STUB="$PROJ/app/libs/xposed-api-stub.jar"
OUT="$PROJ/build-manual"
KS="${KEYSTORE_FILE:-$PROJ/../LspAutoStart.keystore}"

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

echo "== 1/6 编译 Java =="
javac -source 8 -target 8 -bootclasspath "$AJAR" -classpath "$STUB" \
    -d "$OUT/classes" $(find "$SRC/java" -name '*.java') 2>&1 \
    | grep -viE 'bootstrap|deprecat|Note:' || true

echo "== 2/6 生成 dex =="
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --min-api 21 --lib "$AJAR" \
    --output "$OUT/dex" $(find "$OUT/classes" -name '*.class') 2>&1 \
    | grep -viE 'warning|was not found' || true

echo "== 3/6 打包资源 =="
aapt package -f -M "$SRC/AndroidManifest.xml" -S "$SRC/res" -A "$SRC/assets" \
    -I "$AJAR" -F "$OUT/base.apk"

echo "== 4/6 合并 dex =="
(cd "$OUT/dex" && zip -q "$OUT/base.apk" classes.dex)

echo "== 5/6 zipalign（必须在签名前）=="
python3 "$PROJ/tools/zipalign.py" "$OUT/base.apk" "$OUT/aligned.apk"

echo "== 6/6 签名 =="
if [ ! -f "$KS" ]; then
    echo "找不到 keystore: $KS" >&2
    echo "请用 KEYSTORE_FILE=/path/to/keystore 指定" >&2
    exit 1
fi
java -jar "$BT/lib/apksigner.jar" sign \
    --ks "$KS" --ks-key-alias "${KEY_ALIAS:-lsp}" \
    --ks-pass "pass:${KEYSTORE_PASSWORD:-lsp123456}" \
    --key-pass "pass:${KEY_PASSWORD:-lsp123456}" \
    --out "$PROJ/LspAutoStart.apk" "$OUT/aligned.apk"

echo
echo "完成: $PROJ/LspAutoStart.apk"
java -jar "$BT/lib/apksigner.jar" verify "$PROJ/LspAutoStart.apk" && echo "签名校验通过"
