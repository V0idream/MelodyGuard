# MelodyGuard 2.0

## English

Version 2.0 synchronizes the verified feature changes found in the supplied 15.8.1 reference APK while keeping the official Wireless Earphones 17.0.3 APK untouched.

- Retains the 1.0 hooks: force `getDecideByEarDevice()` to return `false` and skip the cloud whitelist refresh.
- Resends whitelist mode type 4 when `NoiseReductionItem` reports adaptive mode type 7 or 10.
- Resends deep mode only when `EarphoneControlProvider` observes an increase in the in-ear bud count.
- Selects or creates the Enco Free4 `Free4 高解析通透` EQ preset with `-3/-2/-1/0/+1/+2/+1 dB` gains.
- Uses reflection for private app classes and targets Wireless Earphones 17.0.3 only.

Required official app: [Wireless Earphones 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## 中文

2.0 版同步用户提供的 15.8.1 参考 APK 中已验证的功能调整，同时保持官方无线耳机 17.0.3 APK 不变。

- 保留 1.0：强制 `getDecideByEarDevice()` 返回 `false`，跳过云端白名单刷新。
- `NoiseReductionItem` 检测到自适应模式类型 7/10 时，重新下发白名单类型 4。
- `EarphoneControlProvider` 仅在在耳耳机数量增加时重新下发深度模式。
- Enco Free4 自动选中或创建 `Free4 高解析通透`，增益为 `-3/-2/-1/0/+1/+2/+1 dB`。
- 使用反射访问私有类，仅针对无线耳机 17.0.3。

所需官方应用：[无线耳机 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## File integrity / 文件校验

`MelodyGuard-2.0.apk`

SHA-256: `0B5A1D6E005E38AB0EC6D203FF620B8B8A488B8140D82B294F9E6614AFDDD0A8`

旧版 1.0 发布不会删除，仍可从 [v1.0 Release](https://github.com/V0idream/MelodyGuard/releases/tag/v1.0) 获取。

---

# MelodyGuard 1.0

## English

Initial public release of the LSPosed runtime module for Oplus Wireless Earphones 17.0.3.

- Forces `getDecideByEarDevice()` to return `false`.
- Skips the cloud whitelist refresh entry in `WhitelistRepositoryServerImpl`.
- Keeps the official Wireless Earphones APK and Oplus signature unchanged.
- Supports Android 12+ and LSPosed API 93+.

Required official app: [Wireless Earphones 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## 中文

适用于 Oplus 无线耳机 17.0.3 的 LSPosed 运行时模块首个公开版本。

- 强制 `getDecideByEarDevice()` 返回 `false`。
- 跳过 `WhitelistRepositoryServerImpl` 的云端白名单刷新入口。
- 不修改官方无线耳机 APK，保留 Oplus 官方签名。
- 支持 Android 12 及以上版本、LSPosed API 93 及以上版本。

所需官方应用：[无线耳机 17.0.3](https://1811812159.mshare.123pan.cn/123pan/7AoDVv-O0U2H)

## File integrity / 文件校验

`MelodyGuard-1.0.apk`

SHA-256: `43E4B87C1BF81040ABAAE0C62C26BCB84F64D7952F5BDAB151F3EBDF8F6030E3`
