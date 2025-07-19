package fin.starhud.mixin;

import fin.starhud.Main;
import fin.starhud.config.GeneralSettings;
import fin.starhud.hud.HUDComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Unique private static final GeneralSettings.InGameHUDSettings SETTINGS = Main.settings.generalSettings.inGameSettings;

    // Mixin used to override vanilla effect HUD, I'm not sure whether this can be done using HUDElementRegistry
    @Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
    private void starhudforged$renderEffects(GuiGraphics context, CallbackInfo ci) {
        if (SETTINGS.disableHUDRendering) return;

        if (HUDComponent.getInstance().effectHUD.shouldRender()) {
            if (HUDComponent.getInstance().shouldRenderInGameScreen())
                HUDComponent.getInstance().effectHUD.render(context);
            ci.cancel();
        }
    }
}
