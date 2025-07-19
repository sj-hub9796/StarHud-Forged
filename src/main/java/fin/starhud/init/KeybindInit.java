package fin.starhud.init;

import com.mojang.blaze3d.platform.InputConstants;
import fin.starhud.Main;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.glfw.GLFW;

public class KeybindInit {

    public static void init(IEventBus bus) {
        Main.openEditHUDKey = new KeyMapping(
                "key.starhud.open_edithud",
                KeyConflictContext.IN_GAME,
                KeyModifier.NONE,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.starhud"
        );

        bus.addListener(KeybindInit::onRegisterKeyMappings);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(Main.openEditHUDKey);
    }
}
