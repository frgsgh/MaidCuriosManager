package com.maidcurios.client.gui;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.MaidCuriosManager;
import com.maidcurios.MaidCuriosNetwork;
import com.maidcurios.network.CurioEditMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * 「饰品管理」界面。
 *
 * <p>列出女仆身上的所有 Curios 槽位：
 * <ul>
 *   <li>每个槽位类型一行：显示已用/总槽位数量，提供「−槽」「+槽」按钮；</li>
 *   <li>每个已装备的饰品一行：显示物品图标/名称，提供「−」「+」与输入框修改堆叠数量；</li>
 *   <li>底部提供「NBT(查看)」「重置」「保存」按钮。</li>
 * </ul>
 *
 * <p>所有修改只记录在本地待办列表，点击「保存」后逐个发送到服务端，
 * 服务端修改女仆的 Curios 实体能力数据（该数据随女仆 NBT 持久化），
 * 并由 Curios 自动把改动同步回所有能看到该女仆的客户端。
 */
@OnlyIn(Dist.CLIENT)
public class MaidCuriosScreen extends Screen {
    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 210;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 30;
    private static final int LIST_W = GUI_WIDTH - 16;
    private static final int LIST_H = GUI_HEIGHT - LIST_Y - 60;

    private static final String KEY_TITLE = "maidcuriosmanager.gui.title";
    private static final String KEY_SUBTITLE = "maidcuriosmanager.gui.subtitle";
    private static final String KEY_NO_SLOTS = "maidcuriosmanager.gui.no_slots";
    private static final String KEY_HEADER_SLOTS = "maidcuriosmanager.gui.header_slots";
    private static final String KEY_SLOT_MINUS = "maidcuriosmanager.gui.slot_minus";
    private static final String KEY_SLOT_PLUS = "maidcuriosmanager.gui.slot_plus";
    private static final String KEY_COUNT_MINUS = "maidcuriosmanager.gui.count_minus";
    private static final String KEY_COUNT_PLUS = "maidcuriosmanager.gui.count_plus";
    private static final String KEY_SAVE = "maidcuriosmanager.gui.save";
    private static final String KEY_RESET = "maidcuriosmanager.gui.reset";
    private static final String KEY_NBT = "maidcuriosmanager.gui.nbt_view";
    private static final String KEY_NBT_BACK = "maidcuriosmanager.gui.nbt_back";
    private static final String KEY_SAVED = "maidcuriosmanager.gui.saved";
    private static final String KEY_UNSAVED = "maidcuriosmanager.gui.unsaved";
    private static final String KEY_NBT_HINT = "maidcuriosmanager.gui.nbt_hint";

    private final LivingEntity maid;
    private final List<Group> groups = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();
    private final List<Op> ops = new ArrayList<>();
    private final List<String> nbtLines = new ArrayList<>();

    private int left;
    private int top;
    private int scroll;
    private int maxScroll;
    private boolean nbtMode;
    private String status;

    private Button saveButton;
    private Button resetButton;
    private Button nbtButton;
    private final List<Button> rowButtons = new ArrayList<>();
    private final List<EditBox> countBoxes = new ArrayList<>();

    public MaidCuriosScreen(LivingEntity maid) {
        super(Component.translatable(KEY_TITLE));
        this.maid = maid;
        this.status = "";
        reload();
    }

    // ------------------------------------------------------------------
    // 数据模型
    // ------------------------------------------------------------------

    private static final class Group {
        final String id;
        int slots;
        final List<ItemStack> stacks = new ArrayList<>();

        Group(String id) {
            this.id = id;
        }

