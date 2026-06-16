package com.github.andrewchen2.missingminions.util;

import com.github.andrewchen2.missingminions.network.BazaarApi;
import com.github.andrewchen2.missingminions.network.RecipesApi;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class RecipeCalculator {
    // Cache calculated minion costs (minion ID, cost)
    public static final Map<String, Double> COST_CACHE = new HashMap<>();

    public static double calculateRecipeCost(JsonObject recipe) {
        double totalCost = 0.0;

        // Crafting table slots
        String[] slots = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};

        for (String slot : slots) {
            if (recipe.has(slot) && !recipe.get(slot).getAsString().isEmpty()) {
                String itemData = recipe.get(slot).getAsString();

                // Format is ITEM_ID:AMOUNT (bulk recipes use ; instead of :)
                String[] parts = itemData.split("[:;]");
                String itemId = parts[0];
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

                if (itemId.contains("_GENERATOR_")) {
                    totalCost += calculateMinionCost(itemId);
                } else {
                    totalCost += (BazaarApi.getBuyPrice(itemId) * amount);
                }
            }
        }

        return totalCost;
    }

    // Helper to recursively fetch the previous tier's recipe
    public static double calculateMinionCost(String minionId) {
        // Skip Tier 12 for now
        if (minionId.contains("12")) return 0.0;

        // Check cache
        if (COST_CACHE.containsKey(minionId)) {
            return COST_CACHE.get(minionId);
        }

        double cost = 0.0;

        if (RecipesApi.RECIPES != null && RecipesApi.RECIPES.has(minionId)) {
            try {
                JsonObject recipeObj = RecipesApi.RECIPES.getAsJsonObject(minionId).getAsJsonObject("recipe");
                cost = calculateRecipeCost(recipeObj);
            } catch (Exception e) {
                System.err.println("Failed to parse bulk recipe for: " + minionId);
            }
        }

        try {
            System.out.println("Falling back to individual fetch for: " + minionId);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + minionId + ".json"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject itemJson = JsonParser.parseString(response.body()).getAsJsonObject();
                // If an item has multiple recipes, they will be in a "recipes" array
                if (itemJson.has("recipe")) {
                    cost = calculateRecipeCost(itemJson.getAsJsonObject("recipe"));
                } else if (itemJson.has("recipes")) {
                    for (JsonElement recipeElement : itemJson.getAsJsonArray("recipes")) {
                        JsonObject recipeObj = recipeElement.getAsJsonObject();
                        if (recipeObj.has("type") && recipeObj.get("type").getAsString().equals("crafting")) {
                            cost = calculateRecipeCost(recipeObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch minion recipe for: " + minionId);
        }

        COST_CACHE.put(minionId, cost);
        return cost;
    }
}