package io.hailpurge.client.biosphere;

import io.hailpurge.biosphere.anchor.BiosphereAnchorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BiosphereAnchorScreen extends AbstractContainerScreen<BiosphereAnchorMenu> {
    public BiosphereAnchorScreen(BiosphereAnchorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 230;
        imageHeight = 155;
        titleLabelX = 16;
        titleLabelY = 14;
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF0111D22);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xF01A3035);
        graphics.fill(x + 12, y + 34, x + imageWidth - 12, y + 35, 0xFF3DE7D0);
        bar(graphics, x + 16, y + 65, menu.energy(), menu.capacity(), 0xFF28C5E8);
        bar(graphics, x + 16, y + 103, menu.condition(), 100, 0xFF57D68D);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE5FFFF, false);
        graphics.drawString(font, Component.translatable("screen.hailpurge.anchor.status", menu.status().name()), 16, 43, 0xFFB8E7E1, false);
        graphics.drawString(font, Component.translatable("screen.hailpurge.anchor.energy", menu.energy(), menu.capacity()), 16, 53, 0xFFE5FFFF, false);
        graphics.drawString(font, Component.translatable("screen.hailpurge.anchor.condition", menu.condition(), menu.conditionBand().name()), 16, 91, 0xFFE5FFFF, false);
        graphics.drawString(font, Component.translatable("screen.hailpurge.anchor.radius", menu.radius()), 16, 120, 0xFFB8E7E1, false);
        graphics.drawString(font, Component.translatable("screen.hailpurge.anchor.hint"), 16, 137, 0xFF749A9B, false);
    }

    private static void bar(GuiGraphics graphics, int x, int y, int value, int maximum, int color) {
        graphics.fill(x, y, x + 198, y + 8, 0xFF081114);
        int width = maximum == 0 ? 0 : Math.round(198.0F * value / maximum);
        graphics.fill(x, y, x + width, y + 8, color);
    }
}
