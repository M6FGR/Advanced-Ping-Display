package M6FGR.apd.mixins.client;

import M6FGR.apd.main.AdvancedPingDisplay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private int apd$extraWidth;

    @Inject(
            method = "renderPingIcon",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void onRenderPingIcon(PoseStack poseStack, int width, int x, int y, PlayerInfo playerInfo, CallbackInfo ci) {
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return;
        }

        ci.cancel();

        int ping = apd$getPing(playerInfo, AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD);
        Font font = Minecraft.getInstance().font;
        String sPing;
        int color;

        if (ping < 0) {
            this.apd$extraWidth = 65;
            sPing = ": Loading Ping...";
            color = 0xFFA0A0A0;
        } else {
            sPing = ping + "ms";
            this.apd$extraWidth = (ping > 999) ? 35 : 25;

            float ratio = Math.min(ping / 1000.0F, 1.0F);
            float curvedRatio = (float) Math.pow(ratio, 0.5);
            int r = (int) (curvedRatio * 255);
            int g = (int) ((1.0F - curvedRatio) * 255);
            color = 0xFF000000 | (r << 16) | (g << 8);
        }

        font.drawShadow(poseStack, sPing, (float)(x + width - font.width(sPing)), (float)y, color);
    }

    @Unique
    private int apd$getPing(PlayerInfo playerInfo, boolean isIncompatibleModsPresent) {
        if (Minecraft.getInstance().player == null) return playerInfo.getLatency();

        boolean isSelf = playerInfo.getProfile().getId().equals(Minecraft.getInstance().player.getGameProfile().getId());
        if (isIncompatibleModsPresent && isSelf) {
            return (int) (Minecraft.getInstance().getConnection().getConnection().getAverageSentPackets());
        }
        return playerInfo.getLatency();
    }

    @ModifyConstant(
            method = "render",
            constant = @Constant(intValue = 13)
    )
    private int modifyTabWidth(int originalWidth) {
        return originalWidth + this.apd$extraWidth;
    }
}