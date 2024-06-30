package dev.lksj.ag.sra.block;

import dev.lksj.ag.sra.AnchorContainer;
import dev.lksj.ag.sra.AnchorInv;
import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Nameable;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.world.chunk.LoadingValidationCallback;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.UUID;

public class AnchorBlockEntity extends BlockEntity implements Nameable {

    /* Note: https://github.com/mekanism/Mekanism/blob/1.15.x/src/main/java/mekanism/common/chunkloading/ChunkManager.java#L94-L104
     * Need investigation
     */
    public static final TicketController ANCHOR_CTRL = new TicketController(ResourceLocation.fromNamespaceAndPath(SRAMod.MOD_ID, "anchor"), null);

    public static final TicketController PASSIVE_ANCHOR_CTRL = new TicketController(ResourceLocation.fromNamespaceAndPath(SRAMod.MOD_ID, "passive_anchor"), new LoadingValidationCallback() {
        @Override
        public void validateTickets(ServerLevel level, TicketHelper ticketHelper) {
            for (BlockPos pos : ticketHelper.getBlockTickets().keySet()) {
                ticketHelper.removeAllTickets(pos);
            }
            for (UUID uuid : ticketHelper.getEntityTickets().keySet()) {
                ticketHelper.removeAllTickets(uuid);
            }
        }
    });

    public static final DataMapType<Item, Integer> ANCHOR_FUEL = DataMapType.builder(ResourceLocation.fromNamespaceAndPath(SRAMod.MOD_ID, "anchor_fuel"),
            Registries.ITEM, ExtraCodecs.POSITIVE_INT).synced(ExtraCodecs.POSITIVE_INT, false).build();

    public static final class SyncedTime implements ContainerData {

        public long timeRemain;

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
        public int getCount() {
            return 4;
        }
        
    }
    private boolean isWorking;

    Component title;

    public final AnchorInv inv = new AnchorInv();
    public final SyncedTime timer = new SyncedTime();

    public AnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected boolean meetsWorkingCondition(Level level, BlockPos pos) {
        return true;
    }

    protected boolean tryConsumeFuel() {
        if (--this.timer.timeRemain <= 0) {
            Integer fuelValue = this.inv.content.getItemHolder().getData(ANCHOR_FUEL);
            if (fuelValue != null && this.inv.content.getCount() > 0) {
                this.inv.content.shrink(1);
                this.timer.timeRemain += fuelValue;
            } else {
                this.timer.timeRemain = 0;
                return false;
            }
        }
        return true;
    }
    /**
     * (Un-)Force-loading the predefined area.
     * @param world the world instance wherein this tile entity locates
     * @param load chunks will be forced if true; otherwise will be unforced
     */
    protected void doWork(ServerLevel world, BlockPos pos, AnchorBlockEntity theAnchor, boolean load) {
        ANCHOR_CTRL.forceChunk(world, pos, pos.getX() >> 4, pos.getZ() >> 4, load, true);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AnchorBlockEntity theAnchor) {
        if (theAnchor.isWorking) {
            if (!theAnchor.meetsWorkingCondition(level, pos) || !theAnchor.tryConsumeFuel()) {
                theAnchor.isWorking = false;
                theAnchor.doWork((ServerLevel) level, pos, theAnchor, false);
            }
            theAnchor.setChanged();
        } else if (theAnchor.meetsWorkingCondition(level, pos)) {
            if (theAnchor.timer.timeRemain > 0 || theAnchor.tryConsumeFuel()) {
                theAnchor.isWorking = true;
                theAnchor.doWork((ServerLevel) level, pos, theAnchor,true);
                theAnchor.setChanged();
            }
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.title = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.title);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider holderLookupProvider) {
        super.saveAdditional(data, holderLookupProvider);
        data.putLong("TimeRemain", this.timer.timeRemain);
        data.put("Inv", this.inv.content.saveOptional(holderLookupProvider));
        if (this.title != null) {
            data.putString("Title", Component.Serializer.toJson(this.title, holderLookupProvider));
        }
    }

    @Override
    public void loadAdditional(CompoundTag data, HolderLookup.Provider holderLookupProvider) {
        super.loadAdditional(data, holderLookupProvider);
        this.timer.timeRemain = data.getLong("TimeRemain");
        this.inv.content = ItemStack.parseOptional(holderLookupProvider, data.getCompound("Inv"));
        if (data.contains("Title", Tag.TAG_STRING)) {
            this.title = Component.Serializer.fromJson(data.getString("Title"), holderLookupProvider);
        }
    }

    @Override
    public Component getName() {
        return this.title == null ? AnchorContainer.Provider.TITLE : this.title;
    }

    @Override
    public Component getCustomName() {
        return this.title;
    }

    public static IItemHandler getItemHandlerCap(AnchorBlockEntity theBE, Direction direction) {
        return theBE.inv.view;
    }
}
