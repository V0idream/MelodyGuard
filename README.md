# 🎧 MelodyGuard

### 面向 Oplus 无线耳机 17.0.3 的 LSPosed 降噪、佩戴与 EQ 运行时控制模块

Android · LSPosed · Oplus Wireless Earphones · runtime hooks

**语言**

[简体中文](#中文) · [English](#english)

**导航**

[项目简介](#项目简介) · [2.0 功能](#20-功能) · [兼容性](#兼容性) · [安装](#安装) · [构建](#构建) · [English](#english)

[![Android](https://img.shields.io/badge/Android-12%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![LSPosed](https://img.shields.io/badge/LSPosed-API%2093%2B-6f42c1)](https://github.com/LSPosed/LSPosed)
[![Target](https://img.shields.io/badge/target-Wireless%20Earphones%2017.0.3-1f6feb)](https://github.com/V0idream/MelodyGuard)
[![License](https://img.shields.io/badge/license-MIT-2ea44f)](LICENSE)
[![Release](https://img.shields.io/github/v/release/V0idream/MelodyGuard)](https://github.com/V0idream/MelodyGuard/releases)

---

## 项目简介

MelodyGuard 是一个只修改运行时行为的 LSPosed 模块，作用域为 `com.oplus.melody`。它不重打包无线耳机 APK，因此不会替换官方签名，也不会破坏耳机配对数据。

2.0 在 1.0 的两个基础 Hook 上同步了已验证的 15.8.1 修改：深度降噪 v2 回退、佩戴数量上升时自动恢复深度模式，以及 Enco Free4 的“Free4 高解析通透” EQ 预设。

## 2.0 功能

- **关闭耳机自决降噪**：`WhitelistConfigDTO$NoiseReductionMode#getDecideByEarDevice()` 固定返回 `false`。
- **阻止云端白名单刷新**：跳过 17.0.3 `h7.e#l(String)`，保留本地白名单，避免远端配置覆盖本地行为。
- **深度降噪 v2 回退**：在 `NoiseReductionItem#onEarphoneDataChanged(E8.s)` 检测到模式类型 `7/10` 时，按白名单寻找类型 `4` 的协议索引并重新下发。
- **佩戴智能恢复**：监听 `EarphoneControlProvider#onActiveEarphoneChanged(EarphoneDTO)`；只有左右耳在耳数量增加时才重新下发深度模式，数量不变或减少不重复发送。
- **Enco Free4 EQ**：在 `Q6.d#b(String)` 收到 EQ 数据后，自动选择或创建“Free4 高解析通透”，频段增益为 `-3/-2/-1/0/+1/+2/+1 dB`，每个设备地址在当前进程只处理一次。

### 已验证的参考 APK

用户提供的 `欢律_15.8.1_v2佩戴修复_Free4默认EQ_闪退修复_外层未签名-aligned-debugSigned.apk` 是 LSPatch 外壳，实际修改位于 `assets/lspatch/origin.apk`：

- `WhitelistConfigDTO$NoiseReductionMode#getDecideByEarDevice()` 已经是 `false`；
- `k5/c#o(String)` 的方法体开头是 `return-void`，对应云端刷新阻断；
- `NoiseReductionItem#onEarphoneDataChanged` 含有模式 `7/10 → 4` 的重新下发逻辑；
- `b5/d#g(String)` 含有 Free4 预设的查找、创建、选中和一次性地址 Map。

完整的类名映射、smali 证据和 17.0.3 适配说明见 [`docs/ANALYSIS-15.8.1.md`](docs/ANALYSIS-15.8.1.md)。

## 兼容性

- **已验证目标**：Oplus 无线耳机 `17.0.3`，版本号 `17000003`，包名 `com.oplus.melody`。
- Android 12 或更高版本。
- LSPosed API 93 或更高版本。
- 其他应用版本的混淆类名、方法名和 EQ 数据结构可能不同，未作兼容承诺。

官方应用下载：[无线耳机 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## 安装

1. 安装官方签名的无线耳机 17.0.3，不要安装重新签名的目标 APK。
2. 从 [MelodyGuard 2.0 Release](https://github.com/V0idream/MelodyGuard/releases/tag/v2.0) 下载并安装 `MelodyGuard-2.0.apk`；旧版仍保留在 [1.0 Release](https://github.com/V0idream/MelodyGuard/releases/tag/v1.0)。
3. 在 LSPosed 中启用 MelodyGuard，将作用域设为 `com.oplus.melody`。
4. 强制停止无线耳机后重新打开，或重启设备。

成功加载后，LSPosed 日志应包含：

```text
[MelodyGuard] Hook 1 installed: getDecideByEarDevice() = false
[MelodyGuard] Hook 2 installed: WhitelistRepositoryServerImpl refresh skipped
[MelodyGuard] Hook 3 installed: v2 noise reduction resends type-4 deep mode
[MelodyGuard] Hook 4 installed: deep ANC dispatched only when in-ear count increases
[MelodyGuard] Hook 5 installed: Free4 high-resolution EQ auto preset
```

## 构建

要求：JDK 17 或更高版本、Android SDK Platform 36、Android Build Tools 36.1.0，以及 apktool framework `1.apk`。

```powershell
.\build.ps1 `
  -FrameworkPackage "$env:LOCALAPPDATA\apktool\framework\1.apk" `
  -BuildToolsDir "$env:ANDROID_HOME\build-tools\36.1.0" `
  -VersionCode 2 `
  -VersionName 2.0.0 `
  -Output ".\dist\MelodyGuard-2.0.apk"
```

构建结果：`dist\MelodyGuard-2.0.apk`。构建脚本会执行 `zipalign`、APK 签名和 `apksigner verify`。

## 限制与免责声明

- 本模块只针对上述目标版本的运行时类名编写；升级无线耳机后请先检查 LSPosed 日志。
- Free4 EQ 会通过官方 EQ 设置通道创建/选择预设；耳机或固件拒绝该协议时不会修改其他 EQ。
- 本项目与 OPPO、Oplus 无关联。请仅在你拥有或获授权的设备和软件上使用。

## English

MelodyGuard is an LSPosed runtime module for Oplus Wireless Earphones `17.0.3` (`com.oplus.melody`). It leaves the official APK and signature untouched.

Version 2.0 keeps the 1.0 hooks and adds the verified behaviors from the supplied 15.8.1 reference APK: v2 deep-noise fallback to mode type 4, deep-mode restoration only when the in-ear count increases, and an automatic Enco Free4 “Free4 高解析通透” EQ preset. The preset uses `-3/-2/-1/0/+1/+2/+1 dB` across the device-reported bands and is dispatched once per address per app process.

Install the official app from [this link](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H), install [`MelodyGuard-2.0.apk`](https://github.com/V0idream/MelodyGuard/releases/tag/v2.0), enable it in LSPosed, and scope it to `com.oplus.melody`. Release `v1.0` remains available.

The module is not affiliated with OPPO/Oplus. Compatibility is verified only for Wireless Earphones 17.0.3 on Android 12+ with LSPosed API 93+.

## ☕ 支持项目 / Support

如果这个项目对你有帮助，欢迎点一个 Star。若愿意进一步支持项目维护，请参阅 [赞赏方式](docs/support.md)。支持完全自愿，不影响使用、Issue 交流或功能建议。

## License

MIT
