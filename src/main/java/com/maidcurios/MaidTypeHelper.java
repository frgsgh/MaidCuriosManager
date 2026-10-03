package com.maidcurios;

import net.minecraft.world.entity.Entity;

/**
 * 车万女仆（Touhou Little Maid）软依赖辅助类。
 *
 * <p>不直接 import 女仆实体类，而是通过反射按类名加载，这样即使未安装车万女仆，
 * 本模组也能正常加载（只是不会有任何交互入口）。
 */
public final class MaidTypeHelper {
    private static Class<?> maidClass;

    private MaidTypeHelper() {
    }

    /** 在 CommonSetup 阶段调用，缓存女仆实体类。 */
    public static void init() {
        try {
            maidClass = Class.forName("com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid");
            MaidCuriosManager.LOGGER.info("Touhou Little Maid detected: EntityMaid resolved.");
        } catch (Throwable t) {
            maidClass = null;
            MaidCuriosManager.LOGGER.info("Touhou Little Maid not found, curio manager is disabled.");
        }
    }

    public static boolean isMaid(Entity entity) {
        return entity != null && maidClass != null && maidClass.isInstance(entity);
    }

    public static boolean isTouhouLittleMaidLoaded() {
        return maidClass != null;
    }
}