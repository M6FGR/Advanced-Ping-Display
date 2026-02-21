package M6FGR.apd.client.gui.screen;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class APDConfigScreen extends OptionsSubScreen {
    private final Screen lastScreen;
    private OptionsList list;

    private OptionInstance<PingType> pingTypeOption;
    private OptionInstance<Integer> pingFreq;


    public APDConfigScreen(@Nullable ModContainer container, Screen lastScreen) {
        super(lastScreen, lastScreen.getMinecraft().options, Component.literal("Advanced Pinger Settings"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void addContents() {
        // 1. Initialize Option Instances
        this.pingTypeOption = new OptionInstance<>(
                "Ping Type",
                OptionInstance.noTooltip(),
                (label, value) -> Component.literal(value.getName()),
                new OptionInstance.Enum<>(
                        List.of(PingType.NORMAL, PingType.PACKET),
                        Codec.INT.xmap(i -> PingType.values()[i], PingType::ordinal)
                ),
                APDConfig.PING_TYPE.get(),
                (value) -> { /* Applied logic if needed */ }
        );

        this.pingFreq = new OptionInstance<>(
                "Ping Frequency",
                OptionInstance.noTooltip(),
                (label, value) -> Component.literal(value == 0 ? "Ping Frequency: Instant" : "Ping Frequency: " + value + "s"),
                new OptionInstance.IntRange(0, 15),
                APDConfig.PING_FREQUENCY.get().intValue(),
                (value) -> { /* Applied logic if needed */ }
        );

        this.list = new OptionsList(this.minecraft, this.width, this);
        this.list.addSmall(this.pingFreq, this.pingTypeOption);

        this.layout.addToContents(this.list);

        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null;
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;

        AbstractWidget typeBtn = this.list.findOption(this.pingTypeOption);
        if (typeBtn != null && (isSingleplayer || isIncompatible)) {
            typeBtn.active = false;
            String mods = String.join(", ", AdvancedPingDisplay.incompatibleMods);
            typeBtn.setTooltip(Tooltip.create(Component.literal(isSingleplayer ? "Cannot change in Singleplayer" : "Incompatible Mods: " + mods)));
        }
    }

    @Override
    protected void addOptions() {
        this.list = new OptionsList(this.minecraft, this.width, this);

        this.list.addSmall(this.pingFreq, this.pingTypeOption);

        this.layout.addToContents(this.list);
    }

    @Override
    protected void addFooter() {
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> saveAndClose()).width(200).build());
    }

    @Override
    protected void repositionElements() {
        // Call parent to arrange the header/footer layout
        super.repositionElements();
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }
    }

    private void saveAndClose() {
        // Update values in memory
        APDConfig.PING_TYPE.set(this.pingTypeOption.get());
        APDConfig.PING_FREQUENCY.set(this.pingFreq.get().doubleValue());

        // Save to file
        APDConfig.SPEC.save();

        // Sync with server if connected
        if (this.minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new SPConfigSync(this.pingFreq.get().doubleValue(), this.pingTypeOption.get().ordinal()));
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void removed() {
        // Final safety save
        APDConfig.PING_TYPE.set(this.pingTypeOption.get());
        APDConfig.PING_FREQUENCY.set(this.pingFreq.get().doubleValue());
        APDConfig.SPEC.save();
    }
}