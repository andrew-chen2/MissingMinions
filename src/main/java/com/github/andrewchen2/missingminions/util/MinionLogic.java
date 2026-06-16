package com.github.andrewchen2.missingminions.util;

import com.github.andrewchen2.missingminions.network.ItemsApi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinionLogic {

    // Minion data class
    public static class Minion {
        public String id;
        public double cost;

        public Minion(String id, double cost) {
            this.id = id;
            this.cost = cost;
        }
    }

    public static void calculateMissing(List<String> unlockedMinions) {
        ChatUtils.sendMessage("§7Calculating costs for missing minions...");

        List<String> missingIds = new ArrayList<>();
        for (String minionId : ItemsApi.MASTER_MINION_LIST) {
            if (!unlockedMinions.contains(minionId)) {
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
            double cost = RecipeCalculator.calculateMinionCost(minionId);

            // Only add to list if a valid cost was found
            if (cost > 0) {
                missingMinions.add(new Minion(minionId, cost));
            }
        }

        // Sort cheapest to most expensive
        missingMinions.sort(Comparator.comparingDouble(m -> m.cost));

        // Output top 5 cheapest
        ChatUtils.sendMessage("§a=== Top 5 Cheapest Missing Minions ===");

        int count = 0;
        for (Minion m : missingMinions) {
            if (count >= 5) break;

            // Nice formatting
            String cleanName = m.id.replace("_GENERATOR_", " Tier ");
            String formattedCost = String.format("%,.0f", m.cost);

            ChatUtils.sendMessage("§b" + (count + 1) + ". §e" + cleanName + " §7- §6" + formattedCost + " coins");
            count++;
        }

        if (missingMinions.isEmpty()) {
            ChatUtils.sendMessage("§aImpressive! You've crafted every single minion!");
        }
    }
}