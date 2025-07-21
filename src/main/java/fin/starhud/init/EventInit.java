package fin.starhud.init;

import fin.starhud.Helper;
import fin.starhud.Main;
import fin.starhud.compat.ImmediatelyFastCompat;
import fin.starhud.config.GeneralSettings;
import fin.starhud.hud.HUDComponent;
import fin.starhud.screen.EditHUDScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

public class EventInit {

    private static final GeneralSettings.InGameHUDSettings SETTINGS = Main.settings.generalSettings.inGameSettings;;

    public static void init() {
        // register keybinding event, on openEditHUDKey pressed -> move screen to edit hud screen.
        MinecraftForge.EVENT_BUS.addListener(EventInit::onClientTick);
        // register hud... idk where tho haha
        MinecraftForge.EVENT_BUS.addListener(EventInit::onRenderGui);
    }

    private static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        while (Main.openEditHUDKey.consumeClick()) {
            Minecraft client = Minecraft.getInstance();
            client.setScreen(new EditHUDScreen(Component.literal("Edit HUD"), client.screen));
        }
    }

    private static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (SETTINGS.disableHUDRendering) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return; // starhudforged - forge gap

        if (SETTINGS.shouldBatchHUDWithImmediatelyFast && Helper.isModPresent("immediatelyfast")) {
            ImmediatelyFastCompat.beginHudBatching();
            if (!Minecraft.getInstance().options.hideGui)
                if (HUDComponent.getInstance().shouldRenderInGameScreen())
                    HUDComponent.getInstance().renderAll(event.getGuiGraphics());
            ImmediatelyFastCompat.endHudBatching();
        } else {
            if (!Minecraft.getInstance().options.hideGui)
                if (HUDComponent.getInstance().shouldRenderInGameScreen())
                    HUDComponent.getInstance().renderAll(event.getGuiGraphics());
        }
    }
}
