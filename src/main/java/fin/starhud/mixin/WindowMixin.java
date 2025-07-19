package fin.starhud.mixin;

import fin.starhud.hud.HUDComponent;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    // we update our HUD when the scaleWidth / scaleHeight is changed.
    // although quite silly, but This is the best performance we can get.
    @Inject(method = "setGuiScale", at = @At("TAIL"))
    public void starhudforged$onScaleChanged(double scaleFactor, CallbackInfo ci) {
        HUDComponent.getInstance().updateAll();
    }
}
