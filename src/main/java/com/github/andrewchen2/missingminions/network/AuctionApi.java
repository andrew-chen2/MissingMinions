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

public class AuctionApi {
    // Maps item ID to lowest BIN price
    public static final Map<String, Double> LOWEST_BIN = new HashMap<>();

    public static CompletableFuture<Void> fetchLowestBin() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                // https://api.skytils.gg/api/auctions/lowestbins
                .uri(URI.create("https://lb.tricked.pro/lowestbins"))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();

                        LOWEST_BIN.clear();
                        for (String itemId : jsonResponse.keySet()) {
                            LOWEST_BIN.put(itemId, jsonResponse.get(itemId).getAsDouble());
                        }
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch AH data.");
                    return null;
                });
    }

    public static double getLowestBinPrice(String itemId) {
        String cleanId = itemId.replace("-", ":");
        return LOWEST_BIN.getOrDefault(cleanId, 0.0);
    }
}