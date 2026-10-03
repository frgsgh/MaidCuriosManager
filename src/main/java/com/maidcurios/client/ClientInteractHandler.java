package com.maidcurios.client;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.MaidTypeHelper;
import com.maidcurios.client.gui.MaidCuriosScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 客户端侧的右键拦截：潜行右键女仆时取消原生交互，并打开「饰品管理」界面。
 */
@OnlyIn(Dist.CLIENT)
public final class ClientInteractHandler {
    private ClientInteractHandler() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
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
        if (Minecraft.getInstance().player == null) {
            return;
        }
        // 取消交互，避免车万女仆同时打开自己的界面
        event.setCanceled(true);
        if (event.getTarget() instanceof LivingEntity living) {
            Minecraft.getInstance().setScreen(new MaidCuriosScreen(living));
        }
    }
}