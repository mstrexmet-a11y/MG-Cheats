package ru.mgcheats;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MGClickScreen extends Screen {
    private static final int PANEL_W = 250;
    private static final int PANEL_H = 210;

    public MGClickScreen() {
        super(Component.literal("MG Cheats"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        int x = (this.width - PANEL_W) / 2;
        int y = (this.height - PANEL_H) / 2;

        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xF0181820);
        g.fill(x, y, x + PANEL_W, y + 3, 0xFF9B59FF);
        g.drawString(this.font, "MG CHEATS", x + 14, y + 12, 0xFFFFFFFF, false);
        g.drawString(this.font, "1.20.1  |  FORGE", x + 14, y + 27, 0xFFBBA6D9, false);

        drawToggle(g, x, y + 52, "AutoSprint", ClientFeatures.autoSprint, 0);
        drawToggle(g, x, y + 79, "Fullbright", ClientFeatures.fullbright, 1);
        drawToggle(g, x, y + 106, "KillAura", ClientFeatures.killAura, 2);
        drawToggle(g, x, y + 133, "Reach Display", ClientFeatures.reachDisplay, 3);

        g.drawString(this.font, "Click a row to toggle", x + 14, y + 178, 0xFFBBA6D9, false);
        g.drawString(this.font, "Right Shift / ESC: close", x + 14, y + 192, 0xFF8B8795, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawToggle(GuiGraphics g, int x, int y, String name, boolean enabled, int id) {
        g.fill(x + 10, y - 4, x + PANEL_W - 10, y + 18, 0xFF25232E);
        g.drawString(this.font, name, x + 18, y + 3, 0xFFFFFFFF, false);
        int color = enabled ? 0xFF9B59FF : 0xFF66616F;
        g.fill(x + PANEL_W - 48, y + 2, x + PANEL_W - 20, y + 13, color);
        g.drawString(this.font, enabled ? "ON" : "OFF", x + PANEL_W - 45, y + 3, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (this.width - PANEL_W) / 2;
            int y = (this.height - PANEL_H) / 2;
            int row = (int) ((mouseY - (y + 48)) / 27);
            if (mouseX >= x && mouseX <= x + PANEL_W && row >= 0 && row <= 3) {
                switch (row) {
                    case 0 -> ClientFeatures.autoSprint = !ClientFeatures.autoSprint;
                    case 1 -> ClientFeatures.fullbright = !ClientFeatures.fullbright;
                    case 2 -> ClientFeatures.killAura = !ClientFeatures.killAura;
                    case 3 -> ClientFeatures.reachDisplay = !ClientFeatures.reachDisplay;
                }
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
