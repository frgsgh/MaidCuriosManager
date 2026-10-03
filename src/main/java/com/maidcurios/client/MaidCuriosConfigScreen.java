package com.maidcurios.client;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.MaidCuriosManager;
import java.lang.reflect.Field;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 把本模组的开关与快捷键注入到车万女仆的「全局设置」界面（Cloth Config）。
 *
 * <p>用公开 API {@code AddClothConfigEvent} 注入，不修改车万女仆本体。
 * Cloth Config 的 {@code fillKeybindingField} 会直接读写同一个 {@link net.minecraft.client.KeyMapping}，
 * 所以在「全局设置」里改键与在「选项 → 按键绑定」里改键完全等价。
 */
@OnlyIn(Dist.CLIENT)
public final class MaidCuriosConfigScreen {
    private static final String CLS_CLOTH_EVENT =
            "com.github.tartaricacid.touhoulittlemaid.api.event.client.AddClothConfigEvent";

    private static Class<?> clothEventClass;
    private static boolean resolved;

    @SubscribeEvent
    public void onAddClothConfig(Event event) {
        if (!isClothConfigEvent(event)) {
            return;
        }
        Object root = call(event, "getRoot");
        Object entryBuilder = call(event, "getEntryBuilder");
        if (!(root instanceof ConfigBuilder configBuilder) || !(entryBuilder instanceof ConfigEntryBuilder entry)) {
            MaidCuriosManager.LOGGER.warn("Unexpected AddClothConfigEvent payload; skipping config injection.");
            return;
        }

        ConfigCategory category = configBuilder.getOrCreateCategory(
                Component.translatable("config.maidcuriosmanager.category"));

        category.addEntry(entry.startBooleanToggle(
                        Component.translatable("config.maidcuriosmanager.enable_manager"),
                        MaidCuriosConfig.ENABLE_MANAGER.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.maidcuriosmanager.enable_manager.tooltip"))
                .setSaveConsumer(MaidCuriosConfig.ENABLE_MANAGER::set)
                .build());

        Object inGuiKey = keyMappingField("OPEN_MANAGER_IN_MAID_GUI");
        if (inGuiKey instanceof net.minecraft.client.KeyMapping mapping) {
            category.addEntry(entry.fillKeybindingField(
                            Component.translatable("config.maidcuriosmanager.open_manager_in_gui_key"), mapping)
                    .setTooltip(Component.translatable("config.maidcuriosmanager.open_manager_in_gui_key.tooltip"))
                    .build());
        }

        MaidCuriosManager.LOGGER.info("[maidcurios] injected settings (enable toggle + hotkey) into TLM global config.");
    }

    /**
     * 通过反射读取 {@link MaidCuriosKeybinds} 的字段。
     *
     * <p>这样在未安装 Cloth Config 时，JVM 不会去解析 {@code MaidCuriosKeybinds} 里
     * {@code Dist.CLIENT} 专有的类型，避免「在错误的一侧加载类」的崩溃。
     */
    private static Object keyMappingField(String name) {
        try {
            Field field = MaidCuriosKeybinds.class.getField(name);
            return field.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isClothConfigEvent(Event event) {
        if (!resolved) {
            resolved = true;
            try {
                clothEventClass = Class.forName(CLS_CLOTH_EVENT);
            } catch (Throwable t) {
                clothEventClass = null;
            }
        }
        return clothEventClass != null && clothEventClass.isInstance(event);
    }

    private static Object call(Event event, String methodName) {
        try {
            return event.getClass().getMethod(methodName).invoke(event);
        } catch (Throwable t) {
            return null;
        }
    }
}
