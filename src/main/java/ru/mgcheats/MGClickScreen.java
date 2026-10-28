
package ru.mgcheats;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MGClickScreen extends Screen {
    private static final int PANEL_W = 300;
    private static final int PANEL_H = 390;

    private static final String[] NAMES = {
            "Auto Sprint",
            "Night Vision",
            "Auto Aim",
            "Kill Aura",
            "FPS HUD",
            "Coordinates HUD",
            "Keystrokes",
            "Reach Display",
            "X-Ray"
    };

    public MGClickScreen() {
        super(Component.literal("MG Cheats"));
    }

    private boolean enabled(int i) {
        return switch (i) {
            case 0 -> ClientFeatures.autoSprint;
            case 1 -> ClientFeatures.nightVision;
            case 2 -> ClientFeatures.autoAim;
            case 3 -> ClientFeatures.killAura;
            case 4 -> ClientFeatures.fpsHud;
            case 5 -> ClientFeatures.coordsHud;
            case 6 -> ClientFeatures.keystrokes;
            case 7 -> ClientFeatures.reachDisplay;
            case 8 -> XrayRenderer.enabled;
            default -> false;
        };
    }

    private void toggle(int i) {
        switch (i) {
            case 0 -> ClientFeatures.autoSprint =
                    !ClientFeatures.autoSprint;
            case 1 -> ClientFeatures.nightVision =
                    !ClientFeatures.nightVision;
            case 2 -> ClientFeatures.autoAim =
                    !ClientFeatures.autoAim;
            case 3 -> ClientFeatures.killAura =
                    !ClientFeatures.killAura;
            case 4 -> ClientFeatures.fpsHud =
                    !ClientFeatures.fpsHud;
            case 5 -> ClientFeatures.coordsHud =
                    !ClientFeatures.coordsHud;
            case 6 -> ClientFeatures.keystrokes =
                    !ClientFeatures.keystrokes;
            case 7 -> ClientFeatures.reachDisplay =
                    !ClientFeatures.reachDisplay;
            case 8 -> XrayRenderer.enabled =
                    !XrayRenderer.enabled;
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

        g.drawString(font, "MG CHEATS", x + 14, y + 12,
                0xFFFFFFFF, false);
        g.drawString(font, "1.20.1 | FORGE", x + 14, y + 27,
                0xFFBBA6D9, false);

        for (int i = 0; i < NAMES.length; i++) {
            int rowY = y + 48 + i * 31;
            boolean on = enabled(i);

            g.fill(x + 10, rowY, x + PANEL_W - 10,
                    rowY + 25, 0xFF25232E);

            g.drawString(font, NAMES[i], x + 18, rowY + 8,
                    0xFFFFFFFF, false);

            int color = on ? 0xFF9B59FF : 0xFF66616F;
            g.fill(x + PANEL_W - 54, rowY + 5,
                    x + PANEL_W - 18, rowY + 20, color);

            g.drawCenteredString(font, on ? "ON" : "OFF",
                    x + PANEL_W - 36, rowY + 9, 0xFFFFFFFF);

            // Кнопка настроек X-Ray
            if (i == 8) {
                g.drawString(font, "Settings",
                        x + 155, rowY + 8, 0xFFBBA6D9, false);
            }
        }

        g.drawString(font, "Click a row to toggle",
                x + 14, y + PANEL_H - 28, 0xFFBBA6D9, false);
        g.drawString(font, "Right Shift / ESC: close",
                x + 14, y + PANEL_H - 15, 0xFF8B8795, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(
            double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - PANEL_W) / 2;
            int y = (height - PANEL_H) / 2;

            for (int i = 0; i < NAMES.length; i++) {
                int rowY = y + 48 + i * 31;

                if (mouseX >= x + 10 && mouseX <= x + PANEL_W - 10
                        && mouseY >= rowY && mouseY <= rowY + 25) {

                    // Открыть настройки X-Ray по нажатию Settings
                    if (i == 8 && mouseX >= x + 145
                            && mouseX <= x + 220) {
                        MCOpenSettings();
                        return true;
                    }

                    toggle(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void MCOpenSettings() {
        minecraft.setScreen(new XraySettingsScreen(this));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
