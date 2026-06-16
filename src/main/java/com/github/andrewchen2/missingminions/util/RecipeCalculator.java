package com.github.andrewchen2.missingminions.util;

import com.github.andrewchen2.missingminions.network.BazaarApi;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RecipeCalculator {
    public static double calculateRecipeCost(JsonObject recipe) {
        double totalCost = 0.0;

        // Crafting table slots
        String[] slots = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};

        for (String slot : slots) {
            if (recipe.has(slot) && !recipe.get(slot).getAsString().isEmpty()) {
                String itemData = recipe.get(slot).getAsString();

                // Format is ITEM_ID:AMOUNT
                String[] parts = itemData.split(":");
                String itemId = parts[0];
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

                if (itemId.contains("_GENERATOR_")) {
                    totalCost += fetchAndCalculateMinionCost(itemId);
                } else {
                    totalCost += (BazaarApi.getBuyPrice(itemId) * amount);
                }
            }
        }

        return totalCost;
    }

    // Helper to recursively fetch the previous tier's recipe
    public static double fetchAndCalculateMinionCost(String minionId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + minionId + ".json"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject itemJson = JsonParser.parseString(response.body()).getAsJsonObject();
                // If an item has multiple recipes, they will be in a "recipes" array, but minions only ever have one recipe
                if (itemJson.has("recipe")) {
                    return calculateRecipeCost(itemJson.getAsJsonObject("recipe"));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch recursive minion recipe: " + minionId);
        }
        return 0.0;
    }
}