        int used() {
            int used = 0;
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    used++;
                }
            }
            return used;
        }
    }

    private static final class Row {
        static final int HEADER = 0;
        static final int ITEM = 1;

        final int kind;
        final Group group;
        final int index;

        Row(int kind, Group group, int index) {
            this.kind = kind;
            this.group = group;
            this.index = index;
        }
    }

    private static final class Op {
        final CurioEditMessage.Action action;
        final String slotId;
        final int index;
        final int value;

        Op(CurioEditMessage.Action action, String slotId, int index, int value) {
            this.action = action;
            this.slotId = slotId;
            this.index = index;
            this.value = value;
        }
    }

    /** 从 Curios 实体能力重新读取女仆饰品数据（客户端同步数据）。 */
    private void reload() {
        groups.clear();
        rows.clear();
        scroll = 0;
        maxScroll = 0;
        ops.clear();
        if (maid != null) {
            CuriosApi.getCuriosInventory(maid).ifPresent(handler -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                    Group group = new Group(entry.getKey());
                    IDynamicStackHandler stacks = entry.getValue().getStacks();
                    group.slots = stacks.getSlots();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        group.stacks.add(stacks.getStackInSlot(i).copy());
                    }
                    groups.add(group);
                }
            });
        }
        buildRows();
    }

    private void buildRows() {
        rows.clear();
        for (Group group : groups) {
            rows.add(new Row(Row.HEADER, group, -1));
            for (int i = 0; i < group.stacks.size(); i++) {
                if (!group.stacks.get(i).isEmpty()) {
                    rows.add(new Row(Row.ITEM, group, i));
                }
            }
        }
    }

    private void buildNbtLines() {
        nbtLines.clear();
        if (maid == null) {
            return;
        }
        CuriosApi.getCuriosInventory(maid).ifPresent(handler -> {
            for (Group group : groups) {
                nbtLines.add("[" + group.id + "] slots = " + group.slots);
                handler.getStacksHandler(group.id).ifPresent(sh -> {
                    String tag = sh.serializeNBT().toString();
                    StringBuilder builder = new StringBuilder(tag);
                    while (!builder.isEmpty()) {
                        String piece = font.plainSubstrByWidth(builder.toString(), LIST_W - 8);
                        nbtLines.add(piece);
                        builder.delete(0, piece.length());
                    }
                });
                nbtLines.add("");
            }
        });
        if (nbtLines.isEmpty()) {
            nbtLines.add(Component.translatable(KEY_NO_SLOTS).getString());
        }
    }

    // ------------------------------------------------------------------
    // 待办操作
    // ------------------------------------------------------------------

    private void queue(CurioEditMessage.Action action, Group group, int index, int value) {
        ops.add(new Op(action, group.id, index, value));
        applyPreview(action, group, index, value);
        status = "";
    }

    private void applyPreview(CurioEditMessage.Action action, Group group, int index, int value) {
        switch (action) {
            case GROW_SLOT -> {
                group.slots++;
                group.stacks.add(ItemStack.EMPTY);
            }
            case SHRINK_SLOT -> {
                if (group.slots > 1 && group.stacks.get(group.slots - 1).isEmpty()) {
                    group.slots--;
                    group.stacks.remove(group.slots);
                }
            }
            case SET_COUNT -> {
                if (index >= 0 && index < group.stacks.size()) {
                    ItemStack stack = group.stacks.get(index);
                    if (!stack.isEmpty()) {
                        int max = Math.max(1, Math.min(stack.getMaxStackSize(), MaidCuriosConfig.MAX_STACK_COUNT.get()));
                        stack.setCount(Math.max(1, Math.min(value, max)));
                    }
                }
            }
            default -> {
                // no-op
            }
        }
    }

    private void doSave() {
        commitAllBoxes();
        if (ops.isEmpty()) {
            status = Component.translatable(KEY_SAVED).getString();
            onClose();
            return;
        }
        for (Op op : ops) {
            MaidCuriosManager.LOGGER.info(
                    "[maidcurios] sending {} slot={} index={} value={} for maid id={}",
                    op.action, op.slotId, op.index, op.value, maid.getId());
            MaidCuriosNetwork.CHANNEL.sendToServer(
                    new CurioEditMessage(maid.getId(), op.slotId, op.index, op.action, op.value));
        }
        int count = ops.size();
        ops.clear();
        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(KEY_SAVED), false);
        MaidCuriosManager.LOGGER.info("Sent {} curio edit op(s) for maid {} (id={})", count, maid.getName(), maid.getId());
        onClose();
    }

    private void doReset() {
        commitAllBoxes();
        reload();
        rebuildRowWidgets();
    }

    private void toggleNbt() {
        commitAllBoxes();
        nbtMode = !nbtMode;
        if (nbtMode) {
            buildNbtLines();
        }
        rebuildRowWidgets();
    }

    // ------------------------------------------------------------------
    // 布局 / 控件
    // ------------------------------------------------------------------

    @Override
    protected void init() {
        this.left = (this.width - GUI_WIDTH) / 2;
        this.top = (this.height - GUI_HEIGHT) / 2;

        int bottomY = this.top + GUI_HEIGHT - 22;
        this.nbtButton = Button.builder(
                        Component.translatable(nbtMode ? KEY_NBT_BACK : KEY_NBT), b -> toggleNbt())
                .bounds(this.left + 8, bottomY, 52, 16)
                .build();
        this.resetButton = Button.builder(Component.translatable(KEY_RESET), b -> doReset())
                .bounds(this.left + 64, bottomY, 52, 16)
                .build();
        this.saveButton = Button.builder(Component.translatable(KEY_SAVE), b -> doSave())
                .bounds(this.left + GUI_WIDTH - 60, bottomY, 52, 16)
                .build();
        this.addRenderableWidget(nbtButton);
        this.addRenderableWidget(resetButton);
        this.addRenderableWidget(saveButton);

        rebuildRowWidgets();
    }

    private void removeRowWidgets() {
        for (Button button : rowButtons) {
            removeWidget(button);
        }
        rowButtons.clear();
        for (EditBox box : countBoxes) {
            removeWidget(box);
        }
        countBoxes.clear();
    }

    /** 按当前滚动位置重建行控件（槽位 ± 按钮、饰品数量 ± 按钮与输入框）。 */
    private void rebuildRowWidgets() {
        removeRowWidgets();
        if (nbtMode || rows.isEmpty()) {
            return;
        }
        int y = this.top + LIST_Y - scroll;
        int right = this.left + LIST_X + LIST_W;

        for (Row row : rows) {
            if (y < this.top + LIST_Y - 22 || y > this.top + LIST_Y + LIST_H + 22) {
                y += rowHeight(row);
                continue;
            }
            if (row.kind == Row.HEADER) {
                Group group = row.group;
                Button minus = rowButton(KEY_SLOT_MINUS, x -> queue(CurioEditMessage.Action.SHRINK_SLOT, group, -1, 1),
                        right - 50, y + 2, 18, 12);
                minus.active = canShrink(group);
                Button plus = rowButton(KEY_SLOT_PLUS, x -> queue(CurioEditMessage.Action.GROW_SLOT, group, -1, 1),
                        right - 26, y + 2, 18, 12);
                plus.active = canGrow(group);
            } else {
                Group group = row.group;
                int index = row.index;
                ItemStack stack = group.stacks.get(index);
                Button minus = rowButton(KEY_COUNT_MINUS,
                        x -> queue(CurioEditMessage.Action.SET_COUNT, group, index, stack.getCount() - 1),
                        right - 92, y + 2, 14, 12);
                minus.active = stack.getCount() > 1;
                EditBox box = new EditBox(this.font, right - 74, y + 1, 44, 14, Component.empty());
                box.setMaxLength(5);
                box.setFilter(s -> s.matches("\\d*"));
                box.setValue(Integer.toString(stack.getCount()));
                this.addRenderableWidget(box);
                countBoxes.add(box);
                Button plus = rowButton(KEY_COUNT_PLUS,
                        x -> queue(CurioEditMessage.Action.SET_COUNT, group, index, stack.getCount() + 1),
                        right - 26, y + 2, 14, 12);
                plus.active = canIncrement(stack);
            }
            y += rowHeight(row);
        }
    }

    private static int rowHeight(Row row) {
        return row.kind == Row.HEADER ? 18 : 16;
    }

    private Button rowButton(String key, Button.OnPress onPress, int x, int y, int w, int h) {
        Button button = Button.builder(Component.translatable(key), onPress)
                .bounds(x, y, w, h)
                .build();
        this.addRenderableWidget(button);
        rowButtons.add(button);
        return button;
    }

    private boolean canGrow(Group group) {
        return group.slots < MaidCuriosConfig.MAX_SLOTS_PER_TYPE.get();
    }

    private boolean canShrink(Group group) {
        return group.slots > 1 && group.stacks.get(group.slots - 1).isEmpty();
    }

    private boolean canIncrement(ItemStack stack) {
        return stack.getCount() < Math.min(stack.getMaxStackSize(), MaidCuriosConfig.MAX_STACK_COUNT.get());
    }

    /** 把所有数量输入框的当前值提交为待办操作。 */
    private void commitAllBoxes() {
        if (nbtMode) {
            return;
        }
        for (Row row : rows) {
            if (row.kind != Row.ITEM) {
                continue;
            }
            Group group = row.group;
            int index = row.index;
            EditBox box = findBoxFor(row);
            if (box == null) {
                continue;
            }
            String text = box.getValue().trim();
            if (text.isEmpty()) {
                continue;
            }
            int value;
            try {
                value = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                continue;
            }
            ItemStack stack = group.stacks.get(index);
            if (stack.isEmpty() || value == stack.getCount()) {
                continue;
            }
            ops.add(new Op(CurioEditMessage.Action.SET_COUNT, group.id, index, value));
        }
    }

    private EditBox findBoxFor(Row row) {
        // 输入框按行顺序创建，这里按 ITEM 行顺序索引
        int itemIndex = 0;
        for (int i = 0; i < rows.indexOf(row); i++) {
            if (rows.get(i).kind == Row.ITEM) {
                itemIndex++;
            }
        }
        return itemIndex < countBoxes.size() ? countBoxes.get(itemIndex) : null;
    }

    // ------------------------------------------------------------------
    // 渲染
    // ------------------------------------------------------------------

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        if (nbtMode) {
            renderNbt(guiGraphics);
        } else {
            renderList(guiGraphics);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 标题
        Component title = Component.translatable(KEY_TITLE);
        guiGraphics.drawString(this.font, title, this.left + (GUI_WIDTH - this.font.width(title)) / 2, this.top + 6, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable(KEY_SUBTITLE).getString() + " · "
                + maid.getName().getString(), this.left + 8, this.top + 19, 0xFFA0A0A0);

        // 状态行
        if (!status.isEmpty()) {
            guiGraphics.drawString(this.font, status, this.left + 8, this.top + GUI_HEIGHT - 32, 0xFFFFFF55);
        }
        if (!ops.isEmpty() && !nbtMode) {
            guiGraphics.drawString(this.font, Component.translatable(KEY_UNSAVED, ops.size()).getString(),
                    this.left + 8, this.top + GUI_HEIGHT - 32, 0xFFFFFFAA);
        }
    }

    private void renderList(GuiGraphics guiGraphics) {
        // 列表背景
        guiGraphics.fill(this.left + LIST_X - 2, this.top + LIST_Y - 2,
                this.left + LIST_X + LIST_W + 2, this.top + LIST_Y + LIST_H + 2, 0x88000000);
        int y = this.top + LIST_Y - scroll;
        int right = this.left + LIST_X + LIST_W;

        for (Row row : rows) {
            if (y < this.top + LIST_Y - 24 || y > this.top + LIST_Y + LIST_H + 24) {
                y += rowHeight(row);
                continue;
            }
            if (row.kind == Row.HEADER) {
                Group group = row.group;
                guiGraphics.fill(this.left + LIST_X, y, right, y + 16, 0x55333333);
                Component label = Component.literal(slotName(group.id) + "  "
                        + Component.translatable(KEY_HEADER_SLOTS, group.used(), group.slots).getString());
                guiGraphics.drawString(this.font, label, this.left + LIST_X + 2, y + 2, 0xFFFFFFFF);
            } else {
                ItemStack stack = groupAt(row).stacks.get(row.index);
                guiGraphics.renderItem(stack, this.left + LIST_X + 2, y);
                String name = stack.getHoverName().getString();
                int nameMax = right - 92 - (this.left + LIST_X + 22);
                if (font.width(name) > nameMax) {
                    name = font.plainSubstrByWidth(name, nameMax) + "...";
                }
                guiGraphics.drawString(this.font, name, this.left + LIST_X + 22, y + 4, 0xFFE0E0E0);
            }
            y += rowHeight(row);
        }
    }

    private void renderNbt(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.left + LIST_X - 2, this.top + LIST_Y - 2,
                this.left + LIST_X + LIST_W + 2, this.top + LIST_Y + LIST_H + 2, 0x88000000);
        int y = this.top + LIST_Y - scroll;
        for (int i = 0; i < nbtLines.size(); i++) {
            if (y < this.top + LIST_Y - 20 || y > this.top + LIST_Y + LIST_H + 20) {
                y += 10;
                continue;
            }
            guiGraphics.drawString(this.font, nbtLines.get(i), this.left + LIST_X + 2, y, 0xFF7FDBFF);
            y += 10;
        }
        guiGraphics.drawString(this.font, Component.translatable(KEY_NBT_HINT).getString(),
                this.left + 8, this.top + GUI_HEIGHT - 32, 0xFF7F7F7F);
    }

    private Group groupAt(Row row) {
        return row.group;
    }

    private static String slotName(String id) {
        return id;
    }

    // ------------------------------------------------------------------
    // 交互
    // ------------------------------------------------------------------

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int contentHeight;
        if (nbtMode) {
            contentHeight = nbtLines.size() * 10;
        } else {
            contentHeight = 0;
            for (Row row : rows) {
                contentHeight += rowHeight(row);
            }
        }
        maxScroll = Math.max(0, contentHeight - LIST_H);
        scroll = Math.max(0, Math.min(scroll - (int) Math.round(delta) * 14, maxScroll));
        if (!nbtMode) {
            rebuildRowWidgets();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}