package com.maidcurios.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 本模组的快捷键。
 *
 * <p>只保留一个：在女仆界面内直接打开饰品管理界面。它注册进原版的按键系统，
 * 会出现在「选项 → 按键绑定」中独立的「女仆饰品管理」条目下，可自行改键；
 * 默认不绑定（{@link GLFW#GLFW_KEY_UNKNOWN}），以免与整合包里其他模组的键位冲突。
 *
 * <p>同一个 {@link KeyMapping} 也会被注入到车万女仆的「全局设置」界面
 * （见 {@link MaidCuriosConfigScreen}），在那里改键与在原版「按键绑定」里改键等价。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidCuriosKeybinds {
    /** 原版「按键绑定」中的分类名（独立条目「女仆饰品管理」）。 */
    public static final String CATEGORY = "key.categories.maidcuriosmanager";

    /** 女仆界面内直接打开饰品管理界面。 */
    public static final KeyMapping OPEN_MANAGER_IN_MAID_GUI = new KeyMapping(
            "key.maidcuriosmanager.open_manager_in_maid_gui",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private MaidCuriosKeybinds() {
    }

    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_MANAGER_IN_MAID_GUI);
    }
}
