package com.github.andrewchen2.missingminions.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatUtils {
    public static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Push to the main thread to prevent OpenGL rendering crashes
        client.execute(() -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§9[MissingMinions] §r" + message), false);
            }
        });
    }
}
