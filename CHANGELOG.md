# Changelog / 更新日志

## 2.0 - 2026-07-16

### English

- Kept the 1.0 hooks for ear-device noise-reduction ownership and cloud whitelist refresh.
- Added the v2 real-time noise-reduction fallback: adaptive types 7/10 resend whitelist mode type 4.
- Added in-ear-count transition handling: deep mode is resent only when the number of buds in-ear increases.
- Added an Enco Free4 `Free4 高解析通透` EQ preset, with one dispatch per address per app process.
- Added a reverse-engineering report mapping the supplied 15.8.1 APK to the 17.0.3 class names.

### 中文

- 保留 1.0 的耳机自决降噪和云端白名单刷新 Hook。
- 新增深度降噪 v2 回退：检测到模式类型 7/10 时重新下发白名单类型 4。
- 新增佩戴数量变化处理：仅在在耳数量增加时重新下发深度模式。
- 新增 Enco Free4 `Free4 高解析通透` EQ 预设，每个地址在当前进程只自动处理一次。
- 新增参考 15.8.1 APK 到 17.0.3 类名映射的逆向检查报告。

## 1.0 - 2026-06-20

### English

- Initial public release.
- Added a runtime hook that forces `getDecideByEarDevice()` to return `false`.
- Added a runtime hook that skips the cloud whitelist refresh entry.
- Scoped the module to Oplus Wireless Earphones 17.0.3 (`com.oplus.melody`).

### 中文

- 首个公开版本。
- 添加运行时 Hook，强制 `getDecideByEarDevice()` 返回 `false`。
- 添加运行时 Hook，跳过云端白名单刷新入口。
- 模块作用域限定为 Oplus 无线耳机 17.0.3（`com.oplus.melody`）。
