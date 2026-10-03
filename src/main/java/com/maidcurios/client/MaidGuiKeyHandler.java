package com.maidcurios.client;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.client.gui.MaidCuriosScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 快捷键处理：让玩家随时用按键打开饰品管理界面。
 *
 * <p>目标女仆的解析顺序：
 * <ol>
 *   <li>如果正开着女仆界面（含背包等子界面），就用该界面正在查看的女仆；</li>
 *   <li>否则用准星正在注视的女仆（原版交互距离）；</li>
 *   <li>都没有则在快捷栏上方提示，不做任何事。</li>
 * </ol>
 */
@OnlyIn(Dist.CLIENT)
public final class MaidGuiKeyHandler {
    /** 准星拾取距离，与原版交互距离一致。 */
    private static final double REACH = 4.5D;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null) {
            return;
        }
        if (!MaidCuriosConfig.isManagerEnabled()) {
            return;
        }
        while (MaidCuriosKeybinds.OPEN_MANAGER.consumeClick()) {
            openForLookedAtMaid(minecraft);
        }
        while (MaidCuriosKeybinds.OPEN_MANAGER_IN_MAID_GUI.consumeClick()) {
            // 该键只在女仆界面内生效；界面外按下不做事
        }
    }

    /**
     * 女仆界面内的按键：对当前女仆打开管理界面。
     *
     * <p>界面打开时 {@code minecraft.screen != null}，所以必须在 Screen 层处理按键，
     * 这里挂在 {@link InputEvent.Key} 上。
     */
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
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
        if (boundKey == org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN || boundKey != event.getKey()) {
            return;
        }
        LivingEntity maid = TlmCompat.getCurrentMaidGuiMaid();
        if (maid != null) {
            minecraft.setScreen(new MaidCuriosScreen(maid));
        }
    }

    /** 打开「正在注视的女仆」的饰品管理界面；没有目标时给出提示。 */
    private static void openForLookedAtMaid(Minecraft minecraft) {
        LivingEntity maid = TlmCompat.getCurrentMaidGuiMaid();
        if (maid == null) {
            maid = findLookedAtMaid(minecraft);
        }
        if (maid == null) {
            Player player = minecraft.player;
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.maidcuriosmanager.no_maid_in_sight"), true);
            }
            return;
        }
        minecraft.setScreen(new MaidCuriosScreen(maid));
    }

    /** 准星指向的女仆实体；没有则返回 null。 */
    @Nullable
    private static LivingEntity findLookedAtMaid(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            return null;
        }
        EntityHitResult hit = pick(player);
        if (hit == null) {
            return null;
        }
        Entity entity = hit.getEntity();
        if (entity instanceof LivingEntity living && com.maidcurios.MaidTypeHelper.isMaid(entity)) {
            return living;
        }
        return null;
    }

    @Nullable
    private static EntityHitResult pick(Player player) {
        var hitResult = player.pick(REACH, 0.0F, false);
        return hitResult instanceof EntityHitResult entityHit ? entityHit : null;
    }
}
