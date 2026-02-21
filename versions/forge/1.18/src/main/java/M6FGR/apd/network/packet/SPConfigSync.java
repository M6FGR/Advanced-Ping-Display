package M6FGR.apd.network.packet;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SPConfigSync {
    private final double frequency;
    private final int pingType;

    public SPConfigSync(double frequency, int pingType) {
        this.frequency = frequency;
        this.pingType = pingType;
    }

    public static void encode(SPConfigSync msg, FriendlyByteBuf buffer) {
        buffer.writeDouble(msg.frequency);
        buffer.writeInt(msg.pingType);
    }

    public static SPConfigSync decode(FriendlyByteBuf buffer) {
        return new SPConfigSync(buffer.readDouble(), buffer.readInt());
    }

    public static void handle(SPConfigSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            APDConfig.PING_FREQUENCY.set(msg.frequency);
            APDConfig.PING_TYPE.set(PingType.values()[msg.pingType]);
            APDConfig.SPEC.save();
            AdvancedPingDisplay.LOGGER.debug("Updated the server config via the client");
        });
        ctx.get().setPacketHandled(true);
    }
}