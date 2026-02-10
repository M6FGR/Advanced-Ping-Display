package M6FGR.apd.mixins.server;

import M6FGR.apd.mixins.server.ServerGamePacketListenerImplAccessor;
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
    int lastSentPing = -1;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ap$onTick(CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor) this;

        if (listener.connection == null || !listener.connection.isConnected()) return;

        if (listener.player == null || listener.player.tickCount < 40) return;

        long millis = net.minecraft.Util.getMillis();

        if (millis - ap$lastPingTriggerTime >= 1000L) {
            if (!accessor.isKeepAlivePending()) {
                accessor.setKeepAliveTime(millis - 15000L);
                this.ap$lastPingTriggerTime = millis;
            }
        }
    }

    @Inject(method = "handleKeepAlive", at = @At("TAIL"))
    private void ap$onHandleKeepAlive(ServerboundKeepAlivePacket packet, CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        if (listener.player == null) return;
        int currentPing = listener.player.latency;
        ClientboundPlayerInfoUpdatePacket update = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY),
                List.of(listener.player)
        );
        listener.player.server.getPlayerList().broadcastAll(update);
        this.lastSentPing = currentPing;
    }
}
