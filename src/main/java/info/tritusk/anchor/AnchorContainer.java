package info.tritusk.anchor;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class AnchorContainer extends Container {

    public static ContainerType<AnchorContainer> TYPE;

    public static final class Provider implements INamedContainerProvider {

        private static final ITextComponent TITLE = new TranslationTextComponent("gui.reality_anchor.title", ObjectArrays.EMPTY_ARRAY);

        private final IItemHandler inv;

        public Provider(IItemHandler inv) {
            this.inv = inv;
        }

        @Override
        public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player) {
            return new AnchorContainer(TYPE, id, this.inv, playerInv);
        }

        @Override
        public ITextComponent getDisplayName() {
            return TITLE;
        }
        
    }

    public AnchorContainer(int id, IInventory playerInv) {
        this(TYPE, id, AnchorInv.MOCK, playerInv);
    }

    public AnchorContainer(ContainerType<?> type, int id, IItemHandler inv, IInventory playerInv) {
        super(type, id);
        this.addSlot(new SlotItemHandler(inv, 0, 62, 30));
        for (int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

}