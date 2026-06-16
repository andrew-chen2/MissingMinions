package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class HypixelApi {
    // Cloudflare worker
    private static final String PROXY_URL = "https://missingminions.andrewchen796.workers.dev/?uuid=";

    public static void fetchMinions(String uuid) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PROXY_URL + uuid))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        parseMinionData(response.body(), uuid);
                    } else {
                        sendMessage("§cAPI Error: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    sendMessage("§cConnection failed.");
                    return null;
                });
    }

    private static void parseMinionData(String json, String uuid) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(json).getAsJsonObject();

            if (!jsonResponse.has("profiles") || jsonResponse.get("profiles").isJsonNull()) {
                sendMessage("§cNo profiles found for this player.");
                return;
            }

            JsonArray profiles = jsonResponse.getAsJsonArray("profiles");
            if (profiles.isEmpty()) {
                sendMessage("§cPlayer has no Skyblock profiles.");
                return;
            }

            // Find the active profile
            JsonObject activeProfile = null;
            for (JsonElement profileElement : profiles) {
                JsonObject profile = profileElement.getAsJsonObject();
                if (profile.has("selected") && profile.get("selected").getAsBoolean()) {
                    activeProfile = profile;
                    break;
                }
            }

            // Fallback to the first profile
            if (activeProfile == null) {
                activeProfile = profiles.get(0).getAsJsonObject();
            }

            JsonObject members = activeProfile.getAsJsonObject("members");

            if (!members.has(uuid)) {
                sendMessage("§cCould not find player data in profile.");
                return;
            }

            JsonObject playerStats = members.getAsJsonObject(uuid);

            JsonArray craftedGenerators = null;
            if (playerStats.has("crafted_generators")) {
                craftedGenerators = playerStats.getAsJsonArray("crafted_generators");
            } else if (playerStats.has("player_data") && playerStats.getAsJsonObject("player_data").has("crafted_generators")) {
                craftedGenerators = playerStats.getAsJsonObject("player_data").getAsJsonArray("crafted_generators");
            }

            if (craftedGenerators == null || craftedGenerators.isEmpty()) {
                sendMessage("§eYou haven't crafted any minions on this profile yet!");
                return;
            }

            // Get unlocked minions
            List<String> unlockedMinions = new ArrayList<>();

            for (JsonElement minion : craftedGenerators) {
                unlockedMinions.add(minion.getAsString());
            }

            sendMessage("§aSuccessfully parsed §e" + unlockedMinions.size() + " §aunlocked minion tiers!");
        } catch (Exception e) {
            sendMessage("§cError parsing data.");
        }
    }

    // Helper to send messages to the player
    private static void sendMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Push the task back to the main game thread
        client.execute(() -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§b[MissingMinions] §r" + message), false);
            }
        });
    }
}
