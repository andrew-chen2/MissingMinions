package com.github.andrewchen2.missingminions.hud;

import com.github.andrewchen2.missingminions.util.ZoneDetector;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

public class MainHud {
    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of("missingminions", "status_text"),
                (context, tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    if (client.player != null && client.textRenderer != null && ZoneDetector.isOnPrivateIsland()) {
                        String hudText = "Placeholder text (MissingMinions)";
                        int x = 10;
                        int y = 10;

                        // Draw text
                        context.drawText(client.textRenderer, hudText, x, y, Colors.WHITE, true);
                    }
                }
        );
    }
}
