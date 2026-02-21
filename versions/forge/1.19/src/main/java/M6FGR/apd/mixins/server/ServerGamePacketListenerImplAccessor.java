package M6FGR.apd.mixins.server;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("keepAlivePending")
    boolean isKeepAlivePending();

    @Accessor("keepAlivePending")
    void setKeepAlivePending(boolean setter);

    @Accessor("keepAliveTime")
    void setKeepAliveTime(long time);

    @Accessor("keepAliveTime")
    long getKeepAliveTime();



}
