package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BazaarApi {
    // Maps item ID to (instant) buy price
    public static final Map<String, Double> BAZAAR_PRICES = new HashMap<>();

    public static CompletableFuture<Void> fetchBazaarData() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/v2/skyblock/bazaar"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                        if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                            JsonObject products = jsonResponse.getAsJsonObject("products");

                            BAZAAR_PRICES.clear();

                            // Save only the buy price
                            for (String itemId : products.keySet()) {
                                double buyPrice = products.getAsJsonObject(itemId)
                                        .getAsJsonObject("quick_status")
                                        .get("buyPrice").getAsDouble();

                                BAZAAR_PRICES.put(itemId, buyPrice);
                            }
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch Bazaar data.");
                    return null;
                });
    }

    public static double getBuyPrice(String itemId) {
        String cleanId = itemId.replace("-", ":");
        return BAZAAR_PRICES.getOrDefault(cleanId, 0.0);
    }
}
