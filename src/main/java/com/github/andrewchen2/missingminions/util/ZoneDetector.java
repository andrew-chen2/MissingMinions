package com.github.andrewchen2.missingminions.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

public class ZoneDetector {
    public static boolean isOnPrivateIsland() {
        return getZone().equals("Your Island");
    }

    public static String getZone() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return "";

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return "";

        for (Team team : scoreboard.getTeams()) {
            for (String entry : team.getPlayerList()) {
                Text prefix = team.getPrefix();
                Text suffix = team.getSuffix();
                String fullLine = prefix.getString() + entry + suffix.getString();

                if (fullLine.contains("§7⏣")) {
                    String cleanLine = fullLine.replaceAll("(?i)§[0-9A-Z]", "");
                    return cleanLine.replace("⏣", "").strip();
                }
            }
        }
        return "";
    }
}
