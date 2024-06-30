package dev.lksj.ag.sra;

import com.mojang.datafixers.DSL;
import dev.lksj.ag.sra.block.AnchorBlock;
import dev.lksj.ag.sra.block.AnchorBlockEntity;
import dev.lksj.ag.sra.block.O5AnchorBlock;
import dev.lksj.ag.sra.block.O5AnchorBlockEntity;
import dev.lksj.ag.sra.block.PassiveAnchorBlock;
import dev.lksj.ag.sra.block.PassiveAnchorBlockEntity;
import dev.lksj.ag.sra.block.PersonalAnchorBlock;
import dev.lksj.ag.sra.block.PersonalAnchorBlockEntity;
import dev.lksj.ag.sra.block.StandardAnchorBlock;
import dev.lksj.ag.sra.block.StandardAnchorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@Mod(SRAMod.MOD_ID)
public final class SRAMod {

    public static final String MOD_ID = "scranton_reality_anchor";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.Blocks.createBlocks(MOD_ID);
    public static final DeferredHolder<Block, AnchorBlock> STANDARD_ANCHOR = BLOCKS.register("standard_anchor",
            () -> new StandardAnchorBlock(Block.Properties.of()
                    .mapColor(DyeColor.LIGHT_BLUE)
                    .strength(5F, 50F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));
    public static final DeferredHolder<Block, AnchorBlock> PERSONAL_ANCHOR = BLOCKS.register("personal_anchor",
            () -> new PersonalAnchorBlock(Block.Properties.of()
                    .mapColor(DyeColor.LIGHT_BLUE)
                    .strength(10F, 50F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));
    public static final DeferredHolder<Block, AnchorBlock> PASSIVE_ANCHOR = BLOCKS.register("passive_anchor",
            () -> new PassiveAnchorBlock(Block.Properties.of()
                    .mapColor(DyeColor.LIGHT_BLUE)
                    .strength(10F, 50F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));
    public static final DeferredHolder<Block, AnchorBlock> O5_ANCHOR = BLOCKS.register("o5_anchor",
            () -> new O5AnchorBlock(Block.Properties.of()
                    .mapColor(DyeColor.LIGHT_BLUE)
                    .strength(-1F, Float.MAX_VALUE)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)));
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(MOD_ID);
    public static final DeferredHolder<Item, BlockItem> STANDARD_ANCHOR_ITEM = ITEMS.register("standard_anchor",
            () -> new BlockItem(STANDARD_ANCHOR.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, BlockItem> PERSONAL_ANCHOR_ITEM = ITEMS.register("personal_anchor",
            () -> new BlockItem(PERSONAL_ANCHOR.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, BlockItem> PASSIVE_ANCHOR_ITEM = ITEMS.register("passive_anchor",
            () -> new BlockItem(PASSIVE_ANCHOR.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredHolder<Item, BlockItem> O5_ANCHOR_ITEM = ITEMS.register("o5_anchor",
            () -> new BlockItem(O5_ANCHOR.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StandardAnchorBlockEntity>> STANDARD_ANCHOR_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("standard_anchor",
            () -> BlockEntityType.Builder.of(StandardAnchorBlockEntity::new, STANDARD_ANCHOR.get()).build(DSL.remainderType()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PersonalAnchorBlockEntity>> PERSONAL_ANCHOR_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("personal_anchor",
            () -> BlockEntityType.Builder.of(PersonalAnchorBlockEntity::new, PERSONAL_ANCHOR.get()).build(DSL.remainderType()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PassiveAnchorBlockEntity>> PASSIVE_ANCHOR_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("passive_anchor",
            () -> BlockEntityType.Builder.of(PassiveAnchorBlockEntity::new, PASSIVE_ANCHOR.get()).build(DSL.remainderType()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<O5AnchorBlockEntity>> O5_ANCHOR_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("o5_anchor",
            () -> BlockEntityType.Builder.of(O5AnchorBlockEntity::new, O5_ANCHOR.get()).build(DSL.remainderType()));

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<AnchorContainer>> ANCHOR_MENU_TYPE = MENU_TYPES.register("anchor",
            () -> new MenuType<>(AnchorContainer::new, FeatureFlagSet.of()));

    public SRAMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        MENU_TYPES.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, SRAMod::capRegister);
        bus.addListener(RegisterTicketControllersEvent.class, SRAMod::ticketCtrlRegister);
        bus.addListener(RegisterDataMapTypesEvent.class, SRAMod::dataMapRegister);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, SRAMod::buildCreativeTab);
    }

    private static void capRegister(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STANDARD_ANCHOR_BLOCK_ENTITY_TYPE.get(), AnchorBlockEntity::getItemHandlerCap);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PERSONAL_ANCHOR_BLOCK_ENTITY_TYPE.get(), AnchorBlockEntity::getItemHandlerCap);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PASSIVE_ANCHOR_BLOCK_ENTITY_TYPE.get(), AnchorBlockEntity::getItemHandlerCap);
    }

    private static void ticketCtrlRegister(RegisterTicketControllersEvent event) {
        event.register(AnchorBlockEntity.ANCHOR_CTRL);
        event.register(AnchorBlockEntity.PASSIVE_ANCHOR_CTRL);
    }

    private static void dataMapRegister(RegisterDataMapTypesEvent event) {
        event.register(AnchorBlockEntity.ANCHOR_FUEL);
    }

    private static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(STANDARD_ANCHOR_ITEM.get());
            event.accept(PERSONAL_ANCHOR_ITEM.get());
            event.accept(PASSIVE_ANCHOR_ITEM.get());
            event.accept(O5_ANCHOR_ITEM.get());
        }
    }

}
