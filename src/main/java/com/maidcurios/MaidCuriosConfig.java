package com.maidcurios;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 模组通用配置（config/maidcuriosmanager-common.toml）。
 */
public final class MaidCuriosConfig {
    public static final ForgeConfigSpec SPEC;

    /**
     * 全局总开关（显示在车万女仆「全局设置」界面）。
     *
     * <p>关闭后本模组完全停用：不注册/不响应按键、右键女仆不再打开管理界面、
     * 女仆界面上的入口按钮也不显示。本开关只控制本模组自身，
     * 与车万女仆自带的 {@code enable_maid_curios}（是否启用女仆饰品栏）互不影响。
     */
    public static final ForgeConfigSpec.BooleanValue ENABLE_MANAGER;

    /** 是否要求潜行 + 右键女仆才打开管理界面（默认 true，避免与车万女仆自带右键菜单冲突）。 */
    public static final ForgeConfigSpec.BooleanValue REQUIRE_SNEAK;

    /** 每类饰品槽位通过本模组最多可扩充到的槽位数上限。 */
    public static final ForgeConfigSpec.IntValue MAX_SLOTS_PER_TYPE;

    /** 单个饰品槽位可通过本模组设置的堆叠数量上限。 */
    public static final ForgeConfigSpec.IntValue MAX_STACK_COUNT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        ENABLE_MANAGER = builder
                .comment(
                        "Master switch for Maid Curios Manager.",
                        "本模组的总开关。关闭后：按键不响应、右键女仆不再打开饰品管理界面、女仆界面上的入口也不显示。",
                        "This only controls this mod. It does NOT change Touhou Little Maid's own",
                        "'enable_maid_curios' option, which is what actually enables the maid's curio slots.",
                        "它不影响车万女仆自带的 enable_maid_curios（那才是真正启用女仆饰品栏的开关）。")
                .define("enableManager", true);

        REQUIRE_SNEAK = builder
                .comment(
                        "Require the player to sneak (shift) while right-clicking a maid to open the curio manager screen.",
                        "潜行时右键女仆才会打开饰品管理界面，避免与车万女仆自带的右键菜单冲突。",
                        "Set to false to open the manager on every right-click on a maid.")
                .define("requireSneak", true);

        MAX_SLOTS_PER_TYPE = builder
                .comment(
                        "Maximum allowed slots per curio slot type after growing via this mod.",
                        "每类饰品槽位通过本模组最多可扩充到的槽位数（默认与事实上限 64 一致）。")
                .defineInRange("maxSlotsPerType", 64, 1, 64);

        MAX_STACK_COUNT = builder
                .comment(
                        "Upper bound for the stack count a single curio slot can be set to via this mod.",
                        "单个饰品槽位可通过本模组设置的堆叠数量上限。")
                .defineInRange("maxStackCount", 64, 1, 4096);

        SPEC = builder.build();
    }

    private MaidCuriosConfig() {
    }

    /**
     * 本模组是否应当工作：需要车万女仆已加载，且总开关为开。
     *
     * <p>配置尚未加载时（极早期）按「开启」处理，避免启动阶段误判为关闭。
     */
    public static boolean isManagerEnabled() {
        try {
            return MaidTypeHelper.isTouhouLittleMaidLoaded() && ENABLE_MANAGER.get();
        } catch (IllegalStateException e) {
            // 配置尚未加载
            return MaidTypeHelper.isTouhouLittleMaidLoaded();
        }
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}