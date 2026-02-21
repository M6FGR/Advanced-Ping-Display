package M6FGR.apd.network.server;

import M6FGR.apd.network.protocol.ModProtocol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "adp",
        value = Dist.CLIENT
)
public class ServerAccessor {

    private static boolean modDetectedOnServer = false;

    public static boolean isModOnServer() {
        return modDetectedOnServer;
    }

    @SubscribeEvent
    public static void onJoinServer(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide) return;

        if (event.getEntity() == Minecraft.getInstance().player) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            if (connection != null) {
                modDetectedOnServer = ModProtocol.CHANNEL.isRemotePresent(connection.getConnection());
            }
        }
    }

    @SubscribeEvent
    public static void onDisconnect(LevelEvent.Unload event) {
        modDetectedOnServer = false;
    }
    @SubscribeEvent
    public void onClientLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        modDetectedOnServer = false;
    }
}