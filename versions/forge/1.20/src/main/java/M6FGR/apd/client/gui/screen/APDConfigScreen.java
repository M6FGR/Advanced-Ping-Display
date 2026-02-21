package M6FGR.apd.client.gui.screen;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import M6FGR.apd.network.protocol.PacketProtocol;
import M6FGR.apd.network.server.DedicatedServerDetector;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class APDConfigScreen extends Screen {
    private final Screen lastScreen;
    private OptionsList list;
    private OptionInstance<PingType> pingTypeOption;
    private OptionInstance<Integer> pingFreq;

    public APDConfigScreen(Screen lastScreen) {
        super(Component.literal("Advanced Pinger Settings"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.pingTypeOption = new OptionInstance<>(
                "Ping Type",
                OptionInstance.noTooltip(),
                (label, value) -> Component.literal(value.getName()),
                new OptionInstance.Enum<>(
                        List.of(PingType.NORMAL, PingType.PACKET),
                        Codec.INT.xmap(i -> PingType.values()[i], PingType::ordinal)
                ),
                APDConfig.PING_TYPE.get(),
                (value) -> {
                }
        );

        this.pingFreq = new OptionInstance<>(
                "Ping Frequency",
                OptionInstance.noTooltip(),
                (label, value) -> Component.literal("Ping Frequency: " + value + "s"),
                new OptionInstance.IntRange(0, 15),
                APDConfig.PING_FREQUENCY.get().intValue(),
                (value) -> {
                }
        );

        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(this.pingFreq, this.pingTypeOption);
        this.addRenderableWidget(this.list);

        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;

        AbstractWidget typeBtn = this.list.findOption(this.pingTypeOption);
        String modList = !AdvancedPingDisplay.incompatibleMods.isEmpty() ? String.join(", ", AdvancedPingDisplay.incompatibleMods.stream().iterator().next()) : "";
        if (typeBtn != null && (isSingleplayer || isIncompatible)) {
            typeBtn.active = false;
            typeBtn.setTooltip(Tooltip.create(Component.literal(isSingleplayer ? "§Cannot change in Singleplayer" : "§cIncompatible Mods: " + modList)));
        }
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
          PingType selectedType = this.pingTypeOption.get() != null ? this.pingTypeOption.get() : PingType.NORMAL;
            double freq = this.pingFreq.get().doubleValue();

            APDConfig.PING_TYPE.set(selectedType);
            APDConfig.PING_FREQUENCY.set(freq);
            APDConfig.SPEC.save();

            if (this.minecraft.getConnection() != null) {
                PacketProtocol.INSTANCE.sendToServer(new SPConfigSync(freq, selectedType.ordinal()));
            }

            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void onClose() {
        super.onClose();
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();
    }

    @Override
    public void removed() {
        if (this.pingTypeOption != null && this.pingFreq != null) {
            PingType type = this.pingTypeOption.get();
            double freq = this.pingFreq.get().doubleValue();

            AdvancedPingDisplay.LOGGER.debug("Attempting to save... Type: " + type + " Freq: " + freq);

            APDConfig.PING_TYPE.set(type);
            APDConfig.PING_FREQUENCY.set(freq);

            if (APDConfig.SPEC.isLoaded()) {
                APDConfig.SPEC.save();
                AdvancedPingDisplay.LOGGER.debug("DEBUG: File saved successfully.");
            } else {
                AdvancedPingDisplay.LOGGER.debug("DEBUG: Could not save - Config is NOT loaded (Server Sync active?)");
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}