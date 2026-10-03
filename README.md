# 女仆饰品管理 Maid Curios Manager

Forge 1.20.1 小工具模组：通过 GUI 管理**车万女仆（Touhou Little Maid）**身上的
**Curios 饰品数据**（该数据保存在女仆实体 NBT 的 Curios 能力中）。

## 功能

- **快捷键打开**：在**女仆界面内**按快捷键直接打开「饰品管理」界面
  - 按键可在「选项 → 按键绑定 → **女仆饰品管理**」中自行更改，默认**未绑定**
- 界面列出女仆身上的所有 Curios 槽位类型：
  - 每类槽位显示「已用/总数」，支持 **−槽 / +槽** 增减槽位数量（每类最多可扩充到 **64** 个）
  - 每个已装备的饰品一行，显示物品图标与名称
  - 通过 **− / +** 按钮或直接**输入框输入数字**修改饰品堆叠数量
- **全局开关**：车万女仆「全局设置」中新增「启用饰品管理模组」，可整体关闭本模组
- **保存**：把待办修改逐条发送到服务端，写入女仆 Curios 实体能力（随女仆 NBT 持久化），
  Curios 自动同步给所有客户端
- **重置**：丢弃未保存的修改，重新读取女仆当前数据
- **NBT**：只读预览每类槽位的 Curios NBT 数据
- 收缩槽位时自动保护：末尾槽仍有饰品则不允许收缩，避免饰品丢失

## 操作说明

| 按键 | 行为 |
| --- | --- |
| 快捷键「在女仆界面内打开饰品管理」 | 对当前女仆界面正在查看的女仆打开饰品管理界面（默认未绑定） |
| 普通 / 潜行右键女仆 | 完全保持车万女仆原生行为，不受本模组影响 |

## 配置（config/maidcuriosmanager-common.toml）

| 选项 | 默认 | 说明 |
| --- | --- | --- |
| enableManager | true | 本模组总开关（也可在车万女仆「全局设置」里改）。关闭后快捷键与界面入口全部停用；**不影响**车万女仆自带的 `enable_maid_curios` |
| maxSlotsPerType | 64 | 每类槽位可扩充到的最大槽位数（上限 64） |
| maxStackCount | 64 | 饰品堆叠数量可设置的上限 |

> ⚠️ **升级提示**：Forge **不会**因为默认值变化而覆盖你已有的配置文件。
> 早期版本默认 `maxSlotsPerType = 8`，升级后旧配置文件里仍然写着 8，表现为
> 「槽位最多只能加到 8」。请把该值改成 `64`，或直接删除
> `config/maidcuriosmanager-common.toml` 让它按新默认值重新生成。
> 模组启动时会检测到这种情况并在日志里给出警告。

## 兼容性说明

本模组对车万女仆是**软依赖**，且**不修改车万女仆本体**：

- 车万女仆未发布到任何公共 Maven 仓库，因此所有对该模组类的调用都通过**反射**完成
  （见 `MaidTypeHelper`、`client/TlmCompat`）。
- 配置注入使用车万女仆的公开 API `AddClothConfigEvent`，向「全局设置」注入总开关与快捷键。
- 未安装车万女仆时，本模组正常加载但没有任何交互入口。
- `enableManager` 只控制本模组自己；是否能真正管理饰品，仍取决于车万女仆的
  `enable_maid_curios`（启用女仆 Curios 饰品栏支持）是否为开。


## 依赖

- **Curios**（必需，Forge 1.20.x）
- **Touhou Little Maid（车万女仆）**（软依赖：未安装时模组正常加载但没有交互入口）

## 构建

需要 JDK 17。直接使用项目自带 Gradle wrapper（国内镜像）：

```bash
gradlew.bat build
```

产物：`build/libs/maidcuriosmanager-1.0.0.jar`
（编译源码 → 打包语言文件/纹理/配置 → 按 Forge 模组规范生成 JAR）

安装：将 JAR 放入 `mods/` 目录即可。

## 目录结构

```
src/main/java/com/maidcurios/
├── MaidCuriosManager.java        # @Mod 主类
├── MaidCuriosConfig.java         # Forge 配置（含总开关 enableManager）
├── MaidCuriosEvents.java         # 【已停用】服务端右键拦截（源码保留）
├── MaidCuriosNetwork.java        # SimpleChannel 网络通道
├── MaidTypeHelper.java           # 车万女仆反射软依赖（实体）
├── network/CurioEditMessage.java # C2S 修改请求包
└── client/
    ├── ClientRegistration.java   # 客户端注册入口
    ├── ClientInteractHandler.java# 【已停用】客户端右键打开界面（源码保留）
    ├── MaidCuriosKeybinds.java   # 快捷键（可改键，独立分类）
    ├── MaidGuiKeyHandler.java    # 快捷键触发逻辑
    ├── MaidGuiHooks.java         # 【已停用】女仆界面侧边栏入口按钮（源码保留）
    ├── MaidCuriosConfigScreen.java # 注入「全局设置」的开关与快捷键
    ├── MaidCuriosClothCompat.java# Cloth Config 存在性检查
    ├── TlmCompat.java            # 车万女仆 GUI 反射桥接
    └── gui/MaidCuriosScreen.java # 饰品管理 GUI

src/curiosapi/java/               # Curios API 源码（仅编译期使用，不打入 JAR）

libs/cloth-config-forge-*.jar     # Cloth Config（仅编译期使用，运行时由本体提供）

src/main/resources/
├── META-INF/mods.toml
├── pack.mcmeta
├── config/maidcuriosmanager-common.toml   # 打包的默认配置样例
└── assets/maidcuriosmanager/
    ├── lang/en_us.json, zh_cn.json        # 语言文件
    └── textures/gui/icon.png              # 图标
```

## 说明

- 编译期使用 Curios 5.14.1 的 API 源码（与 Forge 1.20.1 官方映射一致），
  依赖范围声明为 `[5.9,)`，兼容常见 Curios 版本。
- 所有对女仆 Curios 数据的修改都经过服务端校验，防止越权与物品丢失。