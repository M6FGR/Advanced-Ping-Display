package M6FGR.apd.mixins.client;

import M6FGR.apd.main.AdvancedPingDisplay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Shadow @Final private Minecraft minecraft;
    @Unique
    private static int PLAYER_SLOT_EXTRA_WIDTH;
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    protected void renderPingIcon(PlayerTabOverlay instance, PoseStack poseStack, int width, int x, int y, PlayerInfo playerInfo) {
        int ping = getPing(playerInfo, AdvancedPingDisplay.HAS_TARGET_MOD);
        if (Minecraft.getInstance().isSingleplayer()) return;
        Font font = Minecraft.getInstance().font;
        String sPing;
        int color;
        if (ping < 0) {
            PLAYER_SLOT_EXTRA_WIDTH = 65;
            sPing = ": Loading Ping...";
            color = 0xB2B2B2;
        } else {
            sPing = ping + "ms";
            if (ping > 1000) {
                PLAYER_SLOT_EXTRA_WIDTH = 15;
                color = 0xFF1A1A;
            } else if (ping > 500) {
                color = 0x99CCCC;
            } else if (ping > 300) {
                color = 0x66E6E6;
            } else if (ping > 100) {
                color = 0x80CC1A;
            } else if (ping > 50) {
                color = 0x80FF4D;
            } else {
                PLAYER_SLOT_EXTRA_WIDTH = 25;
                color = 0x1AFF1A;
            }
        }
        float renderX = (float)(x + width - font.width(sPing));
        float renderY = (float)y;
        font.drawShadow(poseStack, sPing, renderX, renderY, color);
    }

    private static int getPing(PlayerInfo playerInfo, boolean isEmbeddiumPresent) {
        int ping;
        boolean isSelf = playerInfo.getProfile().getId().equals(Minecraft.getInstance().player.getGameProfile().getId());
        if (isEmbeddiumPresent && isSelf) {
            ping = (int) (Minecraft.getInstance().getConnection().getConnection().getAverageSentPackets());
        } else {
            ping = playerInfo.getLatency();
        }
        return ping;
    }

    @ModifyConstant(
            method = "render",
            constant = @Constant(intValue = 13)
    )
    private int modifyTabWidth(int originalWidth) {
        return originalWidth + PLAYER_SLOT_EXTRA_WIDTH;
    }
}