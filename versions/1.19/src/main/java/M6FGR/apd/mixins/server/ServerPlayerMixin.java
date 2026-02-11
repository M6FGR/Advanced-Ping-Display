package M6FGR.apd.mixins.server;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    // makes the default ping -1 if wasn't loaded
    @Inject(method = "<init>", at = @At("RETURN"))
    private void ap$initLatency(CallbackInfo ci) {
        ((ServerPlayer) (Object) this).latency = -1;
    }
}