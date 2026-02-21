package M6FGR.apd.network.protocol;

import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
@SuppressWarnings("removal")
@EventBusSubscriber(
        modid = AdvancedPingDisplay.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ModProtocol {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AdvancedPingDisplay.MODID)
                .versioned("1.0.0")
                .optional();

        // Register your packets here
        registrar.playToServer(
                SPConfigSync.TYPE,
                SPConfigSync.STREAM_CODEC,
                SPConfigSync::handle
        );
    }
}