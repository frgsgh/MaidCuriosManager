package com.maidcurios.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 本模组的两个快捷键。
 *
 * <p>两者都注册进原版的按键系统，因此会出现在「选项 → 按键绑定」列表里，
 * 由玩家自行改键；默认均不绑定（{@link GLFW#GLFW_KEY_UNKNOWN}），
 * 以免与整合包里其他模组的键位冲突。
 *
 * <ul>
 *   <li>{@link #OPEN_MANAGER}：随时随地打开<b>正在注视</b>的女仆的饰品管理界面；
 *       若当前开着女仆界面，则直接对当前女仆生效。</li>
 *   <li>{@link #OPEN_MANAGER_IN_MAID_GUI}：仅在女仆界面内生效，对当前女仆打开饰品管理界面。</li>
 * </ul>
 *
 * <p>同一个 {@link KeyMapping} 也会被注入到车万女仆的「全局设置」界面
 * （见 {@link MaidCuriosConfigScreen}），在那里改键与在原版「按键绑定」里改键等价。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidCuriosKeybinds {
    /** 原版「按键绑定」中的分类名，与车万女仆保持一致。 */
    public static final String CATEGORY = "key.categories.touhou_little_maid";

    public static final KeyMapping OPEN_MANAGER = new KeyMapping(
            "key.maidcuriosmanager.open_manager",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    public static final KeyMapping OPEN_MANAGER_IN_MAID_GUI = new KeyMapping(
            "key.maidcuriosmanager.open_manager_in_maid_gui",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private MaidCuriosKeybinds() {
    }

    public static void register(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_MANAGER);
        event.register(OPEN_MANAGER_IN_MAID_GUI);
    }
}
