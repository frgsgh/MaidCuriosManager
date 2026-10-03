package com.maidcurios;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 服务端侧的右键拦截：
 *
 * <p>当玩家潜行右键女仆且满足本模组条件时，取消这次实体交互，阻止车万女仆
 * 自带的右键菜单在服务端被打开（客户端同时会打开饰品管理界面）。
 */
public final class MaidCuriosEvents {
    private MaidCuriosEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return; // 客户端由 ClientInteractHandler 处理
        }
        if (!MaidCuriosConfig.isManagerEnabled()) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!MaidTypeHelper.isMaid(event.getTarget())) {
            return;
        }
        if (MaidCuriosConfig.REQUIRE_SNEAK.get() && !player.isShiftKeyDown()) {
            return;
        }
        // 与客户端保持一致，彻底阻断女仆原生的 mobInteract
        event.setCanceled(true);
    }
}