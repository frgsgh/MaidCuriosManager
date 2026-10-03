package com.maidcurios.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * 仅在客户端加载：注册快捷键、界面钩子与配置注入。
 */
public final class ClientRegistration {
    private ClientRegistration() {
    }

    /** 模组事件总线：注册按键（必须走模组总线）。 */
    public static void registerModBus(IEventBus modBus) {
        modBus.addListener(MaidCuriosKeybinds::register);
    }

    /** Forge 事件总线：界面钩子、快捷键处理、配置界面注入。 */
    public static void registerForgeBus() {
        MinecraftForge.EVENT_BUS.register(ClientInteractHandler.class);
        MinecraftForge.EVENT_BUS.register(new MaidGuiHooks());
        MinecraftForge.EVENT_BUS.register(new MaidGuiKeyHandler());
        if (MaidCuriosClothCompat.isClothConfigLoaded()) {
            MinecraftForge.EVENT_BUS.register(new MaidCuriosConfigScreen());
        }
    }
}
