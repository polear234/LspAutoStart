# LspAutoStart

<p align="center">
  <img src="docs/assets/avatar.jpg" width="120" style="border-radius:50%" alt="avatar">
</p>

<p align="center">
  <b>Lsposed 模块：hook 系统框架，开机后在后台自动启动指定 App</b>
</p>

<p align="center">
  <a href="https://github.com/polear234/LspAutoStart/releases/latest">
    <img src="https://img.shields.io/github/v/release/polear234/LspAutoStart?style=flat-square" alt="Release">
  </a>
  <a href="https://github.com/polear234/LspAutoStart/actions">
    <img src="https://img.shields.io/github/actions/workflow/status/polear234/LspAutoStart/build.yml?style=flat-square" alt="Build">
  </a>
  <img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square" alt="License">
</p>

---

## 功能

- **系统框架级拉起**：hook `android` 进程的 `SystemServer`，以系统身份启动目标 App，不受「后台启动 Activity」限制
- **应用列表选择**：扫描已安装应用，图标 + 名称 + 包名，支持搜索，开关可开可关
- **开机延迟**：0 / 5 / 15 / 30 / 60 秒可选
- **启动后切后台**：拉起后自动把目标 App 退到后台，不打断当前操作（时机可调）
- **Miuix 风格界面**：渐变背景、圆角卡片、底部 Dock、左右滑动切页
- **随时开关**：无需卸载即可停用

## 使用

1. 安装 APK
2. 打开 LspAutoStart → 「应用」页选择目标 App
3. 按需调整延迟、是否切后台
4. Lsposed 里启用本模块，**生效范围勾选「系统框架」（必需）** 和目标 App
5. 重启手机

重启后在 Lsposed 日志搜索 `LspAutoStart`：

```
LspAutoStart: 已挂载系统框架
LspAutoStart: 15 秒后拉起 com.example.app
LspAutoStart: 已拉起 com.example.app
LspAutoStart: 已切到后台 com.example.app
```

## 构建

**标准构建**（需要 Android SDK + JDK 17）：

```sh
./gradlew assembleRelease
```

**手工构建**（arm64 / proot 等无法运行 x86-64 `aapt2` 的环境）：

```sh
sh tools/build.sh
```

手工脚本会依次执行 `javac → d8 → aapt → zipalign → apksigner`。
其中 `tools/zipalign.py` 是自实现的，确保 `resources.arsc` 以 STORED 存储且 4 字节对齐 ——
这是 Android R+ 的硬性要求，否则安装报 `-124`。

> `app/libs/xposed-api-stub.jar` 仅用于编译，运行时由 Lsposed 提供真实实现。

## 结构

```
app/src/main/java/io/polear234/autostart/
├── Module.java            # 核心：hook 系统框架 SystemServer
├── Backgrounder.java      # 启动后把目标 App 切到后台
├── MainActivity.java      # 主界面（主页 / 应用 / 关于三页）
├── AppPickerView.java     # 内嵌应用列表
├── Prefs.java             # 配置读写
├── Miuix.java             # Miuix 风格控件工厂
├── ToggleSwitch.java      # 开关
├── GradientBg.java        # 渐变背景
├── AppIconView.java       # 应用图标
├── DockIcon.java          # Dock 图标
├── SystemBars.java        # 系统栏适配
├── BootReceiver.java      # 开机广播兜底
└── KeepAliveService.java  # 前台服务兜底
```

## 注意事项

- 需要 Root + Lsposed 环境
- 生效范围必须勾选「系统框架」
- 部分 ROM 需额外授予「后台弹出界面」权限
- 修改设置后需重启才能自动生效

## License

[MIT](LICENSE)
