package info.tritusk.anchor;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.INameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

public final class AnchorBlockEntity extends TileEntity implements INameable, ITickableTileEntity {

    public static TileEntityType<AnchorBlockEntity> TYPE;
    /* Note: https://github.com/mekanism/Mekanism/blob/1.15x/src/main/java/mekanism/common/chunkloading/ChunkManager.java#L94-L104
     * Need investigation
     */
    public static final TicketType<ChunkPos> ANCHOR = TicketType.create("reality_anchor:anchor", Comparator.comparingLong(ChunkPos::asLong));

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
    
    UUID owner = new UUID(0L, 0L);
    ITextComponent title;

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
        if (!this.world.isRemote) {
            if (this.isWorking) {
                if (!this.type.perpetual && --this.timer.timeRemain <= 0) {
                    if (this.inv.content.getCount() > 0) {
                        this.inv.content.shrink(1);
                        this.timer.timeRemain += AnchorMod.defaultFuelValue.get();
                    } else {
                        this.isWorking = false;
                        doWork((ServerWorld)this.world, this.pos, false);
                    }
                }
            } else {
                if (this.type.perpetual || this.timer.timeRemain > 0) {
                    this.isWorking = true;
                    doWork((ServerWorld)this.world, this.pos, true);
                } else if (this.inv.content.getCount() > 0) {
                    if (this.type != AnchorType.PERSONAL || this.world.getServer().getPlayerList().getPlayerByUUID(this.owner) != null) {
                        this.inv.content.shrink(1);
                        this.timer.timeRemain += AnchorMod.defaultFuelValue.get();
                        this.isWorking = true;
                        doWork((ServerWorld)this.world, this.pos, true);
                    }
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
        data.putString("Type", this.type.name);
        data.putLong("TimeRemain", this.timer.timeRemain);
        data.put("Inv", this.inv.content.write(new CompoundNBT()));
        data.putUniqueId("Owner", this.owner);
        if (this.title != null) {
            data.putString("Title", ITextComponent.Serializer.toJson(this.title));
        }
        return super.write(data);
    }

    @Override
    public void read(CompoundNBT data) {
        super.read(data);
        this.type = AnchorType.find(data.getString("Type"));
        this.timer.timeRemain = data.getLong("TimeRemain");
        this.inv.content = ItemStack.read(data.getCompound("Inv"));
        this.owner = data.getUniqueId("Owner");
        if (data.contains("Title", Constants.NBT.TAG_STRING)) {
            this.title = ITextComponent.Serializer.fromJson(data.getString("Title"));
        }
    }

    @Override
    public ITextComponent getName() {
        return this.title == null ? AnchorContainer.Provider.TITLE : this.title;
    }

    @Override
    public ITextComponent getCustomName() {
        return this.title;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!this.type.perpetual && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.inv.view.cast();
        }
        return super.getCapability(cap, side);
    }
}
