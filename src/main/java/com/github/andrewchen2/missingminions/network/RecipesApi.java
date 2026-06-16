package com.github.andrewchen2.missingminions.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RecipesApi {
    // JSON object of all recipes
    public static JsonObject RECIPES = new JsonObject();

    public static void fetchRecipes() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://raw.githubusercontent.com/DarthGigi/MinionAH-NotEnoughUpdates-REPO/refs/heads/master/recipes/recipes.json"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        RECIPES = JsonParser.parseString(response.body()).getAsJsonObject();
                        System.out.println("[MissingMinions] Successfully cached " + RECIPES.size() + " recipes!");
                    }
                })
                .exceptionally(e -> {
                    System.err.println("Failed to fetch recipes.");
                    return null;
                });
    }
}