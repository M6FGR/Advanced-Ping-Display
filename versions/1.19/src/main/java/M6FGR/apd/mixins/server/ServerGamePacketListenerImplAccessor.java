package M6FGR.apd.mixins.server;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("keepAliveTime")
    void setKeepAliveTime(long time);

    @Accessor("keepAlivePending")
    boolean isKeepAlivePending();

    @Accessor("connection")
    Connection getConnection();

}