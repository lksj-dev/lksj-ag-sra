package info.tritusk.anchor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

public final class AnchorInv implements IItemHandlerModifiable {

    public static final Tag<Item> ANCHOR_FUEL_LIST = new ItemTags.Wrapper(new ResourceLocation("reality_anchor", "anchor_fuel"));

    ItemStack content = ItemStack.EMPTY;

    final LazyOptional<IItemHandler> view = LazyOptional.of(() -> new IItemHandler() {
        @Override
        public int getSlots() {
            return AnchorInv.this.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return AnchorInv.this.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return AnchorInv.this.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY; // No we don't support extraction.
        }

        @Override
        public int getSlotLimit(int slot) {
            return AnchorInv.this.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return AnchorInv.this.isItemValid(slot, stack);
        }
    });

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? content.copy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0 || !this.isItemValid(slot, stack)) {
            return stack;
        }

        if (this.content.isEmpty()) {
            if (!simulate) {
                this.content = stack;
            }
            return ItemStack.EMPTY;
        } else {
            if (ItemStack.areItemsEqual(this.content, stack)) {
                if (!simulate) {
                    this.content.grow(stack.getCount());
                }
                return ItemStack.EMPTY;
            } else {
                return stack;
            }
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            return (simulate ? this.content.copy() : this.content).split(amount);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return ANCHOR_FUEL_LIST.contains(stack.getItem());
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (this.isItemValid(slot, stack)) {
            this.content = stack;
        }
    }

}