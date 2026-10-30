
package ru.mgcheats;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public final class ClientFeatures {
    private static final Minecraft MC = Minecraft.getInstance();

    private static final double PLAYER_RANGE = 10.0;
    private static final double AIM_RANGE = 6.0;
    private static final double AIM_MAX_ANGLE = 10.0;
    private static final float AIM_SMOOTHING = 0.12F;

    public static boolean autoSprint = true;
    public static boolean nightVision = false;
    public static boolean autoAim = false;
    public static boolean killAura = false;
    public static boolean fpsHud = true;
    public static boolean coordsHud = true;
    public static boolean keystrokes = false;
    public static boolean reachDisplay = true;
    public static boolean playerList = true;

    private static boolean appliedNightVision = false;
    private static KeyMapping openMenu;
    private static KeyMapping toggleAim;

    private ClientFeatures() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientFeatures.class);
        FMLJavaModLoadingContext.get()
                .getModEventBus()
                .addListener(ClientFeatures::registerKeys);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        openMenu = new KeyMapping(
                "key.mgcheats.menu",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.mgcheats"
        );
        event.register(openMenu);

        toggleAim = new KeyMapping(
                "key.mgcheats.toggle_aim",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL,
                "key.categories.mgcheats"
        );
        event.register(toggleAim);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (MC.player == null || MC.level == null) return;

        if (openMenu != null && openMenu.consumeClick()) {
            MC.setScreen(new MGClickScreen());
        }

        if (toggleAim != null && toggleAim.consumeClick()) {
            autoAim = !autoAim;
        }

        if (autoSprint && MC.player.input != null
                && MC.player.input.forwardImpulse > 0.0F) {
            MC.player.setSprinting(true);
        }

        updateNightVision();

        if (autoAim) {
            LivingEntity target = findAimTarget();
            if (target != null) {
                aimAt(target);
            }
        }

        if (killAura && MC.player.tickCount % 10 == 0
                && MC.gameMode != null) {
            LivingEntity target = findAimTarget();
            if (target != null
                    && MC.player.distanceTo(target) <= 4.0F) {
                MC.gameMode.attack(MC.player, target);
                MC.player.swing(
                        net.minecraft.world.InteractionHand.MAIN_HAND
                );
            }
        }
    }

    // Эффект длится 25 секунд и обновляется на отметке 15 секунд.
    private static void updateNightVision() {
        if (nightVision) {
            MobEffectInstance effect =
                    MC.player.getEffect(MobEffects.NIGHT_VISION);

            if (effect == null || effect.getDuration() <= 300) {
                MC.player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        500,
                        0,
                        false,
                        false,
                        false
                ));
                appliedNightVision = true;
            }
        } else if (appliedNightVision) {
            MC.player.removeEffect(MobEffects.NIGHT_VISION);
            appliedNightVision = false;
        }
    }

    // Находим игрока только если прицел уже находится рядом с ним.
    private static LivingEntity findAimTarget() {
        Player player = MC.player;
        Vec3 look = player.getLookAngle();

        return MC.level.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(AIM_RANGE)
                ).stream()
                .filter(target -> target != player)
                .filter(target -> target.isAlive())
                .filter(target -> !target.isSpectator())
                .filter(target -> !(target instanceof Player other) || !other.isCreative())
                .filter(target ->
                        player.distanceTo(target) <= AIM_RANGE)
                .filter(target -> {
                    Vec3 direction = target.getEyePosition()
                            .subtract(player.getEyePosition())
                            .normalize();

                    double dot = Mth.clamp(
                            look.dot(direction), -1.0, 1.0);
                    double angle = Math.toDegrees(
                            Math.acos(dot));

                    return angle <= AIM_MAX_ANGLE;
                })
                .min(Comparator.comparingDouble(
                        player::distanceTo))
                .orElse(null);
    }

    private static void aimAt(LivingEntity target) {
        double dx = target.getX() - MC.player.getX();
        double dz = target.getZ() - MC.player.getZ();
        double dy = target.getEyeY() - MC.player.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (
                Mth.atan2(dz, dx) * (180.0 / Math.PI)
        ) - 90.0F;

        float targetPitch = (float) -(
                Mth.atan2(dy, horizontal) * (180.0 / Math.PI)
        );

        float yawDifference = Mth.wrapDegrees(
                targetYaw - MC.player.getYRot());

        MC.player.setYRot(
                MC.player.getYRot()
                        + yawDifference * AIM_SMOOTHING);

        MC.player.setXRot(Mth.clamp(
                MC.player.getXRot()
                        + (targetPitch - MC.player.getXRot())
                        * AIM_SMOOTHING,
                -90.0F,
                90.0F
        ));
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (MC.player == null || MC.level == null) return;

        var g = event.getGuiGraphics();
        int y = 8;

        if (fpsHud) {
            g.drawString(MC.font, "FPS: " + MC.getFps(),
                    8, y, 0xFF55FF55, true);
            y += 12;
        }

        if (coordsHud) {
            String coords = String.format(
                    "XYZ: %.1f / %.1f / %.1f",
                    MC.player.getX(),
                    MC.player.getY(),
                    MC.player.getZ()
            );

            g.drawString(MC.font, coords,
                    8, y, 0xFFFFFFFF, true);
            y += 12;
        }

        // Список игроков в радиусе 10 блоков.
        if (playerList) {
            List<? extends Player> nearby = MC.level.players().stream()
                    .filter(p -> p != MC.player)
                    .filter(Player::isAlive)
                    .filter(p ->
                            MC.player.distanceTo(p) <= PLAYER_RANGE)
                    .sorted(Comparator.comparingDouble(
                            MC.player::distanceTo))
                    .toList();

            g.drawString(MC.font,
                    "Players (" + nearby.size() + ")",
                    8, y, 0xFFB77AFF, true);
            y += 12;

            for (Player p : nearby) {
                double distance = MC.player.distanceTo(p);
                String line = p.getName().getString()
                        + " [" + String.format("%.1f", distance) + "m]";

                g.drawString(MC.font, line,
                        8, y, 0xFFFFFFFF, true);
                y += 11;
            }
        }

        if (keystrokes) {
            int x = 8;
            int baseY = MC.getWindow().getGuiScaledHeight() - 55;

            drawKey(g, "W", x + 22, baseY,
                    MC.options.keyUp.isDown());
            drawKey(g, "A", x, baseY + 22,
                    MC.options.keyLeft.isDown());
            drawKey(g, "S", x + 22, baseY + 22,
                    MC.options.keyDown.isDown());
            drawKey(g, "D", x + 44, baseY + 22,
                    MC.options.keyRight.isDown());
            drawKey(g, "LMB", x + 70, baseY + 22,
                    MC.options.keyAttack.isDown());
        }

        if (reachDisplay
                && MC.hitResult instanceof EntityHitResult hit) {
            double distance =
                    MC.player.distanceTo(hit.getEntity());

            String text = String.format(
                    "Reach: %.2f blocks", distance);

            int centerX =
                    MC.getWindow().getGuiScaledWidth() / 2;

            g.drawString(MC.font, text,
                    centerX - MC.font.width(text) / 2,
                    MC.getWindow().getGuiScaledHeight() / 2 + 12,
                    0xFFFFFFFF, true);
        }
    }

    private static void drawKey(
            net.minecraft.client.gui.GuiGraphics g,
            String text, int x, int y, boolean pressed) {
        int color = pressed
                ? 0xFF8B45D9 : 0xCC22222C;

        g.fill(x, y, x + 20, y + 18, color);
        g.drawCenteredString(MC.font, text,
                x + 10, y + 5, 0xFFFFFFFF);
    }
}
