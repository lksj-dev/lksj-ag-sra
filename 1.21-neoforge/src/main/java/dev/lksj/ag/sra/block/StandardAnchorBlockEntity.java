package dev.lksj.ag.sra.block;

import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class StandardAnchorBlockEntity extends AnchorBlockEntity {
    public StandardAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(SRAMod.STANDARD_ANCHOR_BLOCK_ENTITY_TYPE.get(), pos, state);
    }
}
