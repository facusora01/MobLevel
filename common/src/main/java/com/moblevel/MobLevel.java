package com.moblevel;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Loader-independent constants. Each loader has its own entry point that wires the
 * shared code into its event bus, networking and config.
 */
public final class MobLevel {
    public static final String MODID = "moblevel";
    public static final Logger LOGGER = LogUtils.getLogger();

    private MobLevel() {
    }
}
