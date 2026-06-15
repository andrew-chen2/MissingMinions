package com.github.andrewchen2.missingminions;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissingMinions implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("missingminions");

	@Override
	public void onInitializeClient() {
		LOGGER.info("MissingMinions loaded.");
	}
}