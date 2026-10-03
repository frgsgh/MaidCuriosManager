package com.maidcurios.client;

import com.maidcurios.MaidCuriosManager;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * 车万女仆 GUI 侧软依赖桥接（反射实现）。
 *
 * <p>本模组对车万女仆始终是<b>软依赖</b>：车万女仆并未发布到任何公共 Maven 仓库，
 * 编译期无法引用其类，因此这里全部通过反射按类名解析（与 {@code MaidTypeHelper} 同一思路）。
 * 未安装车万女仆时，所有方法安全地返回 null。
 *
 * <p>只保留实际需要的能力：取「当前女仆界面正在查看的女仆」。
 * 原侧边栏按钮相关的事件解析方法已随该入口一并移除。
 */
@OnlyIn(Dist.CLIENT)
public final class TlmCompat {
    private static final String CLS_MAID_GUI =
            "com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui";

    private static Class<?> maidGuiClass;
    private static Method getMaidMethod;
    private static boolean resolved;

    private TlmCompat() {
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            maidGuiClass = Class.forName(CLS_MAID_GUI);
            getMaidMethod = maidGuiClass.getMethod("getMaid");
        } catch (Throwable t) {
            maidGuiClass = null;
            getMaidMethod = null;
            MaidCuriosManager.LOGGER.info(
                    "Touhou Little Maid GUI classes not found; the maid GUI hotkey will do nothing.");
        }
    }

    /**
     * 取当前女仆界面正在查看的女仆；没有则返回 null。
     *
     * <p>本模组自己的 {@code MaidCuriosScreen} 继承 {@code Screen}，不是
     * {@code AbstractMaidContainerGui}，所以打开本模组界面时这里返回 null，不会自我触发。
     */
    @Nullable
    public static LivingEntity getCurrentMaidGuiMaid() {
        resolve();
        if (maidGuiClass == null || getMaidMethod == null) {
            return null;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !maidGuiClass.isInstance(screen)) {
            return null;
        }
        try {
            Entity maid = (Entity) getMaidMethod.invoke(screen);
            return maid instanceof LivingEntity living ? living : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
