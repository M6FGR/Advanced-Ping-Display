package M6FGR.apd.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Unique
    private static int PLAYER_SLOT_EXTRA_WIDTH;
    @Unique
    private final boolean ap$isEmbeddiumPresent = net.minecraftforge.fml.loading.LoadingModList.get()
            .getMods().stream()
            .anyMatch(mod -> mod.getModId().equals("embeddium"));
    @Redirect(
            method = "render",
            at =
            @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/components/PlayerTabOverlay;renderPingIcon(Lnet/minecraft/client/gui/GuiGraphics;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"))
    protected void renderPingIcon(PlayerTabOverlay instance, GuiGraphics guiGraphics, int width, int x, int y, PlayerInfo playerInfo) {
        int ping = -1;
        if (ap$isEmbeddiumPresent || Minecraft.getInstance().level.isClientSide) {
            if (playerInfo.getProfile().getId().equals(Minecraft.getInstance().player.getUUID())) {
                ping = (int) (Minecraft.getInstance().getConnection().getConnection().getAverageSentPackets());
            }
        } else {
            ping = playerInfo.getLatency();
        }
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
        guiGraphics.drawString(font, sPing, x + width - font.width(sPing), y, color, false);
    }

    @ModifyConstant(
            method = "render",
            constant = @Constant(intValue = 13)
    )
    private int modifyTabWidth(int originalWidth) {
        return originalWidth + PLAYER_SLOT_EXTRA_WIDTH;
    }
}