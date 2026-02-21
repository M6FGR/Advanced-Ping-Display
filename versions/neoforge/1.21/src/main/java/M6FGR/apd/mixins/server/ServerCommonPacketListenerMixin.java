package M6FGR.apd.mixins.server;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import net.minecraft.Util;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerMixin {
    @Unique
    private long apd$lastPingTriggerTime = 0L;

    @Unique
    private int apd$displayedPing = -1;

    @Inject(method = "keepConnectionAlive", at = @At("TAIL"))
    private void apd$onTick(CallbackInfo ci) {
        ServerCommonPacketListenerImpl listener = (ServerCommonPacketListenerImpl) (Object) this;
        if (!(listener instanceof ServerGamePacketListenerImpl gameListener)) return;

        ServerCommonPacketListenerAccessor accessor = (ServerCommonPacketListenerAccessor) this;
        long millis = Util.getMillis();

        // 1. DEDICATED TIMER FOR KEEP-ALIVE TRIGGER (The "Force" Ping)
        // We only force a keep-alive packet based on the config frequency
        long frequencyMs = (long)(APDConfig.PING_FREQUENCY.get() * 1000L);
        if (millis - apd$lastPingTriggerTime >= frequencyMs) {
            if (!accessor.isKeepAlivePending()) {
                accessor.setKeepAliveTime(millis - 15001L);
                this.apd$lastPingTriggerTime = millis;
            }
        }

        int pingToDisplay;
        if (APDConfig.PING_TYPE.get() == PingType.PACKET) {
            pingToDisplay = (int) (listener.getConnection().getAverageReceivedPackets() + listener.getConnection().getAverageSentPackets());
            if (pingToDisplay != this.apd$displayedPing) {
                this.apd$displayedPing = pingToDisplay;
                this.apd$broadcastPing(gameListener, pingToDisplay);
            }
        } else if (millis - apd$lastPingTriggerTime >= frequencyMs) {
            pingToDisplay = listener.latency();
            this.apd$displayedPing = pingToDisplay;
            this.apd$broadcastPing(gameListener, pingToDisplay);
        }
    }

    @Inject(method = "handleKeepAlive", at = @At("TAIL"))
    private void apd$onHandleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci) {
        if ((Object)this instanceof ServerGamePacketListenerImpl gameListener) {
            if (APDConfig.PING_TYPE.get() == PingType.PACKET && this.apd$displayedPing != -1) {
                gameListener.player.connection.latency = this.apd$displayedPing;
            }
        }
    }

    @Unique
    private void apd$broadcastPing(ServerGamePacketListenerImpl listener, int ping) {
        ServerPlayer player = listener.player;

        player.connection.latency = ping;

        ClientboundPlayerInfoUpdatePacket update = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY),
                List.of(player)
        );

        if (player.server != null) {
            player.server.getPlayerList().broadcastAll(update);
        }
    }
}