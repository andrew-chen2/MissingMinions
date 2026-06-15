package com.github.andrewchen2.missingminions;

import net.fabricmc.api.ClientModInitializer;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingMinions implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("missingminions");

	@Override
	public void onInitializeClient() {
		LOGGER.info("MissingMinions loaded.");

		// Register client-side commands
		ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
			// Main command (/missingminions)
			dispatcher.register(ClientCommandManager.literal("missingminions")
					.executes(this::executeCommand)
			);

			// Alias (/mm)
			dispatcher.register(ClientCommandManager.literal("mm")
					.executes(this::executeCommand)
			);
		}));
	}

	private int executeCommand(CommandContext<FabricClientCommandSource> context) {
		// Send message
		context.getSource().sendFeedback(Component.literal("Testing command."));

		return 1; // Successful command execution
	}
}