package com.github.andrewchen2.missingminions;

import net.fabricmc.api.ClientModInitializer;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
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

		// Register HUD overlay
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.CHAT,
				Identifier.of("missingminions", "status_text"),
				(context, tickCounter) -> {
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.player != null && client.textRenderer != null) {
						String hudText = "Placeholder text (MissingMinions)";
						int x = 10;
						int y = 10;

						// Draw text
						context.drawText(client.textRenderer, hudText, x, y, Colors.WHITE, true);
					}
				}
		);
	}

	private int executeCommand(CommandContext<FabricClientCommandSource> context) {
		// Send message
		context.getSource().sendFeedback(Text.literal("Placeholder text (MissingMinions)"));

		return 1; // Successful command execution
	}
}