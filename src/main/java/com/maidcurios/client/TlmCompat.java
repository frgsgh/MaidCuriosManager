package com.maidcurios.client;

import com.maidcurios.MaidCuriosManager;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * 车万女仆 GUI 侧软依赖桥接（反射实现）。
 *
 * <p>本模组对车万女仆始终是<b>软依赖</b>：车万女仆并未发布到任何公共 Maven 仓库，
 * 编译期无法引用其类，因此这里全部通过反射按类名解析（与 {@code MaidTypeHelper} 同一思路）。
 * 未安装车万女仆时，所有方法安全地返回 null / 不做任何事。
 */
@OnlyIn(Dist.CLIENT)
public final class TlmCompat {
    private static final String CLS_MAID_GUI =
            "com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui";
    private static final String CLS_MAIN_GUI_EVENT =
            "com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent";
    private static final String CLS_SIDE_TAB =
            "com.github.tartaricacid.touhoulittlemaid.entity.passive.SideTab";
    private static final String CLS_MAID_CONFIG_GUI =
            "com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.config.MaidConfigContainerGui";
    private static final String CLS_TASK_CONFIG_GUI =
            "com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.task.MaidTaskConfigGui";

    private static Class<?> maidGuiClass;
    private static Class<?> mainGuiEventClass;
    private static Class<?> sideTabClass;
    private static Class<?> maidConfigGuiClass;
    private static Class<?> taskConfigGuiClass;

    private static Method getMaidMethod;
    private static Method isInstanceMethod;
    private static Method getIndexMethod;
    private static Method valuesMethod;

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
        }
        try {
            mainGuiEventClass = Class.forName(CLS_MAIN_GUI_EVENT);
            isInstanceMethod = mainGuiEventClass.getMethod("isInstance", Event.class);
        } catch (Throwable t) {
            mainGuiEventClass = null;
            isInstanceMethod = null;
        }
        try {
            sideTabClass = Class.forName(CLS_SIDE_TAB);
            valuesMethod = sideTabClass.getMethod("values");
            getIndexMethod = sideTabClass.getMethod("getIndex");
        } catch (Throwable t) {
            sideTabClass = null;
            valuesMethod = null;
            getIndexMethod = null;
        }
        try {
            maidConfigGuiClass = Class.forName(CLS_MAID_CONFIG_GUI);
        } catch (Throwable t) {
            maidConfigGuiClass = null;
        }
        try {
            taskConfigGuiClass = Class.forName(CLS_TASK_CONFIG_GUI);
        } catch (Throwable t) {
            taskConfigGuiClass = null;
        }
        if (maidGuiClass == null) {
            MaidCuriosManager.LOGGER.info("Touhou Little Maid GUI classes not found; maid GUI entry stays hidden.");
        }
    }

    /** 传入的 Forge 事件是否是 {@code MaidContainerGuiEvent} 的子类实例。 */
    public static boolean isMaidContainerGuiEvent(Event event) {
        resolve();
        if (isInstanceMethod == null) {
            return false;
        }
        try {
            return (Boolean) isInstanceMethod.invoke(null, event);
        } catch (Throwable t) {
            return false;
        }
    }

    /** 侧边栏标签的最大索引（车万女仆当前为 GLOBAL_CONFIG = 1）。取不到时退回 1。 */
    public static int lastSideTabIndex() {
        resolve();
        if (valuesMethod == null || getIndexMethod == null) {
            return 1;
        }
        int max = 0;
        try {
            Object[] values = (Object[]) valuesMethod.invoke(null);
            if (values != null) {
                for (Object value : values) {
                    Object index = getIndexMethod.invoke(value);
                    if (index instanceof Integer value2 && value2 > max) {
                        max = value2;
                    }
                }
            }
        } catch (Throwable t) {
            return 1;
        }
        return max;
    }

    /** 从 {@code MaidContainerGuiEvent} 里取出女仆实体；取不到返回 null。 */
    @Nullable
    public static LivingEntity getEventMaid(Event event) {
        resolve();
        if (getMaidMethod == null) {
            return null;
        }
        try {
            Method getGui = event.getClass().getMethod("getGui");
            Object gui = getGui.invoke(event);
            return invokeGetMaid(gui);
        } catch (Throwable t) {
            return null;
        }
    }

    /** 该事件对应的界面是否是「女仆主界面」（用于只在那里加按钮）。 */
    public static boolean isMainMaidGui(Event event) {
        resolve();
        if (maidGuiClass == null) {
            return false;
        }
        try {
            Object gui = event.getClass().getMethod("getGui").invoke(event);
            if (gui == null || !maidGuiClass.isInstance(gui)) {
                return false;
            }
            // 注意：车万女仆的「女仆主界面」本身就是各种背包界面
            // （EmptyBackpackContainerScreen / SmallBackpackContainerScreen ...），
            // 它们都实现了 IBackpackContainerScreen，因此**不能**按背包来排除。
            // 真正需要排除的是「配置 / 任务配置」这类子页面。
            if (maidConfigGuiClass != null && maidConfigGuiClass.isInstance(gui)) {
                return false;
            }
            if (taskConfigGuiClass != null && taskConfigGuiClass.isInstance(gui)) {
                return false;
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 当前是否开着女仆界面（含背包/饰品等子界面）。
     *
     * <p>TPLM 的 {@code MaidCuriosScreen} 继承 {@code Screen}，不是 {@code AbstractMaidContainerGui}，
     * 所以打开本模组界面时这里返回 false，不会自我触发。
     */
    public static boolean isMaidGuiOpen() {
        resolve();
        if (maidGuiClass == null) {
            return false;
        }
        Screen screen = Minecraft.getInstance().screen;
        return screen != null && maidGuiClass.isInstance(screen);
    }

    /** 取当前女仆界面正在查看的女仆；没有则返回 null。 */
    @Nullable
    public static LivingEntity getCurrentMaidGuiMaid() {
        resolve();
        if (maidGuiClass == null) {
            return null;
        }
        Screen screen = Minecraft.getInstance().screen;
        if (screen == null || !maidGuiClass.isInstance(screen)) {
            return null;
        }
        return invokeGetMaid(screen);
    }

    @Nullable
    private static LivingEntity invokeGetMaid(Object gui) {
        if (getMaidMethod == null || gui == null) {
            return null;
        }
        try {
            Entity maid = (Entity) getMaidMethod.invoke(gui);
            return maid instanceof LivingEntity living ? living : null;
        } catch (Throwable t) {
            return null;
        }
    }
}
