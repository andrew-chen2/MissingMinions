package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RecipesApi {
    // Maps item ID to its recipe
    public static final Map<String, JsonObject> RECIPES = new HashMap<>();

    public static void fetchRecipes() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://raw.githubusercontent.com/DarthGigi/MinionAH-NotEnoughUpdates-REPO/refs/heads/master/recipes/recipes.json"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        JsonObject allRecipes = JsonParser.parseString(response.body()).getAsJsonObject();

                        RECIPES.clear();
                        Queue<String> toProcess = new LinkedList<>();

                        // Initially add all minion recipes
                        for (String key : allRecipes.keySet()) {
                            if (key.contains("_GENERATOR_")) {
                                toProcess.add(key);
                            }
                        }

                        while (!toProcess.isEmpty()) {
                            String currentId = toProcess.poll();

                            if (!RECIPES.containsKey(currentId) && allRecipes.has(currentId)) {
                                JsonObject recipeData = allRecipes.getAsJsonObject(currentId);

                                RECIPES.put(currentId, recipeData);

                                if (currentId.contains("_GENERATOR_") && !currentId.endsWith("_1")) {
                                    continue; // No need to add components of higher tiers
                                }

                                // Add items in the current item's recipe to the queue
                                if (recipeData.has("recipe")) {
                                    JsonObject grid = recipeData.getAsJsonObject("recipe");

                                    String[] slots;

                                    if (currentId.contains("_GENERATOR_")) {
                                        slots = new String[]{"B2"};
                                    } else {
                                        slots = new String[]{"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};
                                    }

                                    for (String slot : slots) {
                                        if (grid.has(slot) && !grid.get(slot).getAsString().isEmpty()) {
                                            String itemData = grid.get(slot).getAsString();

                                            // Strip amounts
                                            int splitIdx = Math.max(itemData.lastIndexOf(':'), itemData.lastIndexOf(';'));
                                            String subItem = (splitIdx != -1) ? itemData.substring(0, splitIdx) : itemData;

                                            if (!RECIPES.containsKey(subItem)) {
                                                toProcess.add(subItem);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        System.out.println("[MissingMinions] Reduced recipe count from " + allRecipes.size() + " to " + RECIPES.size() + " recipes!");
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch recipes.");
                    return null;
                });
    }
}