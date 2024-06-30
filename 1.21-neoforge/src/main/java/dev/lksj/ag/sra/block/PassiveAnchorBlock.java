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

public final class PassiveAnchorBlock extends AnchorBlock {

    public static final MapCodec<PassiveAnchorBlock> CODEC = simpleCodec(PassiveAnchorBlock::new);

    public PassiveAnchorBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    protected MapCodec<? extends AnchorBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PassiveAnchorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, SRAMod.STANDARD_ANCHOR_BLOCK_ENTITY_TYPE.get(), AnchorBlockEntity::serverTick);
    }
}
