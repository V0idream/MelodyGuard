<div align="center">

# 🎧 MelodyGuard

### 适用于 Oplus 无线耳机 17.0.3 的 LSPosed 运行时控制模块

**LSPosed Module · Oplus Wireless Earphones · Runtime Hooks · Android 12+ · Authorized Devices Only**

<p>
  <strong>语言</strong><br/>
  <strong>简体中文</strong> ·
  <a href="#english">English</a>
</p>

<p>
  <strong>导航</strong><br/>
  <a href="#项目简介">项目简介</a> ·
  <a href="#兼容条件">兼容条件</a> ·
  <a href="#安装方法">安装方法</a> ·
  <a href="#构建">构建</a> ·
  <a href="#许可证">许可证</a>
</p>

![Platform](https://img.shields.io/badge/Platform-Android-111827)
![Module](https://img.shields.io/badge/Module-LSPosed-0F766E)
![Target](https://img.shields.io/badge/Target-com.oplus.melody-1D4ED8)
![Android](https://img.shields.io/badge/Android-12%2B-7C3AED)
![License](https://img.shields.io/badge/License-MIT-FF5722)
![Release](https://img.shields.io/github/v/release/V0idream/MelodyGuard?include_prereleases)

</div>

---

<a id="中文"></a>

<a id="项目简介"></a>

## 📌 项目简介

**MelodyGuard** 保留官方无线耳机 APK 及其 Oplus 签名，在 `com.oplus.melody` 启动时通过 LSPosed 执行两项运行时 Hook：

1. 强制 `WhitelistConfigDTO.NoiseReductionMode#getDecideByEarDevice()` 返回 `false`。
2. 跳过 `WhitelistRepositoryServerImpl` 的云端白名单刷新入口。

该项目面向需要在本人设备与本人有权修改的软件环境中进行运行时控制、调试和兼容性验证的用户。项目与 OPPO、Oplus 无关联。

<a id="兼容条件"></a>

## 🧩 兼容条件

* Oplus 无线耳机 17.0.3（`versionCode 17000003`）
* Android 12 或更高版本
* LSPosed API 93 或更高版本
* 目标包名：`com.oplus.melody`

官方应用下载：[无线耳机 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

<a id="安装方法"></a>

## 📦 安装方法

1. 保留或安装官方签名的无线耳机 17.0.3，不要使用重新签名的目标 APK。
2. 从 [GitHub Releases](https://github.com/V0idream/MelodyGuard/releases/latest) 下载并安装 `MelodyGuard-1.0.apk`。
3. 在 LSPosed 中启用 MelodyGuard。
4. 将作用域设置为无线耳机（`com.oplus.melody`）；如果找不到，请启用“显示系统应用”。
5. 强制停止无线耳机后重新打开，或重启手机。

成功启用后，LSPosed 日志中会出现：

```text
[OplusMelody173Hook] Hook 1 installed: getDecideByEarDevice() = false
[OplusMelody173Hook] Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped
```

## ⚠️ 注意事项

* Version 1.0 针对无线耳机 17.0.3 的混淆类名和方法名编写，不保证兼容其他版本。
* 请仅在你有权修改的设备和软件上使用。
* 仓库链接、Release 链接和徽章已统一使用当前用户名 `V0idream`。

<a id="构建"></a>

## 🛠️ 构建

Requirements: JDK 17 or later, Android SDK Platform 36, and Android Build Tools 36.1.0.

需要 JDK 17 或更高版本、Android SDK Platform 36 和 Android Build Tools 36.1.0。

```powershell
.\build.ps1 `
  -FrameworkPackage "$env:ANDROID_HOME\platforms\android-36\android.jar" `
  -BuildToolsDir "$env:ANDROID_HOME\build-tools\36.1.0"
```

The files under `compile-stubs` are compile-only Xposed API declarations and are excluded from the module DEX.

`compile-stubs` 中的文件仅用于编译，构建时不会写入模块 DEX。

<a id="许可证"></a>

## 📜 许可证

MIT.

---

<a id="english"></a>

## English

**MelodyGuard** keeps the official Wireless Earphones APK and its Oplus signature intact. It applies two runtime hooks when `com.oplus.melody` starts:

1. Forces `WhitelistConfigDTO.NoiseReductionMode#getDecideByEarDevice()` to return `false`.
2. Skips the cloud whitelist refresh entry in `WhitelistRepositoryServerImpl`.

This project is intended for runtime control, debugging, and compatibility verification on devices and software environments that you are authorized to modify. It is not affiliated with OPPO or Oplus.

## Compatibility

* Oplus Wireless Earphones 17.0.3 (`versionCode 17000003`)
* Android 12 or later
* LSPosed API 93 or later
* Target package: `com.oplus.melody`

Download the required official app: [Wireless Earphones 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## Installation

1. Keep or install the official Wireless Earphones 17.0.3 APK. Do not replace it with a re-signed APK.
2. Download and install `MelodyGuard-1.0.apk` from [GitHub Releases](https://github.com/V0idream/MelodyGuard/releases/latest).
3. Enable MelodyGuard in LSPosed.
4. Set its scope to Wireless Earphones (`com.oplus.melody`). Enable “show system apps” if needed.
5. Force-stop Wireless Earphones and reopen it, or reboot the device.

Successful activation writes these entries to the LSPosed log:

```text
[OplusMelody173Hook] Hook 1 installed: getDecideByEarDevice() = false
[OplusMelody173Hook] Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped
```

## Notes

* Version 1.0 targets the obfuscated class and method names in Wireless Earphones 17.0.3. Other versions are not guaranteed to work.
* Use it only on devices and software you are authorized to modify.
* Repository links, release links, and badges have been normalized to the current username `V0idream`.

## Build

Requirements: JDK 17 or later, Android SDK Platform 36, and Android Build Tools 36.1.0.

```powershell
.\build.ps1 `
  -FrameworkPackage "$env:ANDROID_HOME\platforms\android-36\android.jar" `
  -BuildToolsDir "$env:ANDROID_HOME\build-tools\36.1.0"
```

The files under `compile-stubs` are compile-only Xposed API declarations and are excluded from the module DEX.

## License

MIT.
