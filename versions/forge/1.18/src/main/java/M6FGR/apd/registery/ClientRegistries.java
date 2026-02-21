package M6FGR.apd.registery;

import M6FGR.apd.client.gui.screen.APDConfigScreen;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;
public class ClientRegistries {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((mc, lastScreen) ->
                        new APDConfigScreen(lastScreen))
        );

    }
}
