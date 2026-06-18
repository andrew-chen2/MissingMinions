package com.github.andrewchen2.missingminions.util;

import com.github.andrewchen2.missingminions.config.ModConfig;
import com.github.andrewchen2.missingminions.network.AuctionApi;
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
    // Cache calculated craft costs (item ID, cost)
    public static final Map<String, Double> COST_CACHE = new HashMap<>();

    // Recipes not found in https://raw.githubusercontent.com/DarthGigi/MinionAH-NotEnoughUpdates-REPO/refs/heads/master/recipes/recipes.json as of 16/06/26
    public static final Map<String, JsonObject> HARDCODED_RECIPES = new java.util.HashMap<>();
    static {
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_1", JsonParser.parseString("{\"A1\": \"WATER_LILY:8\", \"A2\": \"WATER_LILY:8\", \"A3\": \"WATER_LILY:8\", \"B1\": \"WATER_LILY:8\", \"B2\": \"FISHING_ROD:1\", \"B3\": \"WATER_LILY:8\", \"C1\": \"WATER_LILY:8\", \"C2\": \"WATER_LILY:8\", \"C3\": \"WATER_LILY:8\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_1\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_2", JsonParser.parseString("{\"A1\": \"WATER_LILY:16\", \"A2\": \"WATER_LILY:16\", \"A3\": \"WATER_LILY:16\", \"B1\": \"WATER_LILY:16\", \"B2\": \"LILY_PAD_GENERATOR_1:1\", \"B3\": \"WATER_LILY:16\", \"C1\": \"WATER_LILY:16\", \"C2\": \"WATER_LILY:16\", \"C3\": \"WATER_LILY:16\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_2\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_3", JsonParser.parseString("{\"A1\": \"WATER_LILY:32\", \"A2\": \"WATER_LILY:32\", \"A3\": \"WATER_LILY:32\", \"B1\": \"WATER_LILY:32\", \"B2\": \"LILY_PAD_GENERATOR_2:1\", \"B3\": \"WATER_LILY:32\", \"C1\": \"WATER_LILY:32\", \"C2\": \"WATER_LILY:32\", \"C3\": \"WATER_LILY:32\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_3\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_4", JsonParser.parseString("{\"A1\": \"WATER_LILY:64\", \"A2\": \"WATER_LILY:64\", \"A3\": \"WATER_LILY:64\", \"B1\": \"WATER_LILY:64\", \"B2\": \"LILY_PAD_GENERATOR_3:1\", \"B3\": \"WATER_LILY:64\", \"C1\": \"WATER_LILY:64\", \"C2\": \"WATER_LILY:64\", \"C3\": \"WATER_LILY:64\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_4\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_5", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:1\", \"A2\": \"ENCHANTED_WATER_LILY:1\", \"A3\": \"ENCHANTED_WATER_LILY:1\", \"B1\": \"ENCHANTED_WATER_LILY:1\", \"B2\": \"LILY_PAD_GENERATOR_4:1\", \"B3\": \"ENCHANTED_WATER_LILY:1\", \"C1\": \"ENCHANTED_WATER_LILY:1\", \"C2\": \"ENCHANTED_WATER_LILY:1\", \"C3\": \"ENCHANTED_WATER_LILY:1\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_5\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_6", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:4\", \"A2\": \"ENCHANTED_WATER_LILY:4\", \"A3\": \"ENCHANTED_WATER_LILY:4\", \"B1\": \"ENCHANTED_WATER_LILY:4\", \"B2\": \"LILY_PAD_GENERATOR_5:1\", \"B3\": \"ENCHANTED_WATER_LILY:4\", \"C1\": \"ENCHANTED_WATER_LILY:4\", \"C2\": \"ENCHANTED_WATER_LILY:4\", \"C3\": \"ENCHANTED_WATER_LILY:4\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_6\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_7", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:8\", \"A2\": \"ENCHANTED_WATER_LILY:8\", \"A3\": \"ENCHANTED_WATER_LILY:8\", \"B1\": \"ENCHANTED_WATER_LILY:8\", \"B2\": \"LILY_PAD_GENERATOR_6:1\", \"B3\": \"ENCHANTED_WATER_LILY:8\", \"C1\": \"ENCHANTED_WATER_LILY:8\", \"C2\": \"ENCHANTED_WATER_LILY:8\", \"C3\": \"ENCHANTED_WATER_LILY:8\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_7\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_8", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:16\", \"A2\": \"ENCHANTED_WATER_LILY:16\", \"A3\": \"ENCHANTED_WATER_LILY:16\", \"B1\": \"ENCHANTED_WATER_LILY:16\", \"B2\": \"LILY_PAD_GENERATOR_7:1\", \"B3\": \"ENCHANTED_WATER_LILY:16\", \"C1\": \"ENCHANTED_WATER_LILY:16\", \"C2\": \"ENCHANTED_WATER_LILY:16\", \"C3\": \"ENCHANTED_WATER_LILY:16\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_8\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_9", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:32\", \"A2\": \"ENCHANTED_WATER_LILY:32\", \"A3\": \"ENCHANTED_WATER_LILY:32\", \"B1\": \"ENCHANTED_WATER_LILY:32\", \"B2\": \"LILY_PAD_GENERATOR_8:1\", \"B3\": \"ENCHANTED_WATER_LILY:32\", \"C1\": \"ENCHANTED_WATER_LILY:32\", \"C2\": \"ENCHANTED_WATER_LILY:32\", \"C3\": \"ENCHANTED_WATER_LILY:32\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_9\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_10", JsonParser.parseString("{\"A1\": \"ENCHANTED_WATER_LILY:64\", \"A2\": \"ENCHANTED_WATER_LILY:64\", \"A3\": \"ENCHANTED_WATER_LILY:64\", \"B1\": \"ENCHANTED_WATER_LILY:64\", \"B2\": \"LILY_PAD_GENERATOR_9:1\", \"B3\": \"ENCHANTED_WATER_LILY:64\", \"C1\": \"ENCHANTED_WATER_LILY:64\", \"C2\": \"ENCHANTED_WATER_LILY:64\", \"C3\": \"ENCHANTED_WATER_LILY:64\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_10\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_11", JsonParser.parseString("{\"A1\": \"CONDENSED_WATER_LILY:1\", \"A2\": \"CONDENSED_WATER_LILY:1\", \"A3\": \"CONDENSED_WATER_LILY:1\", \"B1\": \"CONDENSED_WATER_LILY:1\", \"B2\": \"LILY_PAD_GENERATOR_10:1\", \"B3\": \"CONDENSED_WATER_LILY:1\", \"C1\": \"CONDENSED_WATER_LILY:1\", \"C2\": \"CONDENSED_WATER_LILY:1\", \"C3\": \"CONDENSED_WATER_LILY:1\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_11\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("LILY_PAD_GENERATOR_12", JsonParser.parseString("{\"A1\": \"\", \"A2\": \"\", \"A3\": \"\", \"B1\": \"\", \"B2\": \"LILY_PAD_GENERATOR_11:1\", \"B3\": \"\", \"C1\": \"\", \"C2\": \"GENERATOR_UPGRADE_STONE_LILY_PAD_12:1\", \"C3\": \"\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"LILY_PAD_GENERATOR_12\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_1", JsonParser.parseString("{\"A1\": \"RED_MUSHROOM:10\", \"A2\": \"RED_MUSHROOM:10\", \"A3\": \"RED_MUSHROOM:10\", \"B1\": \"RED_MUSHROOM:10\", \"B2\": \"WOOD_HOE:1\", \"B3\": \"RED_MUSHROOM:10\", \"C1\": \"RED_MUSHROOM:10\", \"C2\": \"RED_MUSHROOM:10\", \"C3\": \"RED_MUSHROOM:10\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_1\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_2", JsonParser.parseString("{\"A1\": \"RED_MUSHROOM:20\", \"A2\": \"RED_MUSHROOM:20\", \"A3\": \"RED_MUSHROOM:20\", \"B1\": \"RED_MUSHROOM:20\", \"B2\": \"MUSHROOM_GENERATOR_1:1\", \"B3\": \"RED_MUSHROOM:20\", \"C1\": \"RED_MUSHROOM:20\", \"C2\": \"RED_MUSHROOM:20\", \"C3\": \"RED_MUSHROOM:20\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_2\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_3", JsonParser.parseString("{\"A1\": \"RED_MUSHROOM:40\", \"A2\": \"RED_MUSHROOM:40\", \"A3\": \"RED_MUSHROOM:40\", \"B1\": \"RED_MUSHROOM:40\", \"B2\": \"MUSHROOM_GENERATOR_2:1\", \"B3\": \"RED_MUSHROOM:40\", \"C1\": \"RED_MUSHROOM:40\", \"C2\": \"RED_MUSHROOM:40\", \"C3\": \"RED_MUSHROOM:40\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_3\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_4", JsonParser.parseString("{\"A1\": \"RED_MUSHROOM:64\", \"A2\": \"RED_MUSHROOM:64\", \"A3\": \"RED_MUSHROOM:64\", \"B1\": \"RED_MUSHROOM:64\", \"B2\": \"MUSHROOM_GENERATOR_3:1\", \"B3\": \"RED_MUSHROOM:64\", \"C1\": \"RED_MUSHROOM:64\", \"C2\": \"RED_MUSHROOM:64\", \"C3\": \"RED_MUSHROOM:64\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_4\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_5", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:1\", \"A2\": \"ENCHANTED_RED_MUSHROOM:1\", \"A3\": \"ENCHANTED_RED_MUSHROOM:1\", \"B1\": \"ENCHANTED_RED_MUSHROOM:1\", \"B2\": \"MUSHROOM_GENERATOR_4:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:1\", \"C1\": \"ENCHANTED_RED_MUSHROOM:1\", \"C2\": \"ENCHANTED_RED_MUSHROOM:1\", \"C3\": \"ENCHANTED_RED_MUSHROOM:1\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_5\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_6", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:2\", \"A2\": \"ENCHANTED_RED_MUSHROOM:2\", \"A3\": \"ENCHANTED_RED_MUSHROOM:2\", \"B1\": \"ENCHANTED_RED_MUSHROOM:2\", \"B2\": \"MUSHROOM_GENERATOR_5:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:2\", \"C1\": \"ENCHANTED_RED_MUSHROOM:2\", \"C2\": \"ENCHANTED_RED_MUSHROOM:2\", \"C3\": \"ENCHANTED_RED_MUSHROOM:2\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_6\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_7", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:4\", \"A2\": \"ENCHANTED_RED_MUSHROOM:4\", \"A3\": \"ENCHANTED_RED_MUSHROOM:4\", \"B1\": \"ENCHANTED_RED_MUSHROOM:4\", \"B2\": \"MUSHROOM_GENERATOR_6:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:4\", \"C1\": \"ENCHANTED_RED_MUSHROOM:4\", \"C2\": \"ENCHANTED_RED_MUSHROOM:4\", \"C3\": \"ENCHANTED_RED_MUSHROOM:4\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_7\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_8", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:8\", \"A2\": \"ENCHANTED_RED_MUSHROOM:8\", \"A3\": \"ENCHANTED_RED_MUSHROOM:8\", \"B1\": \"ENCHANTED_RED_MUSHROOM:8\", \"B2\": \"MUSHROOM_GENERATOR_7:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:8\", \"C1\": \"ENCHANTED_RED_MUSHROOM:8\", \"C2\": \"ENCHANTED_RED_MUSHROOM:8\", \"C3\": \"ENCHANTED_RED_MUSHROOM:8\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_8\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_9", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:16\", \"A2\": \"ENCHANTED_RED_MUSHROOM:16\", \"A3\": \"ENCHANTED_RED_MUSHROOM:16\", \"B1\": \"ENCHANTED_RED_MUSHROOM:16\", \"B2\": \"MUSHROOM_GENERATOR_8:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:16\", \"C1\": \"ENCHANTED_RED_MUSHROOM:16\", \"C2\": \"ENCHANTED_RED_MUSHROOM:16\", \"C3\": \"ENCHANTED_RED_MUSHROOM:16\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_9\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_10", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:32\", \"A2\": \"ENCHANTED_RED_MUSHROOM:32\", \"A3\": \"ENCHANTED_RED_MUSHROOM:32\", \"B1\": \"ENCHANTED_RED_MUSHROOM:32\", \"B2\": \"MUSHROOM_GENERATOR_9:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:32\", \"C1\": \"ENCHANTED_RED_MUSHROOM:32\", \"C2\": \"ENCHANTED_RED_MUSHROOM:32\", \"C3\": \"ENCHANTED_RED_MUSHROOM:32\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_10\"}").getAsJsonObject());
        HARDCODED_RECIPES.put("MUSHROOM_GENERATOR_11", JsonParser.parseString("{\"A1\": \"ENCHANTED_RED_MUSHROOM:64\", \"A2\": \"ENCHANTED_RED_MUSHROOM:64\", \"A3\": \"ENCHANTED_RED_MUSHROOM:64\", \"B1\": \"ENCHANTED_RED_MUSHROOM:64\", \"B2\": \"MUSHROOM_GENERATOR_10:1\", \"B3\": \"ENCHANTED_RED_MUSHROOM:64\", \"C1\": \"ENCHANTED_RED_MUSHROOM:64\", \"C2\": \"ENCHANTED_RED_MUSHROOM:64\", \"C3\": \"ENCHANTED_RED_MUSHROOM:64\", \"type\": \"crafting\", \"count\": 1, \"overrideOutputId\": \"MUSHROOM_GENERATOR_11\"}").getAsJsonObject());
    }

    public static double calculateRecipeCost(JsonObject recipe, String craftItemId) {
        double totalCost = 0.0;

        // Crafting table slots
        String[] slots = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};

        for (String slot : slots) {
            if (recipe.has(slot) && !recipe.get(slot).getAsString().isEmpty()) {
                String itemData = recipe.get(slot).getAsString();

                // Parsing
                int splitIdx = Math.max(itemData.lastIndexOf(':'), itemData.lastIndexOf(';'));

                String itemId = itemData;
                int amount = 1;

                // Parse if there is a separator (; or :)
                if (splitIdx != -1) {
                    try {
                        amount = Integer.parseInt(itemData.substring(splitIdx + 1));
                        itemId = itemData.substring(0, splitIdx);
                    } catch (NumberFormatException ignored) {}
                }

                // Some tier 1 minions require a (different type of) minion to craft
                if (itemId.contains("_GENERATOR_") && !craftItemId.endsWith("_GENERATOR_1")) {
                    // Only calculate (previous minion tiers) recursively if config option is enabled
                    if (ModConfig.useRecursiveCost) {
                        totalCost += calculateItemCost(itemId);
                    }
                } else {
                    double buyPrice = BazaarApi.getBuyPrice(itemId);
                    if (buyPrice != 0.0) {
                        totalCost += (buyPrice * amount);
                        continue;
                    }

                    // Try calculating cost using the item's recipe
                    double craftPrice = calculateItemCost(itemId);
                    if (craftPrice != 0.0) {
                        totalCost += (craftPrice * amount);
                        continue;
                    }

                    // Finally fallback to lowest BIN price
                    double auctionPrice = AuctionApi.getLowestBinPrice(itemId);
                    if (auctionPrice != 0.0) {
                        totalCost += (auctionPrice * amount);
                    }
                }
            }
        }

        return totalCost;
    }

    // Helper to recursively fetch the previous tier's recipe
    public static double calculateItemCost(String itemId) {
        // Skip Tier 12 for now
        if (itemId.contains("12")) return 0.0;

        // Check cache
        if (COST_CACHE.containsKey(itemId)) {
            return COST_CACHE.get(itemId);
        }

        // Hardcoded uncraftable minions
        if (itemId.equals("FLOWER_GENERATOR_1")) {
            double cost = 2_500_000.0;
            COST_CACHE.put(itemId, cost);
            return cost;
        }
        if (itemId.equals("SNOW_GENERATOR_1")) {
            double cost = 600_000.0;
            COST_CACHE.put(itemId, cost);
            return cost;
        }

        double cost = 0.0;

        if (RecipesApi.RECIPES != null && RecipesApi.RECIPES.has(itemId)) {
            try {
                JsonObject recipeObj = RecipesApi.RECIPES.getAsJsonObject(itemId).getAsJsonObject("recipe");
                cost = calculateRecipeCost(recipeObj, itemId);
            } catch (Exception e) {
                System.err.println("Failed to parse bulk recipe for: " + itemId);
            }
        } else if (HARDCODED_RECIPES.containsKey(itemId)) {
            try {
                System.out.println("Falling back to hardcoded recipe for: " + itemId);
                cost = calculateRecipeCost(HARDCODED_RECIPES.get(itemId), itemId);
            } catch (Exception e) {
                System.err.println("Failed to parse hardcoded recipe for: " + itemId);
            }
        } else {
            try {
                System.out.println("Falling back to individual fetch for: " + itemId);
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + itemId + ".json"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject itemJson = JsonParser.parseString(response.body()).getAsJsonObject();
                    // If an item has multiple recipes, they will be in a "recipes" array
                    if (itemJson.has("recipe")) {
                        cost = calculateRecipeCost(itemJson.getAsJsonObject("recipe"), itemId);
                    } else if (itemJson.has("recipes")) {
                        for (JsonElement recipeElement : itemJson.getAsJsonArray("recipes")) {
                            JsonObject recipeObj = recipeElement.getAsJsonObject();
                            if (recipeObj.has("type") && recipeObj.get("type").getAsString().equals("crafting")) {
                                cost = calculateRecipeCost(recipeObj, itemId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch recipe for: " + itemId);
            }
        }

        COST_CACHE.put(itemId, cost);
        return cost;
    }
}