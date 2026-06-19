package com.github.andrewchen2.missingminions.commands;

import com.github.andrewchen2.missingminions.config.ModConfig;
import com.github.andrewchen2.missingminions.gui.MainScreen;
import com.github.andrewchen2.missingminions.network.HypixelApi;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class MMCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("missingminions").executes(MMCommand::execute)
                    .then(ClientCommandManager.literal("toggle")
                            .then(ClientCommandManager.literal("recursive").executes(MMCommand::toggleRecursive))
                            .then(ClientCommandManager.literal("filter").executes(MMCommand::toggleFilter))
                            .then(ClientCommandManager.literal("buyorders").executes(MMCommand::toggleBuyOrders))
                    ));
            dispatcher.register(ClientCommandManager.literal("mm").executes(MMCommand::execute)
                    .then(ClientCommandManager.literal("toggle")
                            .then(ClientCommandManager.literal("recursive").executes(MMCommand::toggleRecursive))
                            .then(ClientCommandManager.literal("filter").executes(MMCommand::toggleFilter))
                            .then(ClientCommandManager.literal("buyorders").executes(MMCommand::toggleBuyOrders))
                    ));
        });
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().setScreen(new MainScreen()));

        // Get player UUID
        String playerUUID = context.getSource().getPlayer().getUuidAsString().replace("-", "");

        // Fetch data (Bazaar and Auctions, then minions)
        CompletableFuture.allOf(
                com.github.andrewchen2.missingminions.network.BazaarApi.fetchBazaarData(),
                com.github.andrewchen2.missingminions.network.AuctionApi.fetchLowestBin()
        ).thenRun(() -> HypixelApi.fetchData(playerUUID));

        return 1; // Successful command execution
    }

    private static int toggleRecursive(CommandContext<FabricClientCommandSource> context) {
        boolean current = ModConfig.useRecursiveCost;
        ModConfig.setUseRecursiveCost(!current);

        String state = ModConfig.useRecursiveCost ? "§aON" : "§cOFF";
        context.getSource().sendFeedback(Text.literal("§9[MissingMinions] §7Recursive Cost Calculation is now: " + state));

        return 1;
    }

    private static int toggleFilter(CommandContext<FabricClientCommandSource> context) {
        boolean current = ModConfig.filterCollections;
        ModConfig.setFilterCollections(!current);

        String state = ModConfig.filterCollections ? "§aON" : "§cOFF";
        context.getSource().sendFeedback(Text.literal("§9[MissingMinions] §7Collection Filtering is now: " + state));

        return 1;
    }

    private static int toggleBuyOrders(CommandContext<FabricClientCommandSource> context) {
        boolean current = ModConfig.useBuyOrders;
        ModConfig.setUseBuyOrders(!current);

        String state = ModConfig.useBuyOrders ? "§aBUY ORDERS" : "§cINSTA-BUY";
        context.getSource().sendFeedback(net.minecraft.text.Text.literal("§9[MissingMinions] §7Calculation mode set to: " + state));

        return 1;
    }
}
