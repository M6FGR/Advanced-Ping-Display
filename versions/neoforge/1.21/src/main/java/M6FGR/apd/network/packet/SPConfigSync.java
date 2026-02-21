package M6FGR.apd.network.packet;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record SPConfigSync(double frequency, int typeOrdinal) implements CustomPacketPayload {
    public static final Type<SPConfigSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AdvancedPingDisplay.MODID, "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SPConfigSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SPConfigSync::frequency,
            ByteBufCodecs.VAR_INT, SPConfigSync::typeOrdinal,
            SPConfigSync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public static void handle(final SPConfigSync payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            AdvancedPingDisplay.LOGGER.debug("Server received sync: Freq {}", payload.frequency());

            APDConfig.PING_FREQUENCY.set(payload.frequency());

            if (payload.typeOrdinal() >= 0 && payload.typeOrdinal() < PingType.values().length) {
                APDConfig.PING_TYPE.set(PingType.values()[payload.typeOrdinal()]);
            }

            APDConfig.SPEC.save();
        });
    }
}