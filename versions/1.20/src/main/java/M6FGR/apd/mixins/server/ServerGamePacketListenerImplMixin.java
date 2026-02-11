package M6FGR.apd.mixins.server;

import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.mixins.server.ServerGamePacketListenerImplAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public abstract SocketAddress getRemoteAddress();

    @Unique
    long ap$lastPingTriggerTime = 0L;

    @Unique
    int lastSentPing = -1;


    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void ap$onTick(CallbackInfo ci) {
        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor) this;

        if (listener.connection == null || !listener.connection.isConnected()) return;

        if (listener.player == null || listener.player.tickCount < 40) return;

        long idleTime = Util.getMillis() - listener.player.getLastActionTime();
        if (idleTime > (2 * 1000) * 60) {
            // timer set for 2 minutes
            listener.disconnect(Component.literal("Kicked for being AFK!"));
        }
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

        if (Math.abs(currentPing - ap$lastPingTriggerTime) > 5) {
            ap$lastPingTriggerTime = currentPing;

            ClientboundPlayerInfoUpdatePacket latencyUpdate = new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY, listener.player
            );

            ClientboundPlayerInfoUpdatePacket nameUpdate = new ClientboundPlayerInfoUpdatePacket(
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, listener.player
            );

            listener.player.server.getPlayerList().broadcastAll(latencyUpdate);
            listener.player.server.getPlayerList().broadcastAll(nameUpdate);
            listener.player.setCustomName(Component.literal(listener.player.getName().toString() + currentPing));
            listener.player.setCustomNameVisible(true);

        }
    }
}

