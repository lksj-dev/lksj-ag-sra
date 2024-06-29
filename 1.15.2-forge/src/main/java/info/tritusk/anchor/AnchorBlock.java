package info.tritusk.anchor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public final class AnchorBlock extends Block {

    public final AnchorType type;

    public AnchorBlock(AnchorType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AnchorBlockEntity(this.type);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof AnchorBlockEntity) {
                if (placer instanceof PlayerEntity) {
                    ((AnchorBlockEntity)tile).owner = ((PlayerEntity)placer).getUniqueID();
                    if (stack.hasDisplayName()) {
                        ((AnchorBlockEntity)tile).title = stack.getDisplayName();
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote && !this.type.perpetual) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof AnchorBlockEntity) {
                player.openContainer(new AnchorContainer.Provider((AnchorBlockEntity)tile));
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override // Oh thanks Androsa reminding me this
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onReplaced(state, world, pos, newState, isMoving);
        if (world instanceof ServerWorld) {
            if (this.type.passive) {
                AnchorMod.transientAnchors.remove(GlobalPos.of(world.getDimension().getType(), pos));
            }
            AnchorBlockEntity.doWork((ServerWorld) world, pos, false);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof AnchorBlockEntity) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((AnchorBlockEntity)tile).inv.content);
            }
        }
    }
}