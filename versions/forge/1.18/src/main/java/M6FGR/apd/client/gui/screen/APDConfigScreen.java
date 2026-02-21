package M6FGR.apd.client.gui.screen;

import M6FGR.apd.api.enums.PingType;
import M6FGR.apd.config.APDConfig;
import M6FGR.apd.main.AdvancedPingDisplay;
import M6FGR.apd.network.packet.SPConfigSync;
import M6FGR.apd.network.protocol.PacketProtocol;
import M6FGR.apd.network.server.ServerAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.CycleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class APDConfigScreen extends Screen {
    private final Screen lastScreen;
    private OptionsList list;

    private ProgressOption pingFreqOption;
    private CycleOption<PingType> pingTypeOption;

    public APDConfigScreen(Screen lastScreen) {
        super(new TranslatableComponent("Advanced Ping Settings"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        // Frequency Slider
        this.pingFreqOption = new ProgressOption(
                "options.ping_frequency",
                0.0, 15.0, 1.0F,
                guiSettings -> (double) APDConfig.PING_FREQUENCY.get(),
                (guiSettings, value) -> {
                    APDConfig.PING_FREQUENCY.set(value);
                },
                (guiSettings, option) -> {
                    double value = APDConfig.PING_FREQUENCY.get();
                    if (value == 0) return new TranslatableComponent("Ping Frequency: Instant");
                    return new TranslatableComponent("Ping Frequency: " + (int)value + "s");
                }
        );

        this.pingTypeOption = CycleOption.create(
                "Ping Type",
                Arrays.asList(PingType.values()),
                (value) -> new TranslatableComponent(value.getName()),
                (guiSettings) -> AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD ? PingType.PACKET : APDConfig.PING_TYPE.get(),
                (guiSettings, option, value) -> {
                    APDConfig.PING_TYPE.set(value);
                }
        );

        this.list = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.list.addSmall(this.pingFreqOption, this.pingTypeOption);
        this.addWidget(this.list);

        boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();
        boolean isIncompatible = AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD;
        boolean modOnServer = ServerAccessor.isModOnServer();
        boolean inWorld = Minecraft.getInstance().level != null;

        AbstractWidget pingFreqWid = this.list.findOption(this.pingFreqOption);
        AbstractWidget pingTypeWid = this.list.findOption(this.pingTypeOption);

        if (pingTypeWid != null && (isSingleplayer || isIncompatible)) {
            pingTypeWid.active = false;
        }

        if (pingFreqWid != null && (isSingleplayer || !modOnServer || !inWorld)) {
            pingFreqWid.active = false;
        }

        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 27, 200, 20, CommonComponents.GUI_DONE, (button) -> {
            double freq = APDConfig.PING_FREQUENCY.get();
            int type = APDConfig.PING_TYPE.get().ordinal();

            if (this.minecraft.getConnection() != null) {
                PacketProtocol.INSTANCE.sendToServer(new SPConfigSync(freq, type));
            }

            APDConfig.SPEC.save();
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        this.list.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTick);

        if (mouseY >= 32 && mouseY <= this.height - 32) {
            int centerX = this.width / 2;
            int hoveredIndex = (int) ((mouseY - 32 + this.list.getScrollAmount()) / 25);

            if (hoveredIndex >= 0 && hoveredIndex < this.list.children().size()) {
                Component tooltip = null;
                boolean isSingleplayer = this.minecraft.getSingleplayerServer() != null || this.minecraft.hasSingleplayerServer();

                boolean isLeftColumn = mouseX >= centerX - 160 && mouseX <= centerX - 10;
                boolean isRightColumn = mouseX >= centerX + 10 && mouseX <= centerX + 160;

                if (hoveredIndex == 0) {
                    if (isLeftColumn) {
                        if (isSingleplayer) {
                            tooltip = new TranslatableComponent("Disabled in Singleplayer!").withStyle(net.minecraft.ChatFormatting.RED);
                        } else if (!ServerAccessor.isModOnServer()) {
                            tooltip = new TranslatableComponent("Requires mod on the server!").withStyle(net.minecraft.ChatFormatting.RED);
                        }
                    } else if (isRightColumn) {
                        if (isSingleplayer) {
                            tooltip = new TranslatableComponent("Disabled in Singleplayer!").withStyle(net.minecraft.ChatFormatting.RED);
                        } else if (AdvancedPingDisplay.HAS_INCOMPATIBLE_MOD) {
                            tooltip = new TranslatableComponent("Incompatible Mods!").withStyle(net.minecraft.ChatFormatting.RED);
                        }
                    }
                }

                if (tooltip != null) {
                    this.renderTooltip(poseStack, tooltip, mouseX, mouseY);
                }
            }
        }
    }
}