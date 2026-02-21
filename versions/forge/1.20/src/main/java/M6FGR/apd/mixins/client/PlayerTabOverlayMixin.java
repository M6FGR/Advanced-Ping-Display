package M6FGR.apd.mixins.client;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.api.math.MathUtil;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.Connection;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static int PLAYER_SLOT_EXTRA_WIDTH;
    @Inject(
            method = "renderPingIcon",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void renderPingIcon(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo, CallbackInfo ci) {
        int ping = getPing(playerInfo, AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD);
        if (Minecraft.getInstance().isSingleplayer()) return;
        ci.cancel();
        Font font = Minecraft.getInstance().font;
        String sPing;
        int color;

        if (ping < 0) {
            PLAYER_SLOT_EXTRA_WIDTH = 65;
            sPing = ": Loading Ping...";
            color = 0xFFAAAAAA;
        } else {
            sPing = ping + (ping > 500 ? "ms!" : "ms");
            PLAYER_SLOT_EXTRA_WIDTH = (ping > 999) ? 35 : 25;

            float ratio = Math.min(ping / 1000.0F, 1.0F);
            float curvedRatio = (float) Math.sqrt(ratio);

            int r = (int)(curvedRatio * 255);
            int g = (int)((1.0F - curvedRatio) * 255);

            color = (0xFF << 24) | (r << 16) | (g << 8);
        }

        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(0, 0, 1.0f);
        guiGraphics.drawString(font, sPing, x + width - font.width(sPing), y, color, true);

        guiGraphics.pose().popPose();
    }
    @Unique
    private int getPing(PlayerInfo playerInfo, boolean incompatible) {
        if (incompatible) {
            return calculatePacketPing();
        }

        PingType currentType = APDConfig.PING_TYPE.get();

        if (currentType == PingType.PACKET) {
            return calculatePacketPing();
        }

        int vanillaPing = playerInfo.getLatency();
        return Math.max(vanillaPing, -1);
    }

    @Unique
    private int calculatePacketPing() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return -1;

        var networkManager = connection.getConnection();

        float sent = networkManager.getAverageSentPackets();
        float received = networkManager.getAverageReceivedPackets();

        if (sent > 0 || received > 0) {
            int packetMetric = (int) (sent + received);
            return packetMetric > 0 ? packetMetric : -1;
        }

        return 0;
    }

    @ModifyConstant(
            method = "render",
            constant = @Constant(intValue = 13)
    )
    private int modifyTabWidth(int originalWidth) {
        return originalWidth + PLAYER_SLOT_EXTRA_WIDTH;
    }
}