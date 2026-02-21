package M6FGR.apd.network.protocol;

import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketProtocol {
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(AdvancedPingDisplay.MODID, "packet"),
            () -> "ANY",
            client -> true,
            server -> true
    );

    public static void init() {
        INSTANCE.messageBuilder(SPConfigSync.class, 0)
                .encoder(SPConfigSync::encode)
                .decoder(SPConfigSync::decode)
                .consumerMainThread(SPConfigSync::handle)
                .add();
    }
}