package com.github.andrewchen2.missingminions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    // Config values
    public static boolean useRecursiveCost = false;
    public static boolean filterCollections = true;
    public static boolean useBuyOrders = false;

    public static void setUseRecursiveCost(boolean newValue) {
        if (useRecursiveCost != newValue) {
            useRecursiveCost = newValue;
            save();
        }
    }

    public static void setFilterCollections(boolean newValue) {
        if (filterCollections != newValue) {
            filterCollections = newValue;
            save();
        }
    }

    public static void setUseBuyOrders(boolean newValue) {
        if (useBuyOrders != newValue) {
            useBuyOrders = newValue;
            save();
        }
    }

    // GSON setup
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "missingminions.json");

    // A tiny data class for GSON to read/write
    private static class ConfigData {
        public boolean useRecursiveCost = ModConfig.useRecursiveCost;
        public boolean filterCollections = ModConfig.filterCollections;
        public boolean useBuyOrders = ModConfig.useBuyOrders;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    useRecursiveCost = data.useRecursiveCost;
                    filterCollections = data.filterCollections;
                    useBuyOrders = data.useBuyOrders;
                }
            } catch (Exception e) {
                System.err.println("[MissingMinions] Failed to load config.");
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            GSON.toJson(data, writer);
        } catch (Exception e) {
            System.err.println("[MissingMinions] Failed to save config.");
        }
    }
}