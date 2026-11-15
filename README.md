# LspAutoStart

Lsposed 模块：应用自启动管理 / 保活。

> 需要 Root + LSPosed（Zygisk）。无 Root 环境无法使用。

## 功能
- 开机自动启动指定应用
- 进程保活（可选）
- 基于 LSPosed / Xposed 框架（**需要 Root**）

## 模块结构
```
app/
├── src/main/
│   ├── java/com/example/lspautostart/
│   │   ├── Module.java           # Xposed 入口
│   │   ├── BootReceiver.java     # 开机广播接收器
│   │   └── KeepAliveService.java # 保活服务
│   ├── assets/
│   │   └── xposed_init           # 模块入口类声明
│   └── AndroidManifest.xml
└── build.gradle
```

## 编译要求
- **JDK 17+**（推荐 OpenJDK 17/21）
- **Android SDK**（command-line tools 即可）
- **Gradle 8.x**（项目自带 `gradlew`）

## 环境变量配置
```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```
> **注意**：proot / Termux / 手机端 Linux 环境**缺少完整工具链**（无 `aapt2`、`d8`、`zipalign` 等原生二进制），**无法直接编译 APK**。请在普通 Linux 电脑 / CI 上构建。

## 本地编译
```bash
git clone https://github.com/polear234/LspAutoStart.git
cd LspAutoStart
./gradlew assembleRelease
```
产物：`app/build/outputs/apk/release/app-release.apk`

## 安装

> ⚠️ **必须已 Root**：LSPosed 依赖 Magisk / KernelSU / APatch 提供的 Zygisk 环境，无 Root 的手机无法使用本模块。

前提条件：
- 已解锁 Bootloader
- 已安装 Magisk（或 KernelSU / APatch）
- 已启用 Zygisk 并安装 [LSPosed](https://github.com/LSPosed/LSPosed)（Zygisk 版）

步骤：
1. 安装编译好的 `app-release.apk`
2. 打开 LSPosed 管理器 → 模块 → 启用 **LspAutoStart**
3. 勾选作用域中的应用
4. 重启手机生效

## 许可证
MIT