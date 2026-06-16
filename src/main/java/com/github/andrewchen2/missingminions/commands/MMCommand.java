package com.github.andrewchen2.missingminions.commands;

import com.github.andrewchen2.missingminions.network.BazaarApi;
import com.github.andrewchen2.missingminions.network.HypixelApi;
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
        });
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.literal("§b[MissingMinions] §7Fetching profile data..."));

        // Get player UUID
        String playerUUID = context.getSource().getPlayer().getUuidAsString().replace("-", "");

        // Fetch data (Bazaar, then minions)
        BazaarApi.fetchBazaarData()
                .thenRun(() -> HypixelApi.fetchMinions(playerUUID));

        return 1; // Successful command execution
    }
}
