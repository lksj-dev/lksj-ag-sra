package info.tritusk.anchor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class AnchorContainer extends Container {

    public static ContainerType<AnchorContainer> TYPE;

    public AnchorContainer(int id, IInventory playerInv) {
        this(TYPE, id, AnchorInv.MOCK, playerInv);
    }

    public AnchorContainer(ContainerType<?> type, int id, IItemHandler inv, IInventory playerInv) {
        super(type, id);
        // TODO Adjust x and y location
        this.addSlot(new SlotItemHandler(inv, 0, 60, 60));

        for (int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 36 + j * 18, 137 + i * 18));
            }
        }
   
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 36 + i * 18, 195));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

}