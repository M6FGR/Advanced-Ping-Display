package M6FGR.apd.registery;

import M6FGR.apd.client.gui.screen.APDConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
@SuppressWarnings("removal")
public class ClientRegistries {
    public static void registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, lastScreen) ->
                        new APDConfigScreen(lastScreen))
        );

    }
}
