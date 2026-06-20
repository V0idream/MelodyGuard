# MelodyGuard

Runtime controls for Oplus Wireless Earphones 17.0.3, delivered as an LSPosed module.

适用于 Oplus 无线耳机 17.0.3 的 LSPosed 运行时控制模块。

[English](#english) | [中文](#中文)

## English

### Overview

MelodyGuard keeps the official Wireless Earphones APK and its Oplus signature intact. It applies two runtime hooks when `com.oplus.melody` starts:

1. Forces `WhitelistConfigDTO.NoiseReductionMode#getDecideByEarDevice()` to return `false`.
2. Skips the cloud whitelist refresh entry in `WhitelistRepositoryServerImpl`.

### Compatibility

- Oplus Wireless Earphones 17.0.3 (`versionCode 17000003`)
- Android 12 or later
- LSPosed API 93 or later
- Target package: `com.oplus.melody`

Download the required official app: [Wireless Earphones 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

### Installation

1. Keep or install the official Wireless Earphones 17.0.3 APK. Do not replace it with a re-signed APK.
2. Download and install `MelodyGuard-1.0.apk` from [GitHub Releases](https://github.com/Sakuramble/MelodyGuard/releases/latest).
3. Enable MelodyGuard in LSPosed.
4. Set its scope to Wireless Earphones (`com.oplus.melody`). Enable “show system apps” if needed.
5. Force-stop Wireless Earphones and reopen it, or reboot the device.

Successful activation writes these entries to the LSPosed log:

```text
[OplusMelody173Hook] Hook 1 installed: getDecideByEarDevice() = false
[OplusMelody173Hook] Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped
```

### Notes

- Version 1.0 targets the obfuscated class and method names in Wireless Earphones 17.0.3. Other versions are not guaranteed to work.
- This project is not affiliated with OPPO or Oplus. Use it only on devices and software you are authorized to modify.

## 中文

### 项目介绍

MelodyGuard 保留官方无线耳机 APK 及其 Oplus 签名，在 `com.oplus.melody` 启动时通过 LSPosed 执行两项运行时 Hook：

1. 强制 `WhitelistConfigDTO.NoiseReductionMode#getDecideByEarDevice()` 返回 `false`。
2. 跳过 `WhitelistRepositoryServerImpl` 的云端白名单刷新入口。

### 兼容条件

- Oplus 无线耳机 17.0.3（`versionCode 17000003`）
- Android 12 或更高版本
- LSPosed API 93 或更高版本
- 目标包名：`com.oplus.melody`

官方应用下载：[无线耳机 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

### 安装方法

1. 保留或安装官方签名的无线耳机 17.0.3，不要使用重新签名的目标 APK。
2. 从 [GitHub Releases](https://github.com/Sakuramble/MelodyGuard/releases/latest) 下载并安装 `MelodyGuard-1.0.apk`。
3. 在 LSPosed 中启用 MelodyGuard。
4. 将作用域设置为无线耳机（`com.oplus.melody`）；如果找不到，请启用“显示系统应用”。
5. 强制停止无线耳机后重新打开，或重启手机。

成功启用后，LSPosed 日志中会出现：

```text
[OplusMelody173Hook] Hook 1 installed: getDecideByEarDevice() = false
[OplusMelody173Hook] Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped
```

### 注意事项

- 1.0 版针对无线耳机 17.0.3 的混淆类名和方法名编写，不保证兼容其他版本。
- 本项目与 OPPO、Oplus 无关联。请仅在你有权修改的设备和软件上使用。

## Build / 构建

Requirements: JDK 17 or later, Android SDK Platform 36, and Android Build Tools 36.1.0.

需要 JDK 17 或更高版本、Android SDK Platform 36 和 Android Build Tools 36.1.0。

```powershell
.\build.ps1 `
  -FrameworkPackage "$env:ANDROID_HOME\platforms\android-36\android.jar" `
  -BuildToolsDir "$env:ANDROID_HOME\build-tools\36.1.0"
```

The files under `compile-stubs` are compile-only Xposed API declarations and are excluded from the module DEX.

`compile-stubs` 中的文件仅用于编译，构建时不会写入模块 DEX。

## License

MIT
