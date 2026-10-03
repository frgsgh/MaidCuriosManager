package com.maidcurios.network;

import com.maidcurios.MaidCuriosConfig;
import com.maidcurios.MaidTypeHelper;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * 客户端 → 服务端：对某只女仆的 Curios 数据做一次修改。
 *
 * <p>动作类型：
 * <ul>
 *   <li>GROW_SLOT：增加一类槽位数量（上限见配置 maxSlotsPerType）</li>
 *   <li>SHRINK_SLOT：减少一类槽位数量（仅当末尾槽为空时允许，避免饰品丢失）</li>
 *   <li>SET_COUNT：设置某个槽位中饰品的堆叠数量</li>
 * </ul>
 */
public class CurioEditMessage {
    public enum Action {
        GROW_SLOT,
        SHRINK_SLOT,
        SET_COUNT
    }

    private final int entityId;
    private final String slotId;
    private final int slotIndex;
    private final Action action;
    private final int value;

    public CurioEditMessage(int entityId, String slotId, int slotIndex, Action action, int value) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.slotIndex = slotIndex;
        this.action = action;
        this.value = value;
    }

    public CurioEditMessage(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.slotId = buf.readUtf(64);
        this.slotIndex = buf.readInt();
        this.action = buf.readEnum(Action.class);
        this.value = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeUtf(slotId);
        buf.writeInt(slotIndex);
        buf.writeEnum(action);
        buf.writeInt(value);
    }

    public static CurioEditMessage decode(FriendlyByteBuf buf) {
        return new CurioEditMessage(buf);
    }

    public static void handle(CurioEditMessage msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ServerLevel level = player.serverLevel();
            Entity entity = level.getEntity(msg.entityId);
            if (!MaidTypeHelper.isMaid(entity) || !(entity instanceof LivingEntity living)) {
                return;
            }
            CuriosApi.getCuriosInventory(living).ifPresent(handler -> msg.apply(handler));
        });
        ctx.setPacketHandled(true);
    }

    private void apply(ICuriosItemHandler handler) {
        switch (action) {
            case GROW_SLOT -> handler.getStacksHandler(slotId).ifPresent(sh -> {
                if (sh.getStacks().getSlots() < MaidCuriosConfig.MAX_SLOTS_PER_TYPE.get()) {
                    handler.growSlotType(slotId, 1);
                }
            });
            case SHRINK_SLOT -> handler.getStacksHandler(slotId).ifPresent(sh -> {
                IDynamicStackHandler stacks = sh.getStacks();
                int size = stacks.getSlots();
                if (size <= 1) {
                    return;
                }
                // 末尾槽还有饰品时不允许收缩，防止饰品被 Curios 丢弃
                if (!stacks.getStackInSlot(size - 1).isEmpty()) {
                    return;
                }
                handler.shrinkSlotType(slotId, 1);
            });
            case SET_COUNT -> handler.getStacksHandler(slotId).ifPresent(sh -> {
                IDynamicStackHandler stacks = sh.getStacks();
                if (slotIndex < 0 || slotIndex >= stacks.getSlots()) {
                    return;
                }
                ItemStack stack = stacks.getStackInSlot(slotIndex);
                if (stack.isEmpty()) {
                    return;
                }
                int max = Math.max(1, Math.min(stack.getMaxStackSize(), MaidCuriosConfig.MAX_STACK_COUNT.get()));
                int count = Math.max(1, Math.min(value, max));
                if (count == stack.getCount()) {
                    return;
                }
                ItemStack copy = stack.copy();
                copy.setCount(count);
                stacks.setStackInSlot(slotIndex, copy);
            });
            default -> {
                // no-op
            }
        }
    }
}