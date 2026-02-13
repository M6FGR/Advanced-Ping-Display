package M6FGR.apd.client.gui.screen;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.server.ServerAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.config.ConfigTracker;

import java.util.List;
import java.util.Optional;

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
                    if (value == 0) return Component.literal("Ping Frequency: Instant");
                    return Component.literal("Ping Frequency: " + value + "s");
                },
                new OptionInstance.IntRange(0, 15).xmap(
                        Double::valueOf,
                        Double::intValue
                ),
                APDConfig.PING_FREQUENCY.get(),
                (value) -> {
                    APDConfig.PING_FREQUENCY.set(value);
                    APDConfig.SPEC.save();
                }
        );

        this.pingTypeOption = new OptionInstance<>(
                "Ping Type",
                OptionInstance.noTooltip(),
                (label, value) -> Component.nullToEmpty(value.getName()),
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
        this.addWidget(this.list);

        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;
        boolean modOnServer = ServerAccessor.isModOnServer();
        boolean inWorld = Minecraft.getInstance().level != null;

        AbstractWidget typeBtn = this.list.findOption(this.pingTypeOption);
        AbstractWidget freqBtn = this.list.findOption(this.pingFreq);

        if (typeBtn != null && (isSingleplayer || isIncompatible)) {
            typeBtn.active = false;
        }

        if (freqBtn != null && (isSingleplayer || !modOnServer || !inWorld)) {
            freqBtn.active = false;
        }

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.lastScreen);
            APDConfig.save();
            this.onClose();
        }));
    }

    @Override
    public void onClose() {
        super.onClose();
        ConfigTracker.INSTANCE.loadDefaultServerConfigs();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);

        Optional<AbstractWidget> hovered = this.list.getMouseOver(mouseX, mouseY);
        if (hovered.isPresent()) {
            AbstractWidget widget = hovered.get();
            String modList = "";
            if (!AdvancedPingDisplay.incompatibleMods.isEmpty()) {
                modList = String.join(", ", AdvancedPingDisplay.incompatibleMods.stream().iterator().next());
            }
            if (widget == this.list.findOption(this.pingTypeOption)) {
                boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
                String text = isSingleplayer ? "§cDisabled in Singleplayer" : (AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD ? "§cIncompatible Mods: " + modList : "Normal: Standard ping.\nPacket: Local packet count.");
                this.renderTooltip(poseStack, this.font.split(Component.literal(text), 200), mouseX, mouseY);
            } else if (widget == this.list.findOption(this.pingFreq)) {
                boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
                String text = isSingleplayer ? "§cDisabled in Singleplayer" : (!ServerAccessor.isModOnServer() ? "§cRequires mod on server" : "Adjust update rate.");
                this.renderTooltip(poseStack, this.font.split(Component.literal(text), 200), mouseX, mouseY);
            }
        }
    }
}