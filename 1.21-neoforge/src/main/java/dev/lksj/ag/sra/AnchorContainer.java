package dev.lksj.ag.sra;

import dev.lksj.ag.sra.block.AnchorBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AnchorContainer extends AbstractContainerMenu {


    public static final class Provider implements MenuProvider {

        public static final Component TITLE = Component.translatable("gui.reality_anchor.title", ObjectArrays.EMPTY_ARRAY);

        private final AnchorBlockEntity tile;

        public Provider(AnchorBlockEntity tile) {
            this.tile = tile;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
            return new AnchorContainer(SRAMod.ANCHOR_MENU_TYPE.get(), id, this.tile.inv, playerInv, this.tile.timer);
        }

        @Override
        public Component getDisplayName() {
            return this.tile.getDisplayName();
        }
        
    }

    public final AnchorBlockEntity.SyncedTime syncedTimer;

    public AnchorContainer(int id, Inventory playerInv) {
        this(SRAMod.ANCHOR_MENU_TYPE.get(), id, new AnchorInv(), playerInv, new AnchorBlockEntity.SyncedTime());
    }

    public AnchorContainer(MenuType<?> type, int id, IItemHandler inv, Inventory playerInv, AnchorBlockEntity.SyncedTime syncedTimer) {
        super(type, id);
        this.addSlot(new SlotItemHandler(inv, 0, 26, 30));
        for (int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
        this.addDataSlots(this.syncedTimer = syncedTimer);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack toReturn = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotContent = slot.getItem();
            toReturn = slotContent.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(slotContent, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else /*if (AnchorInv.ANCHOR_FUEL_LIST.contains(slotContent.getItem()))*/ {
                if (!this.moveItemStackTo(slotContent, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotContent.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return toReturn;
    }

}