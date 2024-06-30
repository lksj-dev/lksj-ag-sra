package dev.lksj.ag.sra.block;

import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class O5AnchorBlockEntity extends AnchorBlockEntity {
    public O5AnchorBlockEntity(BlockPos pos, BlockState state) {
        super(SRAMod.O5_ANCHOR_BLOCK_ENTITY_TYPE.get(), pos, state);
    }
}
