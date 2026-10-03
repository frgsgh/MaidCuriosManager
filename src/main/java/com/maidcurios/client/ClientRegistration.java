package com.maidcurios.client;

import net.minecraftforge.common.MinecraftForge;

/**
 * 仅在客户端加载：把右键打开界面的处理器注册到 Forge 事件总线。
 */
public final class ClientRegistration {
    private ClientRegistration() {
    }

    public static void registerForgeBus() {
        MinecraftForge.EVENT_BUS.register(ClientInteractHandler.class);
    }
}