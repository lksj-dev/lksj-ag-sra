package info.tritusk.anchor;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "reality_anchor")
public final class PersistAnchorData extends WorldSavedData {

    static PersistAnchorData readFrom(ServerWorld world) {
        return world.getSavedData().getOrCreate(PersistAnchorData::new, "PersistAnchorLocations");
    }

    ObjectArraySet<BlockPos> persistAnchorPos = new ObjectArraySet<BlockPos>();

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
    
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        IWorld world = event.getWorld();
        if (world instanceof ServerWorld) {
            PersistAnchorData data = readFrom((ServerWorld) world);
            for (BlockPos pos : data.persistAnchorPos) {
                ((ServerWorld) world).forceChunk(pos.getX() >> 4, pos.getZ() >> 4, true);
            }
        }
    }

}