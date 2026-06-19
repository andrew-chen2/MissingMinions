package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class CollectionsApi {
    public record CollectionReq(String collectionId, int tier) {}

    // Maps item ID (*_GENERATOR_*) prefix to collection requirement
    public static final Map<String, CollectionReq> MINION_REQS = new HashMap<>();

    public static void fetchCollections() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/v2/resources/skyblock/collections"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        JsonObject collections = jsonResponse.getAsJsonObject("collections");

                        for (String category : collections.keySet()) {
                            JsonObject items = collections.getAsJsonObject(category).getAsJsonObject("items");

                            for (String collId : items.keySet()) {
                                JsonArray tiers = items.getAsJsonObject(collId).getAsJsonArray("tiers");

                                for (JsonElement tierEl : tiers) {
                                    JsonObject tierObj = tierEl.getAsJsonObject();
                                    int tierNum = tierObj.get("tier").getAsInt();
                                    JsonArray unlocks = tierObj.getAsJsonArray("unlocks");

                                    for (JsonElement unlockEl : unlocks) {
                                        String unlockStr = unlockEl.getAsString();
                                        if (unlockStr.contains(" Minion Recipes")) {

                                            // Extract prefix
                                            String prefix = getPrefix(unlockStr);

                                            MINION_REQS.put(prefix, new CollectionReq(collId, tierNum));
                                        }
                                    }
                                }
                            }
                        }
                        System.out.println("[MissingMinions] Successfully mapped " + MINION_REQS.size() + " collection requirements!");
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch collections.");
                    return null;
                });
    }

    private static @NonNull String getPrefix(String unlockStr) {
        String prefix = unlockStr.replace(" Minion Recipes", "").toUpperCase().replace(" ", "_");

        prefix = switch (prefix) {
            case "COCOA_BEANS" -> "COCOA";
            case "CAVE_SPIDER" -> "CAVESPIDER";
            case "NETHER_WART" -> "NETHER_WARTS";
            case "END_STONE" -> "ENDER_STONE";
            default -> prefix;
        };
        return prefix;
    }
}