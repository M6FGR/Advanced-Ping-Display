package M6FGR.apd.network.server;

import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(
        modid = AdvancedPingDisplay.MODID,
        value = Dist.CLIENT
)
public class DedicatedServerDetector {

    private static boolean modDetectedOnServer = false;

    public static boolean isModOnServer() {
        return modDetectedOnServer;
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event) {
        Connection connection = event.getConnection();
        if (connection != null) {
            NetworkPayloadSetup payloadSetup = ChannelAttributes.getPayloadSetup(connection);

            if (payloadSetup != null) {
                modDetectedOnServer = payloadSetup.channels().values().stream()
                        .anyMatch(map -> map.keySet().stream()
                                .anyMatch(id -> id.getNamespace().equals(AdvancedPingDisplay.MODID)));
            }
        }
    }

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        modDetectedOnServer = false;
    }
}