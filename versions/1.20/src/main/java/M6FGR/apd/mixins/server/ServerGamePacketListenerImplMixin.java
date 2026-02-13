package M6FGR.apd.mixins.server;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
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
    long ap$lastPingTriggerTime = 0L;

    @Unique
    int apd$lastSentPing;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ap$onTick(CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;

        if (listener.connection == null || !listener.connection.isConnected()) return;
        if (listener.player == null || listener.player.tickCount < 40) return;

        long millis = Util.getMillis();
        double freq = APDConfig.PING_FREQUENCY.get();
        long frequencyMs = (long)(freq * 1000L);

        if (millis - ap$lastPingTriggerTime >= frequencyMs) {
            PingType currentType = APDConfig.PING_TYPE.get();
            int currentPingValue;

            if (currentType == PingType.NORMAL) {
                currentPingValue = listener.player.latency;
            } else {
                float sent = listener.connection.getAverageSentPackets();
                float received = listener.connection.getAverageReceivedPackets();
                currentPingValue = (sent > 0 || received > 0) ? (int)(sent + received) : -1;
            }

            if (currentPingValue != apd$lastSentPing) {
                broadcastPing(listener, currentPingValue);
                this.apd$lastSentPing = currentPingValue;
            }

            this.ap$lastPingTriggerTime = millis;

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
        this.apd$lastSentPing = ping;
    }

    @Inject(method = "handleKeepAlive", at = @At("TAIL"))
    private void ap$onHandleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci) {
    }
}