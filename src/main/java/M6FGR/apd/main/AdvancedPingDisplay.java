package M6FGR.apd.main;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@SuppressWarnings({"removal"})
@Mod(AdvancedPingDisplay.MODID)
public class AdvancedPingDisplay {
    public static final String MODID = "adp";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedPingDisplay() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> NetworkConstants.IGNORESERVERONLY,
                        (remoteVersion, isFromServer) -> true
                )
        );
    }
}
