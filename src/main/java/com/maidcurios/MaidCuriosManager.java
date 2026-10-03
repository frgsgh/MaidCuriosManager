package com.maidcurios;

import com.maidcurios.client.ClientRegistration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * 女仆饰品管理（Maid Curios Manager）
 *
 * <p>一个 Forge 1.20.1 小工具模组：在女仆界面内用快捷键打开「饰品管理」界面，列出
 * 该女仆身上的所有 Curios 槽位，支持增减槽位数量、修改饰品堆叠数量，修改会写入
 * 女仆实体 NBT（通过 Curios 的实体能力持久化并自动同步）。
 *
 * <p>入口只有一个：可改键的快捷键（默认未绑定），仅在该女仆界面打开时生效。
 * 原「潜行 + 右键女仆」入口已按需求移除。
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

        // 注意：服务端侧原来还会拦截「潜行 + 右键女仆」，该功能已按需求移除，
        // 因此这里不再注册 MaidCuriosEvents（入口只保留一个可改键的快捷键）。

        // 客户端侧：注册快捷键与配置注入
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
            warnIfConfigIsStale();
        });
    }

    /**
     * 提醒配置上限偏低的情况。
     *
     * <p>Forge 不会因为默认值改变而覆盖玩家已有的配置文件。老版本默认值是 8，
     * 因此升级后旧的 config/maidcuriosmanager-common.toml 仍会写着 8，
     * 表现为「槽位最多只能加到 8」。这里在启动时明确报出来，免得再去猜。
     */
    private static void warnIfConfigIsStale() {
        try {
            int configured = MaidCuriosConfig.MAX_SLOTS_PER_TYPE.get();
            if (configured < 64) {
                LOGGER.warn("maidcuriosmanager: maxSlotsPerType is {} (the mod default is 64). "
                                + "This value comes from your existing config/maidcuriosmanager-common.toml, "
                                + "which Forge does not overwrite. Raise it to 64 (or delete that file so it "
                                + "regenerates) if you want more than {} slots per slot type.",
                        configured, configured);
            }
        } catch (IllegalStateException e) {
            LOGGER.warn("maidcuriosmanager: could not read maxSlotsPerType (config not loaded).");
        }
    }

    private static String safeMaxSlots() {
        try {
            return String.valueOf(MaidCuriosConfig.MAX_SLOTS_PER_TYPE.get());
        } catch (IllegalStateException e) {
            return "config not loaded yet";
        }
    }
}