
package ru.mgcheats;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class MGClickScreen extends Screen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 25;
    private static final int ROW_GAP = 5;
    private static final int FIRST_ROW_Y = 48;

    private int scroll = 0;
    private int panelX;
    private int panelY;
    private int panelH;
    private int rowGap;
    private int visibleRows;
    private int listTop;
    private int listBottom;

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
            case 0 -> ClientFeatures.autoSprint = !ClientFeatures.autoSprint;
            case 1 -> ClientFeatures.nightVision = !ClientFeatures.nightVision;
            case 2 -> ClientFeatures.autoAim = !ClientFeatures.autoAim;
            case 3 -> ClientFeatures.killAura = !ClientFeatures.killAura;
            case 4 -> ClientFeatures.fpsHud = !ClientFeatures.fpsHud;
            case 5 -> ClientFeatures.coordsHud = !ClientFeatures.coordsHud;
            case 6 -> ClientFeatures.keystrokes = !ClientFeatures.keystrokes;
            case 7 -> ClientFeatures.reachDisplay = !ClientFeatures.reachDisplay;
            case 8 -> XrayRenderer.enabled = !XrayRenderer.enabled;
        }
    }

    private int rowY(int i) {
        return listTop + i * (ROW_H + rowGap) - scroll;
    }

    private int maxScroll() {
        int contentHeight = NAMES.length * (ROW_H + rowGap) - rowGap;
        return Math.max(0, contentHeight - (listBottom - listTop));
    }

    @Override
    public void render(
            GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        panelH = Math.min(430, height - 12);
        panelH = Math.max(100, panelH);

        panelX = (width - PANEL_W) / 2;
        panelY = (height - panelH) / 2;

        int availableListHeight = panelH - 112;
        rowGap = height < 400 ? 2 : ROW_GAP;
        listTop = panelY + FIRST_ROW_Y;
        listBottom = panelY + panelH - 54;

        if (availableListHeight < 0) {
            listBottom = listTop;
        }

        visibleRows = Math.max(
            1,
            (listBottom - listTop + rowGap) / (ROW_H + rowGap)
        );

        scroll = Math.max(0, Math.min(scroll, maxScroll()));

        g.fill(panelX, panelY, panelX + PANEL_W, panelY + panelH,
            0xF0181820);
        g.fill(panelX, panelY, panelX + PANEL_W, panelY + 3,
            0xFF9B59FF);

        g.drawString(font, "MG CHEATS", panelX + 14, panelY + 10,
            0xFFFFFFFF, false);
        g.drawString(font, "1.20.1 | FORGE", panelX + 14, panelY + 25,
            0xFFBBA6D9, false);

        g.enableScissor(
            panelX + 8, listTop, panelX + PANEL_W - 8, listBottom
        );

        for (int i = 0; i < NAMES.length; i++) {
            int ry = rowY(i);

            if (ry + ROW_H < listTop || ry > listBottom) {
                continue;
            }

            boolean on = enabled(i);

            g.fill(panelX + 10, ry, panelX + PANEL_W - 10,
                ry + ROW_H, 0xFF25232E);

            g.drawString(font, NAMES[i], panelX + 18, ry + 8,
                0xFFFFFFFF, false);

            int color = on ? 0xFF9B59FF : 0xFF66616F;

            g.fill(panelX + PANEL_W - 54, ry + 5,
                panelX + PANEL_W - 18, ry + 20, color);

            g.drawCenteredString(font, on ? "ON" : "OFF",
                panelX + PANEL_W - 36, ry + 9, 0xFFFFFFFF);

            if (i == 8) {
                g.drawString(font, "Settings", panelX + 155, ry + 8,
                    0xFFBBA6D9, false);
            }
        }

        g.disableScissor();

        if (maxScroll() > 0) {
            g.drawCenteredString(font, "Mouse wheel: scroll",
                panelX + PANEL_W / 2, panelY + panelH - 47,
                0xFFBBA6D9);
        }

        g.drawString(font, "Right Shift / ESC: close",
            panelX + 14, panelY + panelH - 18, 0xFF8B8795, false);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
                && mouseX >= panelX + 10
                && mouseX <= panelX + PANEL_W - 10
                && mouseY >= listTop
                && mouseY <= listBottom) {

            for (int i = 0; i < NAMES.length; i++) {
                int ry = rowY(i);

                if (mouseY >= ry && mouseY <= ry + ROW_H) {
                    if (i == 8
                            && mouseX >= panelX + 145
                            && mouseX <= panelX + 220) {
                        minecraft.setScreen(new XraySettingsScreen(this));
                        return true;
                    }

                    toggle(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= (int) Math.signum(scrollY) * 20;
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            scroll = Math.max(0, scroll - 20);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            scroll = Math.min(maxScroll(), scroll + 20);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
