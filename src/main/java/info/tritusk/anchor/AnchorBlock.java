package info.tritusk.anchor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AnchorBlockEntity(this.type);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!world.isRemote && this.type != AnchorType.ADMIN) {
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
        // Here is a tricky situation.
        // In AnchorBlockEntity, it always add its position to the list of persist 
        // anchors when being removed from the world. However, a block entity
        // is removed from the world not only because chunk unload but also 
        // beacuse the host block is replaced. So we need to call super first, 
        // and then let this method to remove the position from the list.
        // Yes it sounds reduntdant, but there is no good way around.
        super.onReplaced(state, world, pos, newState, isMoving);
        if (world instanceof ServerWorld) {
            // TODO what if newState is also an anchor?
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof AnchorBlockEntity) {
                ((AnchorBlockEntity)tile).doWork((ServerWorld) world, false);
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((AnchorBlockEntity)tile).inv.content);
            }
        }
    }
}