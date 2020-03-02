package info.tritusk.anchor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
        return new AnchorBlockEntity(AnchorType.STANDARD);
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
            ServerWorld serverWorld = (ServerWorld) world;
            serverWorld.forceChunk(pos.getX() >> 4, pos.getZ() >> 4, false);
            PersistAnchorData.readFrom(serverWorld).persistAnchorPos.remove(pos);
        }
    }
}