package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ItemsApi {
    // List of minions (generators)
    public static final List<String> MASTER_MINION_LIST = new ArrayList<>();

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

                            MASTER_MINION_LIST.clear();

                            for (JsonElement itemElement : items) {
                                String id = itemElement.getAsJsonObject().get("id").getAsString();
                                if (id.contains("_GENERATOR_")) {
                                    MASTER_MINION_LIST.add(id);
                                }
                            }
                            System.out.println("[MissingMinions] Successfully cached " + MASTER_MINION_LIST.size() + " minion IDs!");
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch Master Items list.");
                    return null;
                });
    }
}