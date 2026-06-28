package org.xeb.xeb.client;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings {
    public static final String KEY_CATEGORY_XEB = "key.categories.xeb";

    public static final KeyMapping ACTUAR_1_KEY = new KeyMapping(
            "key.xeb.actuar_1",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY_XEB
    );

    public static final KeyMapping ACTUAR_2_KEY = new KeyMapping(
            "key.xeb.actuar_2",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY_XEB
    );
}
