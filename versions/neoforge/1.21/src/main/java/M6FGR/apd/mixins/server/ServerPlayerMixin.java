package M6FGR.apd.mixins.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ap$initLatency(CallbackInfo ci) {
        if (this.connection != null) {
            this.connection.latency = -1;
        }
    }

}
