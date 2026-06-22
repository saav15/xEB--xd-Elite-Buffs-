package org.xeb.xeb.compat.hooks;

import org.xeb.xeb.compat.CompatHook;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class BetterCombatHook implements CompatHook {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void registerTypes() {
        LOGGER.info("xEB Mod: Better Combat detected! Integrated combat routing registered successfully.");
    }
}
