package io.hailpurge.client.biosphere;

import io.hailpurge.biosphere.domain.SectorStatus;
import io.hailpurge.biosphere.network.SyncBiospherePayload;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class BiosphereVisualEvents {
    private static final DustParticleOptions CONTAINMENT_DUST = new DustParticleOptions(new Vector3f(0.18F, 0.92F, 0.88F), 1.15F);

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientBiosphereState.updateCrossing();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return;
        var field = ClientBiosphereState.current();
        double dx = minecraft.player.getX() - field.centerX();
        double dy = minecraft.player.getY() - field.centerY();
        double dz = minecraft.player.getZ() - field.centerZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.001D || Math.abs(distance - field.radius()) > 18.0D || minecraft.level.getGameTime() % 2 != 0) return;

        double edgeX = field.centerX() + dx / distance * field.radius();
        double edgeY = field.centerY() + dy / distance * field.radius();
        double edgeZ = field.centerZ() + dz / distance * field.radius();
        double tangentX = -dz / Math.sqrt(dx * dx + dz * dz);
        double tangentZ = dx / Math.sqrt(dx * dx + dz * dz);
        for (int i = -3; i <= 3; i++) {
            double spread = i * 0.8D + (minecraft.level.random.nextDouble() - 0.5D) * 0.35D;
            double y = edgeY + (minecraft.level.random.nextDouble() - 0.5D) * 5.0D;
            minecraft.level.addParticle(CONTAINMENT_DUST, edgeX + tangentX * spread, y, edgeZ + tangentZ * spread,
                    tangentX * 0.015D, 0.045D + minecraft.level.random.nextDouble() * 0.02D, tangentZ * 0.015D);
        }
        for (var sector : field.sectors()) {
            if (sector.status() == SectorStatus.ACTIVE) continue;
            double sectorDx = minecraft.player.getX() - (sector.center().getX() + 0.5D);
            double sectorDz = minecraft.player.getZ() - (sector.center().getZ() + 0.5D);
            double sectorDistance = Math.sqrt(sectorDx * sectorDx + sectorDz * sectorDz);
            if (sectorDistance > 32.0D || minecraft.level.getGameTime() % 8 != 0) continue;
            float[] color = statusColor(sector.status());
            minecraft.level.addParticle(new DustParticleOptions(new Vector3f(color[0], color[1], color[2]), 1.45F),
                    sector.center().getX() + 0.5D, sector.center().getY() + 1.2D, sector.center().getZ() + 0.5D,
                    0.0D, 0.06D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return;

        var field = ClientBiosphereState.current();
        var camera = event.getCamera().getPosition();
        double dx = camera.x - field.centerX();
        double dy = camera.y - field.centerY();
        double dz = camera.z - field.centerZ();
        double centerDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double renderDistance = minecraft.options.renderDistance().get() * 16.0D;
        if (centerDistance - field.radius() > renderDistance) return;
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        double time = (minecraft.level.getGameTime() + event.getPartialTick()) * 0.035D;

        // The whole dome stays visible within render distance; this remains a small, fixed mesh.
        int longitudes = 32;
        int latitudes = 16;
        for (int latitude = 0; latitude < latitudes; latitude++) {
            for (int longitude = 0; longitude < longitudes; longitude++) {
                double a0 = Math.PI * 2.0D * longitude / longitudes;
                double a1 = Math.PI * 2.0D * (longitude + 1) / longitudes;
                double e0 = -Math.PI / 2.0D + Math.PI * latitude / latitudes;
                double e1 = -Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudes;
                double wave = Math.sin(a0 * 3.0D + e0 * 7.0D - time);
                double ring = Math.exp(-Math.pow((e0 + e1) * 0.5D / 0.16D, 2.0D));
                double scan = Math.exp(-Math.pow((e0 + e1) * 0.5D - Math.sin(time * 0.32D) * 0.9D, 2.0D) / 0.014D);
                float alpha = (float) (0.19D + (wave + 1.0D) * 0.045D + ring * 0.15D + scan * 0.22D);
                float brightness = (float) (0.88D + (wave + 1.0D) * 0.16D + ring * 0.34D + scan * 0.42D);
                addVertex(buffer, poseStack, field, a0, e0, alpha, brightness);
                addVertex(buffer, poseStack, field, a1, e0, alpha, brightness);
                addVertex(buffer, poseStack, field, a1, e1, alpha, brightness);
                addVertex(buffer, poseStack, field, a0, e1, alpha, brightness);
            }
        }
        BufferUploader.drawWithShader(buffer.end());
        renderGrid(poseStack, field, time);
        renderAnchorSignals(poseStack, field, time);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderGrid(PoseStack poseStack, SyncBiospherePayload field, double time) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder lines = Tesselator.getInstance().getBuilder();
        lines.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        int longitudes = 32;
        int latitudes = 16;
        for (int latitude = 1; latitude < latitudes; latitude += 2) {
            double elevation = -Math.PI / 2.0D + Math.PI * latitude / latitudes;
            for (int longitude = 0; longitude < longitudes; longitude++) {
                addLineVertex(lines, poseStack, field, Math.PI * 2.0D * longitude / longitudes, elevation, 0.42F);
                addLineVertex(lines, poseStack, field, Math.PI * 2.0D * (longitude + 1) / longitudes, elevation, 0.42F);
            }
        }
        for (int longitude = 0; longitude < longitudes; longitude += 2) {
            double azimuth = Math.PI * 2.0D * longitude / longitudes;
            for (int latitude = 0; latitude < latitudes; latitude++) {
                addLineVertex(lines, poseStack, field, azimuth, -Math.PI / 2.0D + Math.PI * latitude / latitudes, 0.34F);
                addLineVertex(lines, poseStack, field, azimuth, -Math.PI / 2.0D + Math.PI * (latitude + 1) / latitudes, 0.34F);
            }
        }
        BufferUploader.drawWithShader(lines.end());
    }

    private static void addLineVertex(BufferBuilder buffer, PoseStack poseStack, SyncBiospherePayload field,
                                      double azimuth, double elevation, float alpha) {
        double cosElevation = Math.cos(elevation);
        double x = field.centerX() + field.radius() * cosElevation * Math.cos(azimuth);
        double y = field.centerY() + field.radius() * Math.sin(elevation);
        double z = field.centerZ() + field.radius() * cosElevation * Math.sin(azimuth);
        buffer.vertex(poseStack.last().pose(), (float) x, (float) y, (float) z).color(0.18F, 1.0F, 0.92F, alpha).endVertex();
    }

    private static void addVertex(BufferBuilder buffer, PoseStack poseStack, SyncBiospherePayload field,
                                  double azimuth, double elevation, float alpha, float brightness) {
        double cosElevation = Math.cos(elevation);
        double x = field.centerX() + field.radius() * cosElevation * Math.cos(azimuth);
        double y = field.centerY() + field.radius() * Math.sin(elevation);
        double z = field.centerZ() + field.radius() * cosElevation * Math.sin(azimuth);
        buffer.vertex(poseStack.last().pose(), (float) x, (float) y, (float) z)
                .color(0.08F * brightness, 0.82F * brightness, 0.76F * brightness, alpha).endVertex();
    }

    private static void renderAnchorSignals(PoseStack poseStack, SyncBiospherePayload field, double time) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (var sector : field.sectors()) {
            if (sector.status() == SectorStatus.ACTIVE) continue;
            float[] color = statusColor(sector.status());
            double x = sector.center().getX() + 0.5D;
            double y = sector.center().getY() + 1.0D;
            double z = sector.center().getZ() + 0.5D;
            double height = 18.0D + Math.sin(time * 2.0D) * 2.0D;
            float alpha = sector.status() == SectorStatus.OFFLINE ? 0.28F : 0.48F;
            buffer.vertex(poseStack.last().pose(), (float) (x - 0.22D), (float) y, (float) z).color(color[0], color[1], color[2], alpha).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x + 0.22D), (float) y, (float) z).color(color[0], color[1], color[2], alpha).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x + 0.06D), (float) (y + height), (float) z).color(color[0], color[1], color[2], 0.02F).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x - 0.06D), (float) (y + height), (float) z).color(color[0], color[1], color[2], 0.02F).endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static float[] statusColor(SectorStatus status) {
        return switch (status) {
            case LOW_POWER -> new float[] {1.0F, 0.70F, 0.10F};
            case DEGRADING, OFFLINE -> new float[] {1.0F, 0.12F, 0.08F};
            case RECOVERING -> new float[] {0.18F, 0.92F, 0.84F};
            case ACTIVE -> new float[] {0.10F, 0.82F, 0.76F};
        };
    }

    @SubscribeEvent
    public static void onClientLogout(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) ClientBiosphereState.clear();
    }

    private BiosphereVisualEvents() {}
}
