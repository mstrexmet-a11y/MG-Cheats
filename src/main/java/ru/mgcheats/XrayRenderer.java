
package ru.mgcheats;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public final class XrayRenderer {

    public static boolean enabled = false;

    public static boolean coal = true;
    public static boolean iron = true;
    public static boolean copper = true;
    public static boolean gold = true;
    public static boolean redstone = true;
    public static boolean lapis = true;
    public static boolean diamond = true;
    public static boolean emerald = true;

    private static final int RADIUS = 30;

    private static final List<OreBlock> ORES = new ArrayList<>();

    private record OreBlock(BlockPos pos, int color) {}

    private XrayRenderer() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(XrayRenderer.class);
    }

    /*
     * Отрисовка контуров без проверки глубины.
     * Это позволяет видеть контуры руд за непрозрачными блоками.
     */
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (!enabled || mc.level == null || mc.player == null) {
            ORES.clear();
            return;
        }

        // Обновляем список руд раз в 5 тиков.
        if (mc.player.tickCount % 5 != 0) {
            return;
        }

        ORES.clear();

        BlockPos center = mc.player.blockPosition();

        int minY = Math.max(
                mc.level.getMinBuildHeight(),
                center.getY() - RADIUS
        );

        int maxY = Math.min(
                mc.level.getMaxBuildHeight() - 1,
                center.getY() + RADIUS
        );

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {

                    pos.set(
                            center.getX() + x,
                            y,
                            center.getZ() + z
                    );

                    BlockState state = mc.level.getBlockState(pos);
                    int color = getColor(state.getBlock());

                    if (color != 0) {
                        ORES.add(new OreBlock(pos.immutable(), color));
                    }
                }
            }
        }
    }

    private static int getColor(Block block) {
        if (coal && is(block,
                Blocks.COAL_ORE,
                Blocks.DEEPSLATE_COAL_ORE)) {
            return 0xFFAAAAAA;
        }

        if (iron && is(block,
                Blocks.IRON_ORE,
                Blocks.DEEPSLATE_IRON_ORE)) {
            return 0xFFFFD0A0;
        }

        if (copper && is(block,
                Blocks.COPPER_ORE,
                Blocks.DEEPSLATE_COPPER_ORE)) {
            return 0xFFFF994D;
        }

        if (gold && is(block,
                Blocks.GOLD_ORE,
                Blocks.DEEPSLATE_GOLD_ORE,
                Blocks.NETHER_GOLD_ORE)) {
            return 0xFFFFFF33;
        }

        if (redstone && is(block,
                Blocks.REDSTONE_ORE,
                Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return 0xFFFF3333;
        }

        if (lapis && is(block,
                Blocks.LAPIS_ORE,
                Blocks.DEEPSLATE_LAPIS_ORE)) {
            return 0xFF3388FF;
        }

        if (diamond && is(block,
                Blocks.DIAMOND_ORE,
                Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return 0xFF33FFFF;
        }

        if (emerald && is(block,
                Blocks.EMERALD_ORE,
                Blocks.DEEPSLATE_EMERALD_ORE)) {
            return 0xFF33FF66;
        }

        return 0;
    }

    private static boolean is(Block block, Block... choices) {
        for (Block choice : choices) {
            if (block == choice) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!enabled
                || event.getStage()
                != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null || ORES.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        pose.pushPose();

        pose.translate(
                -cameraPos.x,
                -cameraPos.y,
                -cameraPos.z
        );

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder lines = tesselator.getBuilder();
        lines.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        for (OreBlock ore : ORES) {
            BlockPos p = ore.pos();

            double distance = mc.player.distanceToSqr(
                    p.getX() + 0.5,
                    p.getY() + 0.5,
                    p.getZ() + 0.5
            );

            if (distance > RADIUS * RADIUS ) {
                continue;
            }

            AABB box = new AABB(p).inflate(0.002);

            int color = ore.color();

            float red = ((color >> 16) & 255) / 255.0F;
            float green = ((color >> 8) & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;

            LevelRenderer.renderLineBox(
                    pose,
                    lines,
                    box,
                    red,
                    green,
                    blue,
                    1.0F
            );
        }

        pose.popPose();

        // Draw the ore outlines with depth testing disabled so blocks
        // do not hide the highlighted ores.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(2.0F);
        BufferUploader.drawWithShader(lines.end());
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
