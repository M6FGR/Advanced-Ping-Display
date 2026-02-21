package M6FGR.apd.mixins.server;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import net.minecraft.Util;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Unique
    private long ap$lastPingTriggerTime = 0L;

    @Unique
    private int apd$displayedPing = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ap$onTick(CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor) this;

        if (listener.connection == null || !listener.connection.isConnected()) return;
        if (listener.player == null || listener.player.tickCount < 40) return;

        long millis = Util.getMillis();
        long frequencyMs = (long)(APDConfig.PING_FREQUENCY.get() * 1000L);

        if (millis - ap$lastPingTriggerTime >= frequencyMs) {
            if (!accessor.isKeepAlivePending()) {
                accessor.setKeepAliveTime(millis - 15001L);
                this.ap$lastPingTriggerTime = millis;
            }

            int pingToDisplay;
            if (APDConfig.PING_TYPE.get() == PingType.NORMAL) {
                pingToDisplay = listener.player.latency;
            } else {
                pingToDisplay = (int)(listener.connection.getAverageSentPackets() + listener.connection.getAverageReceivedPackets());
            }

            this.broadcastPing(listener, pingToDisplay);
        }
    }

    @Inject(method = "handleKeepAlive", at = @At("TAIL"))
    private void ap$onHandleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        if (APDConfig.PING_TYPE.get() == PingType.PACKET && this.apd$displayedPing != -1) {
            listener.player.latency = this.apd$displayedPing;
        }
    }

    @Unique
    private void broadcastPing(ServerGamePacketListenerImpl listener, int ping) {
        listener.player.latency = ping;

        ClientboundPlayerInfoUpdatePacket update = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY),
                List.of(listener.player)
        );

        listener.player.server.getPlayerList().broadcastAll(update);
    }
}