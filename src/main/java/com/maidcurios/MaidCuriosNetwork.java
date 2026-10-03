package com.maidcurios;

import com.maidcurios.network.CurioEditMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 简单的 Forge SimpleChannel 网络通道：客户端 → 服务端 的饰品修改请求。
 */
public final class MaidCuriosNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MaidCuriosManager.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private MaidCuriosNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, CurioEditMessage.class,
                CurioEditMessage::encode, CurioEditMessage::decode, CurioEditMessage::handle);
    }
}