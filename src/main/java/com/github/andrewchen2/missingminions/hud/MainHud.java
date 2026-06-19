package com.github.andrewchen2.missingminions.hud;

import com.github.andrewchen2.missingminions.network.ItemsApi;
import com.github.andrewchen2.missingminions.util.MinionLogic;
import com.github.andrewchen2.missingminions.util.ZoneDetector;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MainHud {
    private static List<MinionLogic.Minion> TOP_MINIONS = new ArrayList<>();

    public static void setTopMinions(List<MinionLogic.Minion> allMissing) {
        TOP_MINIONS = allMissing.size() > 5 ? allMissing.subList(0, 5) : new ArrayList<>(allMissing);
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of("missingminions", "top_minions"),
                (context, tickCounter) -> {
                    MinecraftClient client = MinecraftClient.getInstance();

                    if (client.player == null || client.textRenderer == null) return;
                    if (client.options.hudHidden) return; // Hide when F3 is open
                    if (!ZoneDetector.isOnPrivateIsland()) return;

                    if (TOP_MINIONS.isEmpty()) return;

                    int x = 10;
                    int y = 10;

                    context.drawText(client.textRenderer, "§a=== Top 5 Cheapest Missing Minions ===", x, y, Colors.WHITE, true);
                    y += 12;

                    int rank = 1;
                    for (MinionLogic.Minion m : TOP_MINIONS) {
                        ItemsApi.MinionData data = ItemsApi.MINIONS.get(m.id());
                        String name = (data != null) ? data.displayName() : m.id();

                        String cost = String.format("%,.0f", m.cost());
                        String line = "§b" + rank + ". §e" + name + " §7- §6" + cost + " coins";

                        context.drawText(client.textRenderer, line, x, y, Colors.WHITE, true);

                        y += 10;
                        rank++;
                    }
                }
        );
    }
}
