package com.maidcurios.client;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.client.gui.MaidCuriosScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 快捷键处理：在女仆界面内直接对当前女仆打开饰品管理界面。
 *
 * <p>本模组只保留这一个快捷键。它只在女仆界面（含背包等子界面）打开时生效：
 * 界面打开时 {@code minecraft.screen != null}，所以必须挂在
 * {@link InputEvent.Key} 上处理，而不是 ClientTickEvent。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidGuiKeyHandler {
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen == null) {
            return;
        }
        if (!MaidCuriosConfig.isManagerEnabled()) {
            return;
        }
        KeyMapping key = MaidCuriosKeybinds.OPEN_MANAGER_IN_MAID_GUI;
        // 默认未绑定（GLFW_KEY_UNKNOWN）时不响应，避免误触
        int boundKey = key.getKey().getValue();
        if (boundKey == GLFW.GLFW_KEY_UNKNOWN || boundKey != event.getKey()) {
            return;
        }
        LivingEntity maid = TlmCompat.getCurrentMaidGuiMaid();
        if (maid != null) {
            minecraft.setScreen(new MaidCuriosScreen(maid));
        }
    }
}
