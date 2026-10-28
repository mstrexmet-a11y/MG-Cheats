
package ru.mgcheats;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class XraySettingsScreen extends Screen {
    private final Screen parent;
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 350;

    private static final String[] ORES = {
            "Coal",
            "Iron",
            "Copper",
            "Gold",
            "Redstone",
            "Lapis Lazuli",
            "Diamond",
            "Emerald"
    };

    public XraySettingsScreen(Screen parent) {
        super(Component.literal("X-Ray Settings"));
        this.parent = parent;
    }

    private boolean enabled(int i) {
        return switch (i) {
            case 0 -> XrayRenderer.coal;
            case 1 -> XrayRenderer.iron;
            case 2 -> XrayRenderer.copper;
            case 3 -> XrayRenderer.gold;
            case 4 -> XrayRenderer.redstone;
            case 5 -> XrayRenderer.lapis;
            case 6 -> XrayRenderer.diamond;
            case 7 -> XrayRenderer.emerald;
            default -> false;
        };
    }

    private void toggle(int i) {
        switch (i) {
            case 0 -> XrayRenderer.coal = !XrayRenderer.coal;
            case 1 -> XrayRenderer.iron = !XrayRenderer.iron;
            case 2 -> XrayRenderer.copper = !XrayRenderer.copper;
            case 3 -> XrayRenderer.gold = !XrayRenderer.gold;
            case 4 -> XrayRenderer.redstone = !XrayRenderer.redstone;
            case 5 -> XrayRenderer.lapis = !XrayRenderer.lapis;
            case 6 -> XrayRenderer.diamond = !XrayRenderer.diamond;
            case 7 -> XrayRenderer.emerald = !XrayRenderer.emerald;
        }
    }

    @Override
    public void render(
            GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);

        int x = (width - PANEL_W) / 2;
        int y = (height - PANEL_H) / 2;

        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0181820);
        g.fill(x, y, x + PANEL_W, y + 3, 0xFF9B59FF);

        g.drawString(font, "X-RAY SETTINGS",
                x + 14, y + 13, 0xFFFFFFFF, false);

        for (int i = 0; i < ORES.length; i++) {
            int rowY = y + 42 + i * 30;
            boolean on = enabled(i);

            g.fill(x + 10, rowY, x + PANEL_W - 10,
                    rowY + 24, 0xFF25232E);

            g.drawString(font, ORES[i], x + 18, rowY + 8,
                    0xFFFFFFFF, false);

            int color = on ? 0xFF9B59FF : 0xFF66616F;
            g.fill(x + PANEL_W - 54, rowY + 4,
                    x + PANEL_W - 18, rowY + 20, color);

            g.drawCenteredString(font, on ? "ON" : "OFF",
                    x + PANEL_W - 36, rowY + 8, 0xFFFFFFFF);
        }

        int backY = y + PANEL_H - 35;
        g.fill(x + 10, backY, x + PANEL_W - 10,
                backY + 24, 0xFF383342);
        g.drawCenteredString(font, "Back",
                x + PANEL_W / 2, backY + 8, 0xFFFFFFFF);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(
            double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - PANEL_W) / 2;
            int y = (height - PANEL_H) / 2;

            for (int i = 0; i < ORES.length; i++) {
                int rowY = y + 42 + i * 30;

                if (mouseX >= x + 10 && mouseX <= x + PANEL_W - 10
                        && mouseY >= rowY && mouseY <= rowY + 24) {
                    toggle(i);
                    return true;
                }
            }

            int backY = y + PANEL_H - 35;
            if (mouseX >= x + 10 && mouseX <= x + PANEL_W - 10
                    && mouseY >= backY && mouseY <= backY + 24) {
                minecraft.setScreen(parent);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
