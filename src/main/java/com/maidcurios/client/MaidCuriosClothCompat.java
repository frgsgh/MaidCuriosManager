package com.maidcurios.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

/**
 * Cloth Config 存在性检查。
 *
 * <p>车万女仆的「全局设置」依赖 Cloth Config；没有它时该界面本身也不存在，
 * 因此本模组只在其存在时才注册配置注入监听器。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidCuriosClothCompat {
    private static final String MOD_ID = "cloth_config";

    private MaidCuriosClothCompat() {
    }

    public static boolean isClothConfigLoaded() {
        try {
            return ModList.get().isLoaded(MOD_ID);
        } catch (Throwable t) {
            return false;
        }
    }
}
