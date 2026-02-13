package M6FGR.apd.config;

import M6FGR.apd.api.enums.PingType;
import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

public class APDConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();;
    public static final ForgeConfigSpec.DoubleValue PING_FREQUENCY;
    public static final ForgeConfigSpec.EnumValue<PingType> PING_TYPE;
    public static final ForgeConfigSpec SPEC;

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

    public static void reloadConfig() {
        APDConfig.SPEC.acceptConfig((CommentedConfig) ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.COMMON));
    }

}
