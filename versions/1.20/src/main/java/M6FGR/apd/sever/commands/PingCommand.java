package M6FGR.apd.sever.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PingCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ping")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    int ping = player.latency;

                    String message = (ping < 0)
                            ? "§cPing is still loading..."
                            : "§aYour ping is: §f" + ping + "ms";

                    context.getSource().sendSuccess(() -> Component.literal(message), false);
                    return 1;
                })
        );
    }
}
