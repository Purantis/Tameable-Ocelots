package com.purantis.tameableocelots;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TameableOcelots implements ModInitializer {
    public static final String MOD_ID = "tameable-ocelots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TameableOcelots() {
    }

    @Override
    public void onInitialize() {
        TameableOcelotsConfig.init();

        LOGGER.info("Ocelots becoming tameable...");
    }
}
