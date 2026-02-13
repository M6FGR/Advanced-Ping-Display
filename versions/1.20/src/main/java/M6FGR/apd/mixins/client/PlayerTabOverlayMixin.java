package M6FGR.apd.mixins.client;

import M6FGR.apd.api.math.MathUtil;
import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static int PLAYER_SLOT_EXTRA_WIDTH;
    @Overwrite
    protected void renderPingIcon(GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo) {
        int ping = getPing(playerInfo, AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD);
        if (Minecraft.getInstance().isSingleplayer()) return;

        Font font = Minecraft.getInstance().font;
        String sPing;
        int color = 0;

        if (ping < 0) {
            PLAYER_SLOT_EXTRA_WIDTH = 65;
            sPing = ": Loading Ping...";
            color = MathUtil.toHex(0.5F, 0.5F, 0.5F);
        } else if (ping > 200) {
            sPing = ping + "ms!";
        } else {
            sPing = ping + "ms";
            PLAYER_SLOT_EXTRA_WIDTH = 25;

            float ratio = Math.min(ping / 1000.0F, 1.0F);

            float curvedRatio = (float) Math.pow(ratio, 0.5);

            float r = curvedRatio;
            float g = 1.0F - curvedRatio;
            float b = 0.0F;

            color = MathUtil.toHex(r, g, b);

            if (ping > 999) {
                PLAYER_SLOT_EXTRA_WIDTH = 35;
            }
        }

        guiGraphics.drawString(font, sPing, x + width - font.width(sPing), y, color, true);
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