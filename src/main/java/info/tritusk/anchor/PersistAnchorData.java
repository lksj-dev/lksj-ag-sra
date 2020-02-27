package info.tritusk.anchor;

import java.util.ArrayList;

import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

public final class PersistAnchorData extends WorldSavedData {

    ArrayList<BlockPos> persistAnchorPos = new ArrayList<BlockPos>();

    public PersistAnchorData() {
        super("PersistAnchorLocations");
    }

    @Override
    public void read(CompoundNBT data) {
        ListNBT list = data.getList("AnchorLocations", Constants.NBT.TAG_LIST);
        for (int i = 0; i < list.size(); i++) {
            this.persistAnchorPos.add(NBTUtil.readBlockPos(list.getCompound(i)));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        ListNBT list = new ListNBT();
        for (BlockPos pos : this.persistAnchorPos) {
            list.add(NBTUtil.writeBlockPos(pos));
        }
        data.put("AnchorLocations", list);
        return data;
    }
    
}