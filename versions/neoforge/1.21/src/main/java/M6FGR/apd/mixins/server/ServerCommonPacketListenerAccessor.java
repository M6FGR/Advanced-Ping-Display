package M6FGR.apd.mixins.server;

import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommonPacketListenerImpl.class)
public interface ServerCommonPacketListenerAccessor {

    @Accessor("keepAlivePending")
    boolean isKeepAlivePending();

    @Accessor("keepAlivePending")
    void setKeepAlivePending(boolean pending);

    @Accessor("keepAliveTime")
    void setKeepAliveTime(long time);

    @Accessor("keepAliveTime")
    long getKeepAliveTime();


}
