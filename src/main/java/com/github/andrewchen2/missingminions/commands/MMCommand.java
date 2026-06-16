package com.github.andrewchen2.missingminions.commands;

import com.github.andrewchen2.missingminions.network.BazaarApi;
import com.github.andrewchen2.missingminions.network.HypixelApi;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class MMCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("missingminions").executes(MMCommand::execute));
            dispatcher.register(ClientCommandManager.literal("mm").executes(MMCommand::execute));
            dispatcher.register(ClientCommandManager.literal("bzp")
                .then(ClientCommandManager.argument("item", StringArgumentType.greedyString())
                    .executes(MMCommand::bzp)
                )
            );
        });
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("§b[MissingMinions] §7Fetching profile data..."));

        // Get player UUID
        String playerUUID = context.getSource().getPlayer().getUuidAsString().replace("-", "");

        // Fetch data
        HypixelApi.fetchMinions(playerUUID);

        return 1; // Successful command execution
    }
    
    private static int bzp(CommandContext<FabricClientCommandSource> context) {
        String item = StringArgumentType.getString(context, "item");
        double price = BazaarApi.getBuyPrice(item);

        context.getSource().sendFeedback(Text.literal(String.valueOf(price)));
        
        return 1;
    }
}
