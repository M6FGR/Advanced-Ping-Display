package M6FGR.apd.config;

import M6FGR.apd.api.enums.PingType;
import net.neoforged.neoforge.common.ModConfigSpec;

public class APDConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();;
    public static final ModConfigSpec.DoubleValue PING_FREQUENCY;
    public static final ModConfigSpec.EnumValue<PingType> PING_TYPE;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("Advanced Ping Display - Common Settings");

        PING_FREQUENCY = BUILDER
                .comment("How often to ping the server (in seconds).")
                .defineInRange("pingFrequency", 1.0, 0.0, 15.0);

        PING_TYPE = BUILDER
                .comment("NORMAL: Displays actual ping. PACKET: Displays the amount of packets being sent (local-ping).")
                .defineEnum("pingType", PingType.NORMAL);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

}
