package info.tritusk.anchor;

import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.MinecraftForgeClient;

public final class AnchorScreen extends ContainerScreen<AnchorContainer> {

    private static final ResourceLocation GUI_BG = new ResourceLocation("reality_anchor", "textures/gui/anchor.png");

    public AnchorScreen(AnchorContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        this.font.drawString(this.getTitle().getFormattedText(), 10, 10, 0x404040);
        this.font.drawString(I18n.format("gui.reality_anchor.time_remain_1", ObjectArrays.EMPTY_ARRAY), 52, 30, 0x404040);
        this.font.drawString(I18n.format("gui.reality_anchor.time_remain_2", String.format(MinecraftForgeClient.getLocale(), "%.2f", this.container.syncedTimer.timeRemain / 72000F)), 52, 40, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_BG);
        this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

}