package M6FGR.apd.network.protocol;

import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketProtocol {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(AdvancedPingDisplay.MODID, "packet_protocol"),
            () -> "ANY",
            string -> true,
            string -> true
    );

    private static int packetId = 0;

    public static void init() {
        INSTANCE.registerMessage(packetId++,
                SPConfigSync.class,
                SPConfigSync::encode,
                SPConfigSync::decode,
                SPConfigSync::handle);
    }
}
