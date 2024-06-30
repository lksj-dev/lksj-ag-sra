package dev.lksj.ag.sra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Locale;

public final class AnchorScreen extends AbstractContainerScreen<AnchorContainer> {

    private static final ResourceLocation GUI_BG = ResourceLocation.fromNamespaceAndPath(SRAMod.MOD_ID, "textures/gui/anchor.png");

    private Locale currentLocale = Locale.ROOT;

    public AnchorScreen(AnchorContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        // Set the title location; vanilla will render it accordingly
        this.titleLabelX = 10;
        this.titleLabelY = 10;
        Minecraft mc = this.minecraft;
        if (mc != null) {
            this.currentLocale = mc.getLanguageManager().getJavaLocale();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, Component.translatable("gui.reality_anchor.time_remain_1"), 52, 30, 0x404040, false);
        String timeRemain = String.format(this.currentLocale, "%.2f", this.menu.syncedTimer.timeRemain / 72000F);
        guiGraphics.drawString(this.font, Component.translatable("gui.reality_anchor.time_remain_2", timeRemain), 52, 40, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(GUI_BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

}