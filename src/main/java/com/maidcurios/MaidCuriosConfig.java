package com.maidcurios;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 模组通用配置（config/maidcuriosmanager-common.toml）。
 */
public final class MaidCuriosConfig {
    public static final ForgeConfigSpec SPEC;

    /** 是否要求潜行 + 右键女仆才打开管理界面（默认 true，避免与车万女仆自带右键菜单冲突）。 */
    public static final ForgeConfigSpec.BooleanValue REQUIRE_SNEAK;

    /** 每类饰品槽位通过本模组最多可扩充到的槽位数上限。 */
    public static final ForgeConfigSpec.IntValue MAX_SLOTS_PER_TYPE;

    /** 单个饰品槽位可通过本模组设置的堆叠数量上限。 */
    public static final ForgeConfigSpec.IntValue MAX_STACK_COUNT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        REQUIRE_SNEAK = builder
                .comment(
                        "Require the player to sneak (shift) while right-clicking a maid to open the curio manager screen.",
                        "潜行时右键女仆才会打开饰品管理界面，避免与车万女仆自带的右键菜单冲突。",
                        "Set to false to open the manager on every right-click on a maid.")
                .define("requireSneak", true);

        MAX_SLOTS_PER_TYPE = builder
                .comment(
                        "Maximum allowed slots per curio slot type after growing via this mod.",
                        "每类饰品槽位通过本模组最多可扩充到的槽位数。")
                .defineInRange("maxSlotsPerType", 8, 1, 64);

        MAX_STACK_COUNT = builder
                .comment(
                        "Upper bound for the stack count a single curio slot can be set to via this mod.",
                        "单个饰品槽位可通过本模组设置的堆叠数量上限。")
                .defineInRange("maxStackCount", 64, 1, 4096);

        SPEC = builder.build();
    }

    private MaidCuriosConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}