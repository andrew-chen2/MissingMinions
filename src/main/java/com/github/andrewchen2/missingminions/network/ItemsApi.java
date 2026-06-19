package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class ItemsApi {
    public record MinionData(String displayName, @Nullable String textureValue) {}

    // Maps minion ID to human-readable name
    public static final Map<String, MinionData> MINIONS = new HashMap<>();

    public static void fetchItems() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/v2/resources/skyblock/items"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                            JsonArray items = jsonResponse.getAsJsonArray("items");

                            MINIONS.clear();

                            for (JsonElement itemElement : items) {
                                JsonObject itemObj = itemElement.getAsJsonObject();
                                String id = itemObj.get("id").getAsString();

                                if (id.contains("_GENERATOR_")) {
                                    String name = itemObj.has("name") ? itemObj.get("name").getAsString() : id;
                                    String texture = null;

                                    if (itemObj.has("skin") && itemObj.getAsJsonObject("skin").has("value")) {
                                        texture = itemObj.getAsJsonObject("skin").get("value").getAsString();
                                    }

                                    MINIONS.put(id, new MinionData(name, texture));
                                }
                            }
                            System.out.println("[MissingMinions] Successfully cached " + MINIONS.size() + " minions!");
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch Master Items list.");
                    return null;
                });
    }
}