package M6FGR.apd.main;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@SuppressWarnings({"removal"})
@Mod(AdvancedPingDisplay.MODID)
public class AdvancedPingDisplay {
    public static final String MODID = "adp";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static boolean HAS_TARGET_MOD = false;

    public AdvancedPingDisplay() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isFromServer) -> true
                )
        );
        ModDetector.checkMod("embeddium");
    }

    public static class ModDetector {
        public static void checkMod(String modid) {
            if (ModList.get().isLoaded(modid)) {
                HAS_TARGET_MOD = true;
            }
        }
    }
}
