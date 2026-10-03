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

    /** Forge 事件总线：快捷键处理、配置界面注入。 */
    public static void registerForgeBus() {
        // 说明：以下两项已按需求停用，但源码保留在仓库中以免误删：
        //   ClientInteractHandler —— 潜行 + 右键女仆打开界面
        //   MaidGuiHooks          —— 女仆界面侧边栏的入口按钮
        MinecraftForge.EVENT_BUS.register(new MaidGuiKeyHandler());
        if (MaidCuriosClothCompat.isClothConfigLoaded()) {
            MinecraftForge.EVENT_BUS.register(new MaidCuriosConfigScreen());
        }
    }
}
