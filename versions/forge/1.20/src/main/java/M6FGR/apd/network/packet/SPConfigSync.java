package M6FGR.apd.network.packet;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SPConfigSync {
    private final double frequency;
    private final int typeOrdinal;

    public SPConfigSync(double frequency, int typeOrdinal) {
        this.frequency = frequency;
        this.typeOrdinal = typeOrdinal;
    }

    public static void encode(SPConfigSync msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.frequency);
        buf.writeInt(msg.typeOrdinal);
    }

    public static SPConfigSync decode(FriendlyByteBuf buf) {
        return new SPConfigSync(buf.readDouble(), buf.readInt());
    }

    public static void handle(SPConfigSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            AdvancedPingDisplay.LOGGER.debug("Server received sync: Freq {}", msg.frequency);
            APDConfig.PING_FREQUENCY.set(msg.frequency);
            APDConfig.PING_TYPE.set(PingType.values()[msg.typeOrdinal]);
            APDConfig.SPEC.save();
        });
        ctx.get().setPacketHandled(true);
    }
}