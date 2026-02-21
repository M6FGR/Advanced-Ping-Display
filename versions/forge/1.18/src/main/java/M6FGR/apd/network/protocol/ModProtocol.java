package M6FGR.apd.network.protocol;

import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModProtocol {

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(AdvancedPingDisplay.MODID, "main"),
            () -> "ANY", // Change "1" to "ANY"
            s -> true,   // Accept ANY version from server (including null/absent)
            c -> true    // Accept ANY version from client
    );

    public static void init() {

    }
}