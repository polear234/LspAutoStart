# LspAutoStart

一个 Lsposed 模块：**开机后在后台自动启动指定 App**。

通过 hook Android 系统框架（`android` 进程的 `SystemServer`），在系统就绪后以系统身份拉起目标应用。因为发起者是系统进程，**不受 Android 后台启动限制**，比在目标 App 内部 hook 更可靠。

## 功能

- **图形界面**（Miuix 风格）：圆角卡片、分组列表、边缘到边缘适配，无黑边
- **应用列表选择**：扫描已安装的可启动应用，显示真实图标 / 名称 / 包名，支持搜索
- **开机延迟**：0 / 5 / 15 / 30 / 60 秒可选，等系统就绪后再拉起
- **启用开关**：随时关闭自启而不必卸载
- **立即测试**：手动触发一次启动，验证配置是否正确
- **双通道兜底**：系统框架 hook（主）+ 开机广播前台服务（辅）

## 使用步骤

1. 安装 APK
2. 打开 LspAutoStart，点「目标应用」→ 从列表中选择要自启的 App
3. 按需调整「开机延迟启动」
4. 打开 Lsposed → 启用本模块
5. **生效范围勾选：**
   - ✅ **系统框架**（`android`）— 关键，没有它就没有系统拉起能力
   - ✅ 目标 App 本人
6. 重启手机

## 验证

重启后在 Lsposed 日志中搜索 `LspAutoStart`，正常应看到：

```
LspAutoStart: 已挂载系统框架
LspAutoStart: 15 秒后拉起 com.example.app
LspAutoStart: 已拉起 com.example.app
```

## 构建

本项目可用标准 Android Studio / Gradle 构建（`./gradlew assembleRelease`）。

> 注意：仓库内 `app/libs/xposed-api-stub.jar` 是**仅用于编译**的 Xposed API 桩，
> 运行时由 Lsposed 框架提供真实实现，不会打进 APK。

若在 arm64 环境（如 Termux/proot）下无法运行 x86-64 的 `aapt2`，可参考以下手工构建流程：

```bash
SDK=$ANDROID_HOME; BT=$SDK/build-tools/34.0.0; AJAR=$SDK/platforms/android-34/android.jar
javac -source 8 -target 8 -bootclasspath "$AJAR" \
  -classpath app/libs/xposed-api-stub.jar -d /tmp/classes \
  $(find app/src/main/java -name '*.java')
java -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --min-api 21 --lib "$AJAR" \
  --output /tmp/dex /tmp/classes/**/*.class
aapt package -f -M app/src/main/AndroidManifest.xml -S app/src/main/res \
  -A app/src/main/assets -I "$AJAR" -F /tmp/base.apk
cd /tmp/dex && zip -q /tmp/base.apk classes.dex
java -jar "$BT/lib/apksigner.jar" sign --ks your.keystore --ks-key-alias your \
  --out LspAutoStart.apk /tmp/base.apk
```

## 结构

```
app/src/main/
├── AndroidManifest.xml
├── assets/xposed_init              # 模块入口类声明
├── res/values/styles.xml           # 系统栏配色（消除黑边）
└── java/io/polear234/autostart/
    ├── Module.java                 # 核心：hook 系统框架 SystemServer
    ├── MainActivity.java           # 主界面（Miuix 风格）
    ├── AppPickerActivity.java      # 应用列表选择
    ├── Miuix.java                  # Miuix 风格控件工厂
    ├── Prefs.java                  # 配置读写
    ├── BootReceiver.java           # 开机广播兜底
    └── KeepAliveService.java       # 前台服务兜底
```

## 许可证

MIT
