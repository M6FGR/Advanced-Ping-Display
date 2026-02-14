package M6FGR.apd.main;

import M6FGR.apd.config.APDConfig;
import M6FGR.apd.network.protocol.ModProtocol;
import M6FGR.apd.network.protocol.PacketProtocol;
import M6FGR.apd.registery.ClientRegistries;
import M6FGR.apd.sever.commands.PingCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"removal"})
@Mod(AdvancedPingDisplay.MODID)
public class AdvancedPingDisplay {
    public static final String MODID = "adp";
    public static final Logger LOGGER = LogManager.getLogger("AdvancedPingDisplay");
    public static boolean HAS_INCOMPATIBLE_MOD;
    public static List<String> incompatibleMods = new ArrayList<>();

    public AdvancedPingDisplay() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, APDConfig.SPEC, "apd-common.toml");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isFromServer) -> true
                )
        );
        MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientRegistries::registerConfigScreen);
        ModDetector.checkMod("embeddium");
        modBus.addListener(this::setup);
    }
    private void onCommandsRegister(RegisterCommandsEvent event) {
        PingCommand.register(event.getDispatcher());
    }
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModProtocol::init);
        event.enqueueWork(PacketProtocol::init);
    }

    public static class ModDetector {
        public static void checkMod(String modid) {
            if (ModList.get().isLoaded(modid)) {
                incompatibleMods.add(modid);
                HAS_INCOMPATIBLE_MOD = true;
                LOGGER.warn(modid + " was found!, switching to packet-mode");
            }
        }
    }
}
