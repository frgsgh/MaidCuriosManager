package com.maidcurios;

import com.maidcurios.client.ClientRegistration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

/**
 * 女仆饰品管理（Maid Curios Manager）
 *
 * <p>一个 Forge 1.20.1 小工具模组：潜行右键车万女仆打开「饰品管理」界面，列出
 * 该女仆身上的所有 Curios 槽位，支持增减槽位数量、修改饰品堆叠数量，修改会写入
 * 女仆实体 NBT（通过 Curios 的实体能力持久化并自动同步）。
 */
@Mod(MaidCuriosManager.MODID)
public final class MaidCuriosManager {
    public static final String MODID = "maidcuriosmanager";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MaidCuriosManager() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);

        // 通用（服务端）配置
        MaidCuriosConfig.register();

        // 服务端侧拦截右键，防止车万女仆自带的右键菜单被同时打开
        MinecraftForge.EVENT_BUS.register(MaidCuriosEvents.class);

        // 客户端侧拦截右键并打开饰品管理界面，同时注册快捷键与界面钩子
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientRegistration::registerForgeBus);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientRegistration.registerModBus(modBus));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MaidCuriosNetwork.register();
            MaidTypeHelper.init();
            LOGGER.info("Maid Curios Manager {} loaded. Touhou Little Maid present: {}, maxSlotsPerType config: {}",
                    ModList.get().getModContainerById(MODID).map(c -> c.getModInfo().getVersion().toString()).orElse("?"),
                    MaidTypeHelper.isTouhouLittleMaidLoaded(),
                    safeMaxSlots());
        });
    }

    private static String safeMaxSlots() {
        try {
            return String.valueOf(MaidCuriosConfig.MAX_SLOTS_PER_TYPE.get());
        } catch (IllegalStateException e) {
            return "config not loaded yet";
        }
    }
}