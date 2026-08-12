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
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
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
        ClientAtmosphereState.tick();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return;
        var field = ClientBiosphereState.current();
        if (minecraft.level.getGameTime() % 2 == 0) {
            spawnBoundaryParticles(minecraft, field, field.centerX(), field.centerY(), field.centerZ(), field.radius(), true);
            for (var sector : field.sectors()) {
                spawnBoundaryParticles(minecraft, field, sector.center().getX() + 0.5D,
                        sector.center().getY() + sector.radius() / 2.0D, sector.center().getZ() + 0.5D,
                        sector.radius() * sector.stability(), false);
            }
        }
        for (var sector : field.sectors()) {
            double sectorDx = minecraft.player.getX() - (sector.center().getX() + 0.5D);
            double sectorDz = minecraft.player.getZ() - (sector.center().getZ() + 0.5D);
            double sectorDistance = Math.sqrt(sectorDx * sectorDx + sectorDz * sectorDz);
            if (sectorDistance > 32.0D) continue;
            if (sector.status() == SectorStatus.ACTIVE || minecraft.level.getGameTime() % 8 != 0) continue;
            float[] color = statusColor(sector.status());
            minecraft.level.addParticle(new DustParticleOptions(new Vector3f(color[0], color[1], color[2]), 1.45F),
                    sector.center().getX() + 0.5D, sector.center().getY() + 1.2D, sector.center().getZ() + 0.5D,
                    0.0D, 0.06D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        float contamination = ClientAtmosphereState.contamination();
        boolean outside = !ClientBiosphereState.inside(event.getCamera().getPosition().x, event.getCamera().getPosition().y,
                event.getCamera().getPosition().z);
        float intensity = outside ? Math.max(0.72F, contamination) : exteriorHaze(event.getCamera());
        if (intensity <= 0.0F) return;
        event.setRed(event.getRed() * (1.0F - intensity * 0.62F) + intensity * 0.46F);
        event.setGreen(event.getGreen() * (1.0F - intensity * 0.70F) + intensity * 0.38F);
        event.setBlue(event.getBlue() * (1.0F - intensity * 0.94F) + intensity * 0.035F);
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float contamination = ClientAtmosphereState.contamination();
        float boundaryHaze = exteriorHaze(event.getCamera());
        boolean outside = !ClientBiosphereState.inside(event.getCamera().getPosition().x, event.getCamera().getPosition().y,
                event.getCamera().getPosition().z);
        float intensity = outside ? Math.max(0.72F, contamination) : Math.max(contamination, boundaryHaze);
        if (intensity <= 0.0F) return;
        double exitDistance = protectedExitDistance(event.getCamera());
        if (exitDistance > 0.0D) {
            event.setNearPlaneDistance((float) exitDistance);
            event.setFarPlaneDistance((float) (exitDistance + 26.0D));
        } else event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 30.0F - contamination * 14.0F));
    }

    @SubscribeEvent
    public static void onOverlay(RenderGuiOverlayEvent.Post event) {
        float contamination = ClientAtmosphereState.contamination();
        if (contamination <= 0.02F) return;
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        double time = Minecraft.getInstance().level == null ? 0.0D : Minecraft.getInstance().level.getGameTime() * 0.025D;
        for (int blotch = 0; blotch < 6; blotch++) {
            float phase = blotch * 1.73F;
            int alpha = (int) (contamination * (10.0F + (float) Math.sin(time + phase) * 4.0F));
            int color = Math.max(0, alpha) << 24 | 0xC5A438;
            float x = (float) (width * (0.18D + blotch % 3 * 0.34D) + Math.sin(time * 0.63D + phase) * 34.0D);
            float y = (float) (height * (0.22D + blotch / 3 * 0.48D) + Math.cos(time * 0.48D + phase) * 26.0D);
            float size = 72.0F + blotch * 18.0F;
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(x, y, 0.0F);
            event.getGuiGraphics().pose().mulPose(Axis.ZP.rotationDegrees((float) (Math.sin(time * 0.37D + phase) * 24.0D)));
            event.getGuiGraphics().fill((int) -size, (int) (-size * 0.28F), (int) size, (int) (size * 0.28F), color);
            event.getGuiGraphics().fill((int) (-size * 0.42F), (int) (-size * 0.54F), (int) (size * 0.42F), (int) (size * 0.54F), color);
            event.getGuiGraphics().pose().popPose();
        }
    }

    private static float exteriorHaze(net.minecraft.client.Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return 0.0F;
        if (ClientBiosphereState.inside(camera.getPosition().x, camera.getPosition().y, camera.getPosition().z)) return 0.58F;
        var field = ClientBiosphereState.current();
        double x = camera.getPosition().x;
        double y = camera.getPosition().y;
        double z = camera.getPosition().z;
        double nearest = distanceToBoundary(x, y, z, field.centerX(), field.centerY(), field.centerZ(), field.radius());
        for (var sector : field.sectors()) {
            nearest = Math.min(nearest, distanceToBoundary(x, y, z, sector.center().getX() + 0.5D,
                    sector.center().getY() + sector.radius() / 2.0D, sector.center().getZ() + 0.5D,
                    sector.radius() * sector.stability()));
        }
        return nearest >= 0.0D && nearest < 14.0D ? (float) ((14.0D - nearest) / 14.0D * 0.42D) : 0.0F;
    }

    private static double protectedExitDistance(net.minecraft.client.Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !ClientBiosphereState.matches(minecraft.level)) return -1.0D;
        var field = ClientBiosphereState.current();
        double x = camera.getPosition().x;
        double y = camera.getPosition().y;
        double z = camera.getPosition().z;
        var look = camera.getLookVector();
        double farthestExit = rayExitDistance(x, y, z, look.x, look.y, look.z, field.centerX(), field.centerY(), field.centerZ(), field.radius());
        for (var sector : field.sectors()) {
            farthestExit = Math.max(farthestExit, rayExitDistance(x, y, z, look.x, look.y, look.z,
                    sector.center().getX() + 0.5D, sector.center().getY() + sector.radius() / 2.0D,
                    sector.center().getZ() + 0.5D, sector.radius() * sector.stability()));
        }
        return farthestExit;
    }

    private static double rayExitDistance(double x, double y, double z, double lookX, double lookY, double lookZ,
                                          double centerX, double centerY, double centerZ, double radius) {
        double ox = x - centerX;
        double oy = y - centerY;
        double oz = z - centerZ;
        double inside = radius * radius - ox * ox - oy * oy - oz * oz;
        if (inside < 0.0D) return -1.0D;
        double projection = ox * lookX + oy * lookY + oz * lookZ;
        return -projection + Math.sqrt(projection * projection + inside);
    }

    private static double distanceToBoundary(double x, double y, double z, double centerX, double centerY, double centerZ, double radius) {
        double dx = x - centerX;
        double dy = y - centerY;
        double dz = z - centerZ;
        return radius - Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void spawnBoundaryParticles(Minecraft minecraft, SyncBiospherePayload field, double centerX, double centerY,
                                               double centerZ, double radius, boolean initialField) {
        double dx = minecraft.player.getX() - centerX;
        double dy = minecraft.player.getY() - centerY;
        double dz = minecraft.player.getZ() - centerZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (radius <= 0.0D || distance < 0.001D || horizontalDistance < 0.001D || Math.abs(distance - radius) > 18.0D) return;

        double azimuth = Math.atan2(dz, dx);
        double elevation = Math.asin(dy / distance);
        if (!surfaceIsExposed(field, centerX, centerY, centerZ, radius, azimuth - 0.04D, azimuth + 0.04D,
                elevation - 0.04D, elevation + 0.04D, initialField)) return;

        double edgeX = centerX + dx / distance * radius;
        double edgeY = centerY + dy / distance * radius;
        double edgeZ = centerZ + dz / distance * radius;
        double tangentX = -dz / horizontalDistance;
        double tangentZ = dx / horizontalDistance;
        for (int i = -3; i <= 3; i++) {
            double spread = i * 0.8D + (minecraft.level.random.nextDouble() - 0.5D) * 0.35D;
            double y = edgeY + (minecraft.level.random.nextDouble() - 0.5D) * 5.0D;
            minecraft.level.addParticle(CONTAINMENT_DUST, edgeX + tangentX * spread, y, edgeZ + tangentZ * spread,
                    tangentX * 0.015D, 0.045D + minecraft.level.random.nextDouble() * 0.02D, tangentZ * 0.015D);
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
                if (!surfaceIsExposed(field, field.centerX(), field.centerY(), field.centerZ(), field.radius(), a0, a1, e0, e1, true)) continue;
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
        renderAnchorFields(poseStack, field, camera.x, camera.y, camera.z, renderDistance, time);
        renderAnchorSignals(poseStack, field, time);
        renderCentralSignal(poseStack, field, time);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderAnchorFields(PoseStack poseStack, SyncBiospherePayload field, double cameraX, double cameraY,
                                           double cameraZ, double renderDistance, double time) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (var sector : field.sectors()) {
            double radius = sector.radius() * sector.stability();
            double centerX = sector.center().getX() + 0.5D;
            double centerY = sector.center().getY() + sector.radius() / 2.0D;
            double centerZ = sector.center().getZ() + 0.5D;
            double distance = Math.sqrt(Math.pow(cameraX - centerX, 2.0D) + Math.pow(cameraY - centerY, 2.0D) + Math.pow(cameraZ - centerZ, 2.0D));
            if (radius <= 0.0D || distance - radius > renderDistance) continue;
            float[] color = statusColor(sector.status());
            for (int latitude = 0; latitude < 16; latitude++) {
                for (int longitude = 0; longitude < 32; longitude++) {
                    double a0 = Math.PI * 2.0D * longitude / 32.0D;
                    double a1 = Math.PI * 2.0D * (longitude + 1) / 32.0D;
                    double e0 = -Math.PI / 2.0D + Math.PI * latitude / 16.0D;
                    double e1 = -Math.PI / 2.0D + Math.PI * (latitude + 1) / 16.0D;
                    if (!surfaceIsExposed(field, centerX, centerY, centerZ, radius, a0, a1, e0, e1, false)) continue;
                    double wave = Math.sin(a0 * 3.0D + e0 * 7.0D - time);
                    double ring = Math.exp(-Math.pow((e0 + e1) * 0.5D / 0.16D, 2.0D));
                    double scan = Math.exp(-Math.pow((e0 + e1) * 0.5D - Math.sin(time * 0.32D) * 0.9D, 2.0D) / 0.014D);
                    float alpha = (float) (0.19D + (wave + 1.0D) * 0.045D + ring * 0.15D + scan * 0.22D);
                    float brightness = (float) (0.88D + (wave + 1.0D) * 0.16D + ring * 0.34D + scan * 0.42D);
                    addSectorVertex(buffer, poseStack, centerX, centerY, centerZ, radius, a0, e0, color, alpha, brightness);
                    addSectorVertex(buffer, poseStack, centerX, centerY, centerZ, radius, a1, e0, color, alpha, brightness);
                    addSectorVertex(buffer, poseStack, centerX, centerY, centerZ, radius, a1, e1, color, alpha, brightness);
                    addSectorVertex(buffer, poseStack, centerX, centerY, centerZ, radius, a0, e1, color, alpha, brightness);
                }
            }
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static boolean surfaceIsExposed(SyncBiospherePayload field, double centerX, double centerY, double centerZ,
                                            double radius, double azimuthStart, double azimuthEnd, double elevationStart,
                                            double elevationEnd, boolean initialField) {
        double azimuth = (azimuthStart + azimuthEnd) * 0.5D;
        double elevation = (elevationStart + elevationEnd) * 0.5D;
        double cosElevation = Math.cos(elevation);
        double x = centerX + radius * cosElevation * Math.cos(azimuth);
        double y = centerY + radius * Math.sin(elevation);
        double z = centerZ + radius * cosElevation * Math.sin(azimuth);

        if (!initialField && inside(x, y, z, field.centerX(), field.centerY(), field.centerZ(), field.radius())) return false;
        return field.sectors().stream().noneMatch(sector -> {
            double sectorRadius = sector.radius() * sector.stability();
            double sectorX = sector.center().getX() + 0.5D;
            double sectorY = sector.center().getY() + sector.radius() / 2.0D;
            double sectorZ = sector.center().getZ() + 0.5D;
            boolean sameField = !initialField && sectorX == centerX && sectorY == centerY && sectorZ == centerZ;
            return !sameField && sectorRadius > 0.0D && inside(x, y, z, sectorX, sectorY, sectorZ, sectorRadius);
        });
    }

    private static boolean inside(double x, double y, double z, double centerX, double centerY, double centerZ, double radius) {
        double dx = x - centerX;
        double dy = y - centerY;
        double dz = z - centerZ;
        return dx * dx + dy * dy + dz * dz < radius * radius;
    }

    private static void addSectorVertex(BufferBuilder buffer, PoseStack poseStack, double centerX, double centerY, double centerZ,
                                        double radius, double azimuth, double elevation, float[] color, float alpha, float brightness) {
        double cosElevation = Math.cos(elevation);
        buffer.vertex(poseStack.last().pose(), (float) (centerX + radius * cosElevation * Math.cos(azimuth)),
                (float) (centerY + radius * Math.sin(elevation)), (float) (centerZ + radius * cosElevation * Math.sin(azimuth)))
                .color(color[0] * brightness, color[1] * brightness, color[2] * brightness, alpha).endVertex();
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
            float[] color = statusColor(sector.status());
            double x = sector.center().getX() + 0.5D;
            double y = sector.center().getY() + 1.0D;
            double z = sector.center().getZ() + 0.5D;
            double height = sector.status() == SectorStatus.ACTIVE ? 22.0D + Math.sin(time * 2.0D) * 2.0D
                    : 18.0D + Math.sin(time * 2.0D) * 2.0D;
            float alpha = sector.status() == SectorStatus.ACTIVE ? 0.34F
                    : sector.status() == SectorStatus.OFFLINE ? 0.28F : 0.48F;
            buffer.vertex(poseStack.last().pose(), (float) (x - 0.22D), (float) y, (float) z).color(color[0], color[1], color[2], alpha).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x + 0.22D), (float) y, (float) z).color(color[0], color[1], color[2], alpha).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x + 0.06D), (float) (y + height), (float) z).color(color[0], color[1], color[2], 0.02F).endVertex();
            buffer.vertex(poseStack.last().pose(), (float) (x - 0.06D), (float) (y + height), (float) z).color(color[0], color[1], color[2], 0.02F).endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
    }

    private static void renderCentralSignal(PoseStack poseStack, SyncBiospherePayload field, double time) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        double x = field.centerX();
        double y = field.centerY() - field.radius() / 2.0D + 1.0D;
        double z = field.centerZ();
        double height = 36.0D + Math.sin(time * 1.4D) * 4.0D;
        float alpha = 0.56F + (float) Math.sin(time * 1.4D) * 0.12F;
        buffer.vertex(poseStack.last().pose(), (float) (x - 0.34D), (float) y, (float) z).color(0.20F, 1.0F, 0.94F, alpha).endVertex();
        buffer.vertex(poseStack.last().pose(), (float) (x + 0.34D), (float) y, (float) z).color(0.20F, 1.0F, 0.94F, alpha).endVertex();
        buffer.vertex(poseStack.last().pose(), (float) (x + 0.05D), (float) (y + height), (float) z).color(0.20F, 1.0F, 0.94F, 0.02F).endVertex();
        buffer.vertex(poseStack.last().pose(), (float) (x - 0.05D), (float) (y + height), (float) z).color(0.20F, 1.0F, 0.94F, 0.02F).endVertex();
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
        if (event.getLevel().isClientSide()) {
            ClientBiosphereState.clear();
            ClientAtmosphereState.clear();
        }
    }

    private BiosphereVisualEvents() {}
}
