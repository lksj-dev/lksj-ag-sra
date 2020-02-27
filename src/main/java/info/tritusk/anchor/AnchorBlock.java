package info.tritusk.anchor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public final class AnchorBlock extends Block {

    public final AnchorType type;

    public AnchorBlock(AnchorType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AnchorBlockEntity();
    }

}