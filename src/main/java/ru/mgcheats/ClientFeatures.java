package ru.mgcheats;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class ClientFeatures {
    private static final Minecraft MC = Minecraft.getInstance();

    public static boolean guiOpen = false;
    public static boolean autoSprint = true;
    public static boolean fullbright = false;
    public static boolean killAura = false;
    public static boolean reachDisplay = true;
    private static double previousGamma = 1.0;
    private static boolean wasFullbright = false;

    private static KeyMapping openMenu;

    private ClientFeatures() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientFeatures.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientFeatures::registerKeys);
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
        if (event.phase != TickEvent.Phase.END || MC.player == null || MC.level == null) return;

        if (openMenu != null && openMenu.consumeClick()) {
            MC.setScreen(new MGClickScreen());
        }

        if (autoSprint && MC.player.input != null && MC.player.input.forwardImpulse > 0.0F) {
            MC.player.setSprinting(true);
        }

        if (fullbright) {
            if (!wasFullbright) previousGamma = MC.options.gamma().get();
            MC.options.gamma().set(15.0);
            wasFullbright = true;
        } else if (wasFullbright) {
            MC.options.gamma().set(previousGamma);
            wasFullbright = false;
        }

        if (killAura && MC.player.tickCount % 10 == 0) {
            LivingEntity target = findTarget(4.0);
            if (target != null && MC.player.distanceTo(target) <= 4.0F) {
                MC.gameMode.attack(MC.player, target);
                MC.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
        }
    }

    private static LivingEntity findTarget(double range) {
        Player player = MC.player;
        AABB box = player.getBoundingBox().inflate(range);
        return MC.level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive() && !entity.isSpectator())
            .stream()
            .filter(entity -> !(entity instanceof Player other) || !other.isCreative())
            .filter(entity -> player.distanceTo(entity) <= range)
            .min((a, b) -> Double.compare(player.distanceTo(a), player.distanceTo(b)))
            .orElse(null);
    }
}
