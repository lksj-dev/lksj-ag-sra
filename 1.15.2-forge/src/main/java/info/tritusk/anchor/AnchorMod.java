package info.tritusk.anchor;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@Mod("reality_anchor")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AnchorMod {

    /*
     * Collection of locations of known passive/personal anchors,
     * used for forcing releasing chunk tickets on sevrer shutting down.
     */
    public static HashSet<GlobalPos> transientAnchors = new HashSet<>();

    public static Block anchorStandard;
    public static Block anchorPersonal;
    public static Block anchorPassive;
    public static Block anchorAdmin;

    public static ForgeConfigSpec.LongValue defaultFuelValue;

    public AnchorMod() {
        MinecraftForge.EVENT_BUS.addListener(AnchorMod::cleanup);
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        configBuilder.comment("General options about the Reality Anchor mod").push("general");
        defaultFuelValue = configBuilder
            .comment("Number of ticks that one unit of fuel can keep anchor working")
            .translation("config.reality_anchor.fuel_value")
            .defineInRange("fuelValue", 12 * 60 * 60 * 20, 0, Long.MAX_VALUE);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, configBuilder.pop().build());
    }
    
    @SubscribeEvent
    public static void regBlock(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
            anchorStandard = new AnchorBlock(AnchorType.STANDARD, Block.Properties.create(Material.IRON, DyeColor.LIGHT_BLUE)
                    .hardnessAndResistance(5F, 50F)
                    .harvestLevel(1)
                    .harvestTool(ToolType.PICKAXE)    
                    .sound(SoundType.METAL)
            ).setRegistryName("reality_anchor", "standard_anchor"),
            anchorPersonal = new AnchorBlock(AnchorType.PERSONAL, Block.Properties.create(Material.IRON, DyeColor.LIGHT_BLUE)
                    .hardnessAndResistance(10F, 100F)
                    .harvestLevel(2)
                    .harvestTool(ToolType.PICKAXE)    
                    .sound(SoundType.METAL)
            ).setRegistryName("reality_anchor", "personal_anchor"),
            anchorPassive = new AnchorBlock(AnchorType.PASSIVE, Block.Properties.create(Material.IRON, DyeColor.LIGHT_BLUE)
                    .hardnessAndResistance(10F, 100F)
                    .harvestLevel(2)
                    .harvestTool(ToolType.PICKAXE)
                    .sound(SoundType.METAL)
            ).setRegistryName("reality_anchor", "passive_anchor"),
            anchorAdmin = new AnchorBlock(AnchorType.ADMIN, Block.Properties.create(Material.IRON, DyeColor.LIGHT_BLUE)
                    .hardnessAndResistance(20F, 200F)
                    .harvestLevel(3)
                    .harvestTool(ToolType.PICKAXE)
                    .sound(SoundType.METAL)
            ).setRegistryName("reality_anchor", "admin_anchor")
        );
    }

    @SubscribeEvent
    public static void regContainerType(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(
                (AnchorContainer.TYPE = new ContainerType<>(AnchorContainer::new)).setRegistryName("reality_anchor", "anchor")
        );
    }

    @SubscribeEvent
    public static void regItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
            new BlockItem(anchorStandard, new Item.Properties()
                    .rarity(Rarity.RARE)
                    .group(ItemGroup.REDSTONE)
            ).setRegistryName("reality_anchor", "standard_anchor"),
            new BlockItem(anchorPersonal, new Item.Properties()
                    .rarity(Rarity.RARE)
                    .group(ItemGroup.REDSTONE)
            ).setRegistryName("reality_anchor", "personal_anchor"),
            new BlockItem(anchorPassive, new Item.Properties()
                    .rarity(Rarity.RARE)
                    .group(ItemGroup.REDSTONE)
            ).setRegistryName("reality_anchor", "passive_anchor"),
            new BlockItem(anchorAdmin, new Item.Properties()
                    .rarity(Rarity.EPIC)
                    .group(ItemGroup.REDSTONE)
            ).setRegistryName("reality_anchor", "admin_anchor")
        );
    }

    @SubscribeEvent
    public static void regTile(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(
            (AnchorBlockEntity.TYPE = TileEntityType.Builder.create(AnchorBlockEntity::new, anchorStandard, anchorPersonal, anchorPassive, anchorAdmin).build(null)).setRegistryName("reality_anchor", "anchor")            
        );
    }

    private static void cleanup(FMLServerStoppingEvent event) {
        for (ServerWorld world : event.getServer().getWorlds()) {
            for (Iterator<GlobalPos> itr = transientAnchors.iterator(); itr.hasNext();) {
                GlobalPos pos = itr.next();
                if (pos.getDimension() == world.dimension.getType()) {
                    final ChunkPos chunkPos = new ChunkPos(pos.getPos());
                    world.getChunkProvider().releaseTicket(AnchorBlockEntity.ANCHOR, chunkPos, 4, chunkPos);
                }
            }
        }
        transientAnchors = new HashSet<>();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = "reality_anchor", value = Dist.CLIENT)
    public static final class ClientSetup {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            ScreenManager.registerFactory(AnchorContainer.TYPE, AnchorScreen::new);
        }
    }
}
