package com.github.andrewchen2.missingminions;

import com.github.andrewchen2.missingminions.commands.MMCommand;
import com.github.andrewchen2.missingminions.config.ModConfig;
import com.github.andrewchen2.missingminions.hud.MainHud;
import com.github.andrewchen2.missingminions.network.ItemsApi;
import com.github.andrewchen2.missingminions.network.RecipesApi;
import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingMinions implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("missingminions");

	@Override
	public void onInitializeClient() {
		LOGGER.info("MissingMinions loaded.");

		ModConfig.load();

		ItemsApi.fetchItems();
		RecipesApi.fetchRecipes();

		MainHud.register();
		MMCommand.register();
	}
}