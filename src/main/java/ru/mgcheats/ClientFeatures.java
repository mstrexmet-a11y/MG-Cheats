
package ru.mgcheats;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public final class ClientFeatures {
    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean autoSprint = true;
    public static boolean nightVision = false;
    public static boolean autoAim = false;
    public static boolean killAura = false;
    public static boolean fpsHud = true;
    public static boolean coordsHud = true;
    public static boolean keystrokes = false;
    public static boolean reachDisplay = true;

    private static boolean appliedNightVision = false;
    private static KeyMapping openMenu;

    private ClientFeatures() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientFeatures.class);
        FMLJavaModLoadingContext.get().getModEventBus()
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
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (MC.player == null || MC.level == null) return;

        if (openMenu != null && openMenu.consumeClick()) {
            MC.setScreen(new MGClickScreen());
        }

        if (autoSprint && MC.player.input != null
                && MC.player.input.forwardImpulse > 0.0F) {
            MC.player.setSprinting(true);
        }

        updateNightVision();

        if (autoAim) {
            LivingEntity target = findTarget(6.0);
            if (target != null) aimAt(target);
        }

        if (killAura && MC.player.tickCount % 10 == 0
                && MC.gameMode != null) {
            LivingEntity target = findTarget(4.0);
            if (target != null) {
                MC.gameMode.attack(MC.player, target);
                MC.player.swing(
                        net.minecraft.world.InteractionHand.MAIN_HAND
                );
            }
        }
    }

    private static void updateNightVision() {
        if (nightVision) {
            MobEffectInstance effect =
                    MC.player.getEffect(MobEffects.NIGHT_VISION);

            if (effect == null || effect.getDuration() < 100) {
                MC.player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        260, 0, false, false, false
                ));
                appliedNightVision = true;
            }
        } else if (appliedNightVision) {
            MC.player.removeEffect(MobEffects.NIGHT_VISION);
            appliedNightVision = false;
        }
    }

    private static void aimAt(LivingEntity target) {
        double dx = target.getX() - MC.player.getX();
        double dz = target.getZ() - MC.player.getZ();
        double dy = target.getEyeY() - MC.player.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Mth.atan2(dz, dx)
                * (180.0 / Math.PI)) - 90.0F;
        float targetPitch = (float) -(Mth.atan2(dy, horizontal)
                * (180.0 / Math.PI));

        MC.player.setYRot(MC.player.getYRot()
                + Mth.wrapDegrees(targetYaw - MC.player.getYRot()) * 0.25F);
        MC.player.setXRot(MC.player.getXRot()
                + (targetPitch - MC.player.getXRot()) * 0.25F);
    }

    private static LivingEntity findTarget(double range) {
        Player player = MC.player;
        AABB box = player.getBoundingBox().inflate(range);

        return MC.level.getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity != player
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && (!(entity instanceof Player other)
                            || !other.isCreative())
        ).stream()
         .filter(entity -> player.distanceTo(entity) <= range)
         .min((a, b) -> Double.compare(
                 player.distanceTo(a), player.distanceTo(b)))
         .orElse(null);
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
            g.drawString(MC.font, coords, 8, y,
                    0xFFFFFFFF, true);
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

        if (reachDisplay && MC.hitResult instanceof EntityHitResult hit) {
            double distance = MC.player.distanceTo(hit.getEntity());
            String text = String.format("Reach: %.2f blocks", distance);
            int width = MC.font.width(text);
            int centerX = MC.getWindow().getGuiScaledWidth() / 2;

            g.drawString(MC.font, text, centerX - width / 2,
                    MC.getWindow().getGuiScaledHeight() / 2 + 12,
                    0xFFFFFFFF, true);
        }
    }

    private static void drawKey(
            net.minecraft.client.gui.GuiGraphics g,
            String text, int x, int y, boolean pressed) {
        int color = pressed ? 0xFF8B45D9 : 0xCC22222C;
        g.fill(x, y, x + 20, y + 18, color);
        g.drawCenteredString(MC.font, text, x + 10, y + 5,
                0xFFFFFFFF);
    }
}
