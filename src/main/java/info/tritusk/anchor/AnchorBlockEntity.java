package info.tritusk.anchor;

import java.util.Objects;

import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public final class AnchorBlockEntity extends TileEntity implements ITickable {

    public static TileEntityType<?> TYPE;

    public static final class Inv implements IItemHandler {

        static final Tag<Item> ANCHOR_FUEL_LIST = new ItemTags.Wrapper(new ResourceLocation("reality_anchor", "anchor_fuel"));

        ItemStack content = ItemStack.EMPTY;

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
                    content = stack;
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
            return ItemStack.EMPTY; // No we don't support extraction.
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return ANCHOR_FUEL_LIST.contains(stack.getItem());
        }

    }

    private AnchorType type;
    private Inv inv = new Inv();
    private LazyOptional<IItemHandler> invView = LazyOptional.of(() -> this.inv);
    private long timeRemain;
    private boolean isWorking;

    public AnchorBlockEntity() {
        super(Objects.requireNonNull(TYPE, "You forget to initialize field AnchorBlockEntity.TYPE"));
    }

    public AnchorBlockEntity(AnchorType type) {
        this();
        this.type = type;
    }

    /**
     * (Un-)Force-loading the predefined area.
     * @param world the world instance wherein this tile entity locates
     * @param load chunks will be forced if true; otherwise will be unforced
     */
    private void doWork(ServerWorld world, boolean load) {
        int centerX = this.pos.getX() >> 4;
        int centerZ = this.pos.getZ() >> 4;
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                world.forceChunk(centerX + xOffset, centerZ + zOffset, load);
            }
        }
    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
            if (this.type == AnchorType.ADMIN) {
                return;
            }
            if (this.isWorking) {
                if (--this.timeRemain <= 0) {
                    if (this.inv.content.getCount() > 0) {
                        this.inv.content.shrink(1);
                        this.timeRemain += 864000;
                    } else {
                        this.isWorking = false;
                        doWork((ServerWorld)this.world, false);
                    }
                }
            } else {
                if (this.inv.content.getCount() > 0) {
                    this.inv.content.shrink(1);
                    this.timeRemain += 864000;
                    this.isWorking = true;
                    this.doWork((ServerWorld)this.world, true);
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.isWorking && !this.world.isRemote) {
            this.doWork((ServerWorld)this.world, true);
        }
    }

    @Override
    public void remove() {
        if (this.type != AnchorType.PASSIVE && !this.world.isRemote) {
            PersistAnchorData data = ((ServerWorld) this.world).getSavedData().getOrCreate(PersistAnchorData::new, "PersistAnchorLocations");
            data.persistAnchorPos.add(this.pos);
        }
        super.remove();
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        data.putInt("Type", this.type.ordinal());
        data.putLong("TimeRemain", this.timeRemain);
        data.putBoolean("Working", this.isWorking);
        data.put("Inv", this.inv.content.write(new CompoundNBT()));
        return super.write(data);
    }

    @Override
    public void read(CompoundNBT data) {
        super.read(data);
        int ordinal = data.getInt("Type");
        if (ordinal < 0 || ordinal > 3) ordinal = 0;
        this.type = AnchorType.values()[ordinal];
        this.timeRemain = data.getLong("TimeRemain");
        this.isWorking = data.getBoolean("Working");
        this.inv.content = ItemStack.read(data.getCompound("Inv"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.invView.cast();
        }
        return super.getCapability(cap, side);
    }
}