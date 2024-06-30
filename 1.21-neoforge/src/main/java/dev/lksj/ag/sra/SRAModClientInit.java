package dev.lksj.ag.sra;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(SRAMod.MOD_ID)
public class SRAModClientInit {

    public SRAModClientInit(IEventBus bus) {
        bus.addListener(RegisterMenuScreensEvent.class, SRAModClientInit::setup);
    }

    public static void setup(RegisterMenuScreensEvent event) {
        event.register(SRAMod.ANCHOR_MENU_TYPE.get(), AnchorScreen::new);
    }
}
