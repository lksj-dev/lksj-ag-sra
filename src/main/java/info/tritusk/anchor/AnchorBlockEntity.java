package info.tritusk.anchor;

import java.util.Comparator;
import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public final class AnchorBlockEntity extends TileEntity implements ITickableTileEntity {

    public static TileEntityType<?> TYPE;

    public static final TicketType<ChunkPos> ANCHOR = TicketType.create("reality_anchor", Comparator.comparingLong(ChunkPos::asLong));

    static final class SyncedTime implements IIntArray {

        long timeRemain;

        @Override
        public int get(int index) {
            return (int) (timeRemain >>> (index << 3)); // index << 3 == index * 8
        }

        @Override
        public void set(int index, int value) {
            this.timeRemain &= (0xFFFF_FFFF_FFFF_FFFFL ^ (0xFFFFL << (index << 3)));
            this.timeRemain |= (value << (index << 3)); // same above
        }

        @Override
        public int size() {
            return 4;
        }
        
    }

    private AnchorType type = AnchorType.STANDARD; // As a safe-guard measure, preventing null
    private boolean isWorking;

    final AnchorInv inv = new AnchorInv();
    final SyncedTime timer = new SyncedTime();

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
    static void doWork(ServerWorld world, BlockPos pos, boolean load) {
        if (load) {
            world.getChunkProvider().registerTicket(ANCHOR, new ChunkPos(pos), 4, new ChunkPos(pos));
        } else {
            world.getChunkProvider().releaseTicket(ANCHOR, new ChunkPos(pos), 4, new ChunkPos(pos));
        }
    }

    @Override
    public void tick() {
        if (this.type == AnchorType.ADMIN) {
            if (!this.isWorking) {
                this.isWorking = true;
                doWork((ServerWorld)this.world, this.pos, true);
            }
            return;
        }
        if (!this.world.isRemote) {
            if (this.isWorking) {
                if (--this.timer.timeRemain <= 0) {
                    if (this.inv.content.getCount() > 0) {
                        this.inv.content.shrink(1);
                        this.timer.timeRemain += 864000;
                    } else {
                        this.isWorking = false;
                        doWork((ServerWorld)this.world, this.pos, false);
                    }
                }
            } else {
                if (this.timer.timeRemain > 0) {
                    this.isWorking = true;
                    doWork((ServerWorld)this.world, this.pos, true);
                } else if (this.inv.content.getCount() > 0) {
                    this.inv.content.shrink(1);
                    this.timer.timeRemain += 864000;
                    this.isWorking = true;
                    doWork((ServerWorld)this.world, this.pos, true);
                }
            }
        }
    }

    @Override
    public synchronized void onLoad() {
        super.onLoad();
        if (this.type.passive) {
            AnchorMod.transientAnchors.add(GlobalPos.of(this.world.dimension.getType(), this.pos));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        data.putInt("Type", this.type.ordinal());
        data.putLong("TimeRemain", this.timer.timeRemain);
        data.put("Inv", this.inv.content.write(new CompoundNBT()));
        return super.write(data);
    }

    @Override
    public void read(CompoundNBT data) {
        super.read(data);
        int ordinal = data.getInt("Type");
        if (ordinal < 0 || ordinal > 3) ordinal = 0;
        this.type = AnchorType.values()[ordinal];
        this.timer.timeRemain = data.getLong("TimeRemain");
        this.inv.content = ItemStack.read(data.getCompound("Inv"));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (this.type != AnchorType.ADMIN && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.inv.view.cast();
        }
        return super.getCapability(cap, side);
    }
}