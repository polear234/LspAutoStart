# LspAutoStart

Lsposed 模块：应用自启动管理 / 保活。

## 功能
- 开机自动启动指定应用
- 进程保活（可选）
- 无需 Root，仅需 LSPosed / Xposed 框架

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
1. 手机安装 [Lsposed](https://github.com/LSPosed/LSPosed) 或 [EdXposed](https://github.com/ElderDrivers/EdXposed)
2. 安装编译好的 `app-release.apk`
3. 在 Lsposed 管理器中勾选模块并重启

## 许可证
MIT