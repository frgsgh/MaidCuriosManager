package com.maidcurios.client;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.MaidCuriosManager;
import com.maidcurios.client.gui.MaidCuriosScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 在车万女仆「女仆主界面」的侧边栏上追加一个入口按钮。
 *
 * <p>按钮位置在 {@code SideTab} 最后一项（当前即「全局设置」）<b>下方一行</b>，
 * 因此它的对象永远是「当前正在看的这只女仆」，不会出现不知道对谁操作的问题。
 *
 * <p>通过车万女仆的公开 API {@code MaidContainerGuiEvent.Init} 注入，
 * 完全不修改车万女仆本体。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidGuiHooks {
    /** 侧边栏标签之间的行距（与车万女仆 MaidSideTabs.SPACING 一致）。 */
    private static final int SPACING = 25;
    /** 侧边栏标签的起始 X 偏移（leftPos + 251 + 5）。 */
    private static final int TAB_X_OFFSET = 256;
    /** 侧边栏标签的起始 Y 偏移（topPos + 28 + 9）。 */
    private static final int TAB_Y_OFFSET = 37;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 20;

    private static final String BUTTON_ID = "maidcuriosmanager:open_manager";

    @SubscribeEvent
    public void onMaidContainerGuiInit(Event event) {
        if (!MaidCuriosConfig.isManagerEnabled()) {
            return;
        }
        if (!TlmCompat.isMaidContainerGuiEvent(event)) {
            return;
        }
        // 只在「女仆主界面」加按钮，避免配置/任务等子界面重复出现
        if (!TlmCompat.isMainMaidGui(event)) {
            return;
        }
        LivingEntity maid = TlmCompat.getEventMaid(event);
        if (maid == null) {
            MaidCuriosManager.LOGGER.warn("[maidcurios] maid GUI init: could not resolve the maid entity.");
            return;
        }
        int leftPos = readInt(event, "getLeftPos");
        int topPos = readInt(event, "getTopPos");
        if (leftPos == Integer.MIN_VALUE || topPos == Integer.MIN_VALUE) {
            return;
        }

        int row = TlmCompat.lastSideTabIndex() + 1;
        int x = leftPos + TAB_X_OFFSET;
        int y = topPos + TAB_Y_OFFSET + row * SPACING;

        Button button = Button.builder(
                        Component.translatable("gui.maidcuriosmanager.open_manager"),
                        b -> Minecraft.getInstance().setScreen(new MaidCuriosScreen(maid)))
                .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();

        if (!addButton(event, BUTTON_ID, button)) {
            MaidCuriosManager.LOGGER.warn("Failed to inject the curio manager button into the maid GUI.");
        } else {
            MaidCuriosManager.LOGGER.info("[maidcurios] injected manager button at ({}, {}) for maid {}.",
                    x, y, maid.getName().getString());
        }
    }

    private static int readInt(Event event, String methodName) {
        try {
            Object value = event.getClass().getMethod(methodName).invoke(event);
            return value instanceof Integer i ? i : Integer.MIN_VALUE;
        } catch (Throwable t) {
            return Integer.MIN_VALUE;
        }
    }

    private static boolean addButton(Event event, String id, Button button) {
        try {
            event.getClass().getMethod("addButton", String.class,
                    net.minecraft.client.gui.components.AbstractWidget.class).invoke(event, id, button);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
