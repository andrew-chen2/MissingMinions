package com.github.andrewchen2.missingminions.util;

import com.github.andrewchen2.missingminions.config.ModConfig;
import com.github.andrewchen2.missingminions.gui.MainScreen;
import com.github.andrewchen2.missingminions.network.CollectionsApi;
import com.github.andrewchen2.missingminions.network.ItemsApi;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MinionLogic {

    public record Minion(String id, double cost) {}

    public static void calculateMissing(List<String> unlockedMinions, Map<String, Integer> playerCollections) {
        // Clear cache
        RecipeCalculator.COST_CACHE.clear();

        List<String> missingIds = new ArrayList<>();
        for (String minionId : ItemsApi.MINIONS.keySet()) {
            if (!unlockedMinions.contains(minionId)) {
                // Collection filter
                if (ModConfig.filterCollections) {
                    String prefix = minionId.split("_GENERATOR_")[0];
                    CollectionsApi.CollectionReq req = CollectionsApi.MINION_REQS.get(prefix);

                    if (req != null) {
                        // Skip this minion if not yet unlocked in collection
                        int playerTier = playerCollections.getOrDefault(req.collectionId(), 0);
                        if (playerTier < req.tier()) {
                            System.out.println("excluded minion: " + minionId);
                            continue;
                        }
                    }
                }

                missingIds.add(minionId);
            }
        }

        // Sort by tier
        missingIds.sort(Comparator.comparingInt(id -> {
            try {
                String[] parts = id.split("_");
                return Integer.parseInt(parts[parts.length - 1]);
            } catch (Exception e) {
                return 99; // Fallback so it goes to the end if parsing fails
            }
        }));

        List<Minion> missingMinions = new ArrayList<>();

        for (String minionId : missingIds) {
            double cost = RecipeCalculator.calculateItemCost(minionId);

            // Only add to list if a valid cost was found
            if (cost > 0) {
                missingMinions.add(new Minion(minionId, cost));
            }
        }

        // Sort cheapest to most expensive
        missingMinions.sort(Comparator.comparingDouble(m -> m.cost));

        MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().currentScreen instanceof MainScreen screen) {
                screen.setData(missingMinions);
            }
        });
    }
}