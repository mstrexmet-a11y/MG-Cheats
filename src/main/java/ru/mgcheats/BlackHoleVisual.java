package ru.mgcheats;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Client-only decorative black-hole effect.
 * It only renders particles: it does not affect entities, blocks, or physics.
 */
public final class BlackHoleVisual {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int age = 0;

    public static boolean enabled = false;

    private BlackHoleVisual() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(BlackHoleVisual.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !enabled
                || MC.player == null || MC.level == null) {
            return;
        }

        age++;
        Vec3 center = MC.player.getEyePosition()
                .add(MC.player.getLookAngle().scale(5.0));

        // Two tilted rings of particles orbit around an empty dark center.
        for (int i = 0; i < 4; i++) {
            double angle = age * 0.16 + i * (Math.PI / 2.0);
            double radius = 0.85;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            MC.level.addParticle(ParticleTypes.PORTAL,
                    center.x + x, center.y + Math.sin(angle * 1.7) * 0.18,
                    center.z + z,
                    -Math.sin(angle) * 0.025, 0.008,
                    Math.cos(angle) * 0.025);

            double innerAngle = -angle * 1.35;
            MC.level.addParticle(ParticleTypes.REVERSE_PORTAL,
                    center.x + Math.cos(innerAngle) * 0.48,
                    center.y + Math.sin(innerAngle) * 0.12,
                    center.z + Math.sin(innerAngle) * 0.48,
                    0.0, 0.006, 0.0);
        }

        if (age % 3 == 0) {
            MC.level.addParticle(ParticleTypes.SMOKE,
                    center.x, center.y, center.z,
                    0.0, 0.012, 0.0);
        }
    }
}
