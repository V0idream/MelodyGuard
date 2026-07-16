# 15.8.1 参考 APK 检查与 17.0.3 同步说明

## 中文

### 检查对象

参考文件是一个 LSPatch 外壳 APK。外层包名为 `com.heytap.headset`，实际原始应用位于 `assets/lspatch/origin.apk`；内置的 `com.virb3.trustmealready.apk` 只负责 TLS 信任校验，不包含降噪、佩戴或 EQ 逻辑。因此本次比对针对 `origin.apk` 的 smali，而不是外层壳。

### 与 1.0 的重复部分

| 参考行为 | 15.8.1 位置 | 17.0.3 运行时同步 |
| --- | --- | --- |
| 禁止耳机自决降噪 | `WhitelistConfigDTO$NoiseReductionMode#getDecideByEarDevice()` 直接 `const/4 v0, 0x0; return v0` | Hook 1：固定返回 `false` |
| 阻止云端白名单 | `k5/c#o(String)` 方法体第一条指令为 `return-void` | Hook 2：跳过 `h7.e#l(String)` |

这两项与 1.0 的运行时目标完全重合，所以没有复制 APK 代码，而是保留为 LSPosed Hook；官方 APK 的签名和安装包内容不会被改写。

### 深度降噪 v2

参考 APK 在 `com.oplus.melody.ui.component.detail.noisereduction.NoiseReductionItem#onEarphoneDataChanged(Lx6/g;)V` 的 UI 更新后加入了以下逻辑：

1. 从当前 Noise Reduction VO 取得当前协议索引；
2. 在白名单模式列表中找到该索引对应的 `modeType`；
3. 仅当类型为 `10` 或 `7`（v2/实时计算路径）时继续；
4. 查找 `modeType == 4` 的协议索引；
5. 调用耳机仓库的设置接口重新发送该索引。

17.0.3 的同名类位于 `smali_classes2`，参数类型变为 `E8.s`，仓库接口变为 `b.E().p0(protocolIndex, address)`。Hook 3 在同一观察点完成这组映射，因此轻度、通透和关闭等其他 mode type 不会被重写。

### 佩戴数量恢复

参考描述的行为规则是“在耳数量增加才重新下发，数量不变或减少不重复下发”。17.0.3 的 `EarphoneControlProvider#onActiveEarphoneChanged(EarphoneDTO)` 同时持有 `activeEarphone` 的旧状态和新 `EarphoneDTO`，而 `EarStatusDTO` 的左右状态以 bit `0x2` 表示在耳：

```text
inEarCount = ((leftStatus & 0x2) != 0 ? 1 : 0)
            + ((rightStatus & 0x2) != 0 ? 1 : 0)
```

Hook 4 在该方法执行前比较两个计数。只有 `newCount > oldCount` 才从 `h7.a` 白名单找到类型 4 并调用 `b.E().p0(...)`；同一地址短时间内的重复回调还会被节流。已知的轻度/通透/关闭模式会保留，不会因摘戴或 UI 刷新被强制覆盖。

### Enco Free4 EQ

参考 APK 的对应实现位于 `b5/d#g(String)`：

- 设备名包含 `Enco Free4` 时启用；
- 用地址 Map 保证一次性处理；
- 已存在 `Free4 高解析通透` 时发送 action `2` 选中；
- 不存在时创建并发送 action `1`；
- 频率来自设备第一套 EQ 数据，缺失时使用 `62, 250, 1000, 4000, 8000, 16000`；
- 增益计算为 `-3, -2, -1, 0, +1, +2, +1 dB`，范围锁定在 `-6..+6`。

17.0.3 的 EQ 仓库是 `Q6/d`，接收入口为 `b(String)`，模型为 `Q6/b`，设置接口为 `i(address, Q6/b, action)`。Hook 5 在原方法返回后完成同样的查找、创建和选中流程，并沿用当前设备上报的频率数组。

## English

The supplied APK is an LSPatch wrapper. Its embedded `origin.apk` contains the feature changes; the embedded TrustMeAlready module only changes TLS trust handling. The exact overlap with MelodyGuard 1.0 is the forced `getDecideByEarDevice() == false` result and the early-return whitelist refresh method.

The 17.0.3 module maps the remaining verified behavior to its renamed classes: `NoiseReductionItem#onEarphoneDataChanged(E8.s)` for v2-to-type-4 deep-mode fallback, `EarphoneControlProvider#onActiveEarphoneChanged(EarphoneDTO)` for an in-ear-count increase policy, and `Q6.d#b(String)` / `Q6.b` for the Free4 EQ preset. All hooks are reflection-based so the module can compile without private application classes.
