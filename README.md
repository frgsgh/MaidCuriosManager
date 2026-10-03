# 女仆饰品管理 Maid Curios Manager

Forge 1.20.1 小工具模组：通过 GUI 管理**车万女仆（Touhou Little Maid）**身上的
**Curios 饰品数据**（该数据保存在女仆实体 NBT 的 Curios 能力中）。

## 功能

- **潜行 + 右键女仆** 打开「饰品管理」界面（可在配置中去掉潜行要求）
- 界面列出女仆身上的所有 Curios 槽位类型：
  - 每类槽位显示「已用/总数」，支持 **−槽 / +槽** 增减槽位数量
  - 每个已装备的饰品一行，显示物品图标与名称
  - 通过 **− / +** 按钮或直接**输入框输入数字**修改饰品堆叠数量
- **保存**：把待办修改逐条发送到服务端，写入女仆 Curios 实体能力（随女仆 NBT 持久化），
  Curios 自动同步给所有客户端
- **重置**：丢弃未保存的修改，重新读取女仆当前数据
- **NBT**：只读预览每类槽位的 Curios NBT 数据
- 收缩槽位时自动保护：末尾槽仍有饰品则不允许收缩，避免饰品丢失

## 操作说明

| 按键 | 行为 |
| --- | --- |
| 潜行 + 右键女仆 | 打开饰品管理界面 |
| 普通右键女仆 | 保持车万女仆自带菜单（不冲突） |

## 配置（config/maidcuriosmanager-common.toml）

| 选项 | 默认 | 说明 |
| --- | --- | --- |
| requireSneak | true | 是否必须潜行右键才打开界面 |
| maxSlotsPerType | 8 | 每类槽位可扩充到的最大槽位数 |
| maxStackCount | 64 | 饰品堆叠数量可设置的上限 |

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
├── MaidCuriosConfig.java         # Forge 配置
├── MaidCuriosEvents.java         # 服务端右键拦截
├── MaidCuriosNetwork.java        # SimpleChannel 网络通道
├── MaidTypeHelper.java           # 车万女仆反射软依赖
├── network/CurioEditMessage.java # C2S 修改请求包
└── client/
    ├── ClientRegistration.java   # 客户端注册入口
    ├── ClientInteractHandler.java# 客户端右键打开界面
    └── gui/MaidCuriosScreen.java # 饰品管理 GUI

src/curiosapi/java/               # Curios API 源码（仅编译期使用，不打入 JAR）

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