package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BazaarApi {
    // JSON object with all Bazaar prices
    public static JsonObject BAZAAR_DATA = null;

    public static void fetchBazaarData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/v2/skyblock/bazaar"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (jsonResponse.get("success").getAsBoolean()) {
                            BAZAAR_DATA = jsonResponse.getAsJsonObject("products");
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch Bazaar data.");
                    return null;
                });
    }

    // Helper method to get buy price of an item
    public static double getBuyPrice(String itemId) {
        if (BAZAAR_DATA == null) return 0.0;

        String cleanId = itemId.replace("-", ":");

        if (BAZAAR_DATA.has(cleanId)) {
            return BAZAAR_DATA.getAsJsonObject(cleanId)
                    .getAsJsonObject("quick_status")
                    .get("buyPrice").getAsDouble();
        }
        return 0.0;
    }
}
