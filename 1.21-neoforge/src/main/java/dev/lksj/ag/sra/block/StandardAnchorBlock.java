package dev.lksj.ag.sra.block;

import com.mojang.serialization.MapCodec;
import dev.lksj.ag.sra.SRAMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class StandardAnchorBlock extends AnchorBlock {

    public static final MapCodec<StandardAnchorBlock> CODEC = simpleCodec(StandardAnchorBlock::new);

    public StandardAnchorBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    protected MapCodec<? extends AnchorBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StandardAnchorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, SRAMod.STANDARD_ANCHOR_BLOCK_ENTITY_TYPE.get(), AnchorBlockEntity::serverTick);
    }
}
