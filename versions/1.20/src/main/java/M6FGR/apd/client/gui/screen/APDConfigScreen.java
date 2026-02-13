package M6FGR.apd.client.gui.screen;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.server.DedicatedServerDetector;
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

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class APDConfigScreen extends Screen {
    private final Screen lastScreen;
    private OptionsList list;

    private OptionInstance<PingType> pingTypeOption;
    private OptionInstance<Double> pingFreq;

    public APDConfigScreen(Screen lastScreen) {
        super(Component.literal("Advanced Pinger Settings"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.pingFreq = new OptionInstance<>(
                "options.ping_frequency",
                OptionInstance.noTooltip(),
                (label, value) -> {
                    if (value == 0) {
                        return Component.literal("Ping Frequency: Instant");
                    }
                    return Component.literal("Ping Frequency").append(": " + value + "s");
                },
                new OptionInstance.IntRange(0, 15).xmap(
                        (int val) -> (double) val,
                        (Double val) -> val.intValue()
                ),
                APDConfig.PING_FREQUENCY.get(),
                (value) -> {
                    APDConfig.PING_FREQUENCY.set(value);
                    APDConfig.SPEC.save();
                }
        );

        this.pingTypeOption = new OptionInstance<>(
                "Ping Type",
                value -> OptionInstance.cachedConstantTooltip(Component.literal("Normal: Standard ping.\nPacket: Local packet count.")).apply(value),
                (label, value) -> Component.literal("").append(value.getName()),
                new OptionInstance.Enum<>(
                        List.of(PingType.NORMAL, PingType.PACKET),
                        Codec.INT.xmap(i -> PingType.values()[i], PingType::ordinal)
                ),
                AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD ? PingType.PACKET : APDConfig.PING_TYPE.get(),
                (value) -> {
                    if (!AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD) {
                        APDConfig.PING_TYPE.set(value);
                        APDConfig.SPEC.save();
                    }
                }
        );

        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(this.pingFreq, this.pingTypeOption);
        this.addRenderableWidget(this.list);


        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;
        boolean modOnServer = DedicatedServerDetector.isModOnServer();

        AbstractWidget typeBtn = this.list.findOption(this.pingTypeOption);
        AbstractWidget freqBtn = this.list.findOption(this.pingFreq);

        if (typeBtn != null) {
            if (isSingleplayer || isIncompatible) {
                typeBtn.active = false;
                String reason = isSingleplayer ? "§cDisabled in Singleplayer" : "§cIncompatible Mods: " + String.join(", ", AdvancedPingDisplay.incompatibleMods);
                typeBtn.setTooltip(Tooltip.create(Component.literal(reason)));
            }
        }

        if (freqBtn != null) {
            if (isSingleplayer) {
                freqBtn.active = false;
                freqBtn.setTooltip(Tooltip.create(Component.literal("§cDisabled in Singleplayer")));
            } else if (!modOnServer) {
                freqBtn.active = false;
                freqBtn.setTooltip(Tooltip.create(Component.literal("§cRequires mod on server to change frequency")));
            }
        }

        // --- LOGIC UPDATES END HERE ---

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}