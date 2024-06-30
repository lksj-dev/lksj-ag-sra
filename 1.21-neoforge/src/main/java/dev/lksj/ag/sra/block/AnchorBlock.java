package dev.lksj.ag.sra.block;

import com.mojang.serialization.MapCodec;
import dev.lksj.ag.sra.AnchorContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class AnchorBlock extends BaseEntityBlock {
    private final boolean hasGui;

    public AnchorBlock(boolean hasGui, Properties properties) {
        super(properties);
        this.hasGui = hasGui;
    }

    @Override
    protected abstract MapCodec<? extends AnchorBlock> codec();

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);

    @Override
    protected RenderShape getRenderShape(BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult rayTrace) {
        if (!level.isClientSide && this.hasGui) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AnchorBlockEntity) {
                player.openMenu(new AnchorContainer.Provider((AnchorBlockEntity)tile));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("deprecation")
    @Override // Oh thanks Androsa reminding me this
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (level instanceof ServerLevel) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof AnchorBlockEntity theAnchor) {
                theAnchor.doWork((ServerLevel) level, pos, theAnchor, false);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), theAnchor.inv.content);
            }
        }
    }

}