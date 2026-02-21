package M6FGR.apd.main;

import M6FGR.apd.client.gui.screen.APDConfigScreen;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.network.protocol.ModProtocol;
import M6FGR.apd.sever.commands.PingCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(AdvancedPingDisplay.MODID)
public class AdvancedPingDisplay {
    public static final String MODID = "apd";
    public static final Logger LOGGER = LogManager.getLogger("AdvancedPingDisplay");
    public static boolean HAS_INCOMPATIBLE_MOD;
    public static List<String> incompatibleMods = new ArrayList<>();
    private static final boolean CLIENT = FMLLoader.getDist().isClient();

    public AdvancedPingDisplay(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, APDConfig.SPEC);
        if (isClient()) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, APDConfigScreen::new);
        }
        NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
    }
    private void onCommandsRegister(RegisterCommandsEvent event) {
        PingCommand.register(event.getDispatcher());
    }

    public static boolean isClient() {
        return CLIENT;
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
