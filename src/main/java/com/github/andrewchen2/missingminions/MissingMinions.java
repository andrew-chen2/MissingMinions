package com.github.andrewchen2.missingminions;

import net.fabricmc.api.ClientModInitializer;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;
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

					if (client.player != null && client.textRenderer != null && isOnPrivateIsland()) {
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

	private boolean isOnPrivateIsland() {
		return getZone().equals("Your Island");
	}

	private String getZone() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) return "";

		Scoreboard scoreboard = client.world.getScoreboard();

		ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
		if (objective == null) return "";

		// Iterate through scoreboard lines
		for (Team team : scoreboard.getTeams()) {
			for (String entry : team.getPlayerList()) {
				// Construct full line
				Text prefix = team.getPrefix();
				Text suffix = team.getSuffix();
				String fullLine = prefix.getString() + entry + suffix.getString();

				// Zone (island) line starts with a "⏣"
				if (fullLine.contains("§7⏣")) {
					// Remove formatting codes
					String cleanLine = fullLine.replaceAll("(?i)§[0-9A-Z]", "");
					return cleanLine.replace("⏣", "").strip();
				}
			}
		}

		return "";
	}
}