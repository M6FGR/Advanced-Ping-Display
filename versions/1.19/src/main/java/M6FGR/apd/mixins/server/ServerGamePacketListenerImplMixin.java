package M6FGR.apd.mixins.server;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Unique
    long ap$lastPingTriggerTime = 0L;

    @Unique
    int apd$lastSentPing = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void ap$onTick(CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor) this;

        if (listener.connection == null || !listener.connection.isConnected()) return;
        if (listener.player == null || listener.player.tickCount < 40) return;

        long idleTime = Util.getMillis() - listener.player.getLastActionTime();
        if (idleTime > 3 * 60 * 1000L && listener.player.moveDist == 0) {
            listener.disconnect(Component.literal("Kicked for being idle for too long"));
            return;
        }

        long millis = Util.getMillis();
        double frequencySeconds = APDConfig.SPEC.isLoaded() ? APDConfig.PING_FREQUENCY.get() : 1.0;
        long frequencyMs = (long)(frequencySeconds * 1000L);

        if (millis - ap$lastPingTriggerTime >= frequencyMs) {
            if (!accessor.isKeepAlivePending()) {
                accessor.setKeepAliveTime(millis - 15001L);
                this.ap$lastPingTriggerTime = millis;
            } else {
                if (millis - accessor.getKeepAliveTime() > 5000L) {
                    accessor.setKeepAlivePending(false);
                }
            }
        }
    }

    @Inject(method = "handleKeepAlive", at = @At("TAIL"))
    private void ap$onHandleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        if (listener.player == null || listener.player.server == null) return;

        int currentPing;
        PingType type = APDConfig.SPEC.isLoaded() ? APDConfig.PING_TYPE.get() : PingType.NORMAL;

        if (type == PingType.NORMAL) {
            currentPing = listener.player.latency;
        } else {
            currentPing = (int) (listener.connection.getAverageSentPackets() + listener.connection.getAverageReceivedPackets());

            listener.player.latency = currentPing;
        }

        if (currentPing != apd$lastSentPing) {
            ClientboundPlayerInfoPacket update = new ClientboundPlayerInfoPacket(
                    ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY,
                    List.of(listener.player)
            );

            listener.player.server.getPlayerList().broadcastAll(update);
            this.apd$lastSentPing = currentPing;
        }
    }
}