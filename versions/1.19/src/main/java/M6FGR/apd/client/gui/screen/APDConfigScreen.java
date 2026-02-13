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

@OnlyIn(Dist.CLIENT)
public class APDConfigScreen extends Screen {
    private final Screen lastScreen;
    private OptionsList list;

    private OptionInstance<PingType> pingTypeOption;
    private OptionInstance<Double> pingFreq;
    private AbstractWidget pingTypeWid;
    private AbstractWidget pingFreqWid;

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
                    APDConfig.PING_TYPE.set(value);
                    APDConfig.SPEC.save();

                }
        );

        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(this.pingFreq, this.pingTypeOption);
        this.addWidget(this.list);

        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;
        boolean modOnServer = ServerAccessor.isModOnServer();
        boolean inWorld = Minecraft.getInstance().level != null;

        this.pingTypeWid = this.list.findOption(this.pingTypeOption);
        this.pingFreqWid = this.list.findOption(this.pingFreq);

        if (pingTypeWid != null && (isSingleplayer || isIncompatible)) {
            pingTypeWid.active = false;
        }

        if (pingFreqWid != null && (isSingleplayer || !modOnServer || !inWorld)) {
            pingFreqWid.active = false;
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

        if (mouseY >= 32 && mouseY <= this.height - 32) {
            int centerX = this.width / 2;
            int listTop = 32;
            int rowHeight = 25;
            double scrollAmount = this.list.getScrollAmount();

            int hoveredIndex = (int) ((mouseY - listTop + scrollAmount) / rowHeight);

            if (hoveredIndex >= 0 && hoveredIndex < this.list.children().size()) {
                net.minecraft.network.chat.MutableComponent tooltip = null;
                boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();

                boolean isLeftColumn = mouseX >= centerX - 160 && mouseX <= centerX - 10;
                boolean isRightColumn = mouseX >= centerX + 10 && mouseX <= centerX + 160;

                if (hoveredIndex == 0) {
                    if (isLeftColumn) {
                        if (isSingleplayer) {
                            tooltip = Component.literal("Disabled in Singleplayer!").withStyle(net.minecraft.ChatFormatting.RED);
                        } else if (!ServerAccessor.isModOnServer()) {
                            tooltip = Component.literal("Requires mod on the server to change frequency!").withStyle(net.minecraft.ChatFormatting.RED);
                        } else {
                            tooltip = Component.literal("Adjust ping update rate in seconds.");
                        }
                    } else if (isRightColumn) { // Ping Type (Second in addSmall)
                        if (isSingleplayer) {
                            tooltip = Component.literal("Disabled in Singleplayer!").withStyle(net.minecraft.ChatFormatting.RED);
                        } else if (AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD) {
                            String mods = String.join(", ", AdvancedPingDisplay.incompatibleMods);
                            tooltip = Component.literal("Incompatible Mods!: " + mods).withStyle(net.minecraft.ChatFormatting.RED);
                        } else {
                            tooltip = Component.literal("Normal: Standard ping.\nPacket: Local packet count.");
                        }
                    }
                }

                if (tooltip != null) {
                    this.renderTooltip(poseStack, this.font.split(tooltip, 200), mouseX, mouseY);
                }
            }
        }
    }
}