package dev.lksj.ag.sra.block;

import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public final class PersonalAnchorBlockEntity extends AnchorBlockEntity {

    private UUID owner = new UUID(0L, 0L);

    public PersonalAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(SRAMod.PERSONAL_ANCHOR_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    protected void doWork(ServerLevel world, BlockPos pos, AnchorBlockEntity theAnchor, boolean load) {
        PASSIVE_ANCHOR_CTRL.forceChunk(world, pos, pos.getX() >> 4, pos.getZ() >> 4, load, true);
    }

    @Override
    protected boolean meetsWorkingCondition(Level level, BlockPos pos) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }
        return server.getPlayerList().getPlayer(this.owner) != null;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider holderLookupProvider) {
        super.saveAdditional(data, holderLookupProvider);
        data.putUUID("owner", this.owner);
    }

    @Override
    public void loadAdditional(CompoundTag data, HolderLookup.Provider holderLookupProvider) {
        super.loadAdditional(data, holderLookupProvider);
        this.owner = data.getUUID("owner");
    }
}
