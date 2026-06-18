package com.github.andrewchen2.missingminions.network;

import com.github.andrewchen2.missingminions.config.ModConfig;
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
    public static class PricePair {
        public double instaBuy;
        public double buyOrder;

        public PricePair(double instaBuy, double buyOrder) {
            this.instaBuy = instaBuy;
            this.buyOrder = buyOrder;
        }
    }

    // Maps item ID to (instant) buy price
    public static final Map<String, PricePair> BAZAAR_PRICES = new HashMap<>();

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

                            // Save only insta-buy and buy order prices
                            for (String itemId : products.keySet()) {
                                JsonObject quickStatus = products.getAsJsonObject(itemId).getAsJsonObject("quick_status");

                                double instaBuy = quickStatus.get("buyPrice").getAsDouble(); // Insta-buy price
                                double buyOrder = quickStatus.get("sellPrice").getAsDouble(); // Buy order price

                                BAZAAR_PRICES.put(itemId, new PricePair(instaBuy, buyOrder));
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
        PricePair prices = BAZAAR_PRICES.get(cleanId);

        if (prices == null) {
            return 0.0;
        }

        return ModConfig.useBuyOrders ? prices.buyOrder : prices.instaBuy;
    }
}
