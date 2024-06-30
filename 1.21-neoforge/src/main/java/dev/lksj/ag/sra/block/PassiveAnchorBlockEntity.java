package dev.lksj.ag.sra.block;

import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class PassiveAnchorBlockEntity extends AnchorBlockEntity {
    public PassiveAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(SRAMod.PASSIVE_ANCHOR_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    protected void doWork(ServerLevel world, BlockPos pos, AnchorBlockEntity theAnchor, boolean load) {
        PASSIVE_ANCHOR_CTRL.forceChunk(world, pos, pos.getX() >> 4, pos.getZ() >> 4, load, true);
    }
}
