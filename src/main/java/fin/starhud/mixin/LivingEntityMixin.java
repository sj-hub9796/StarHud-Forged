package fin.starhud.mixin;

import fin.starhud.helper.StatusEffectAttribute;
import fin.starhud.hud.HUDComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // this is the way to ENSURE that for every change in status effect, the old value is removed.
    // I don't know if this is safe tho...

    @Inject(method = "forceAddEffect", at = @At("HEAD"))
    private void starhudforged$onSetStatusEffect(MobEffectInstance effect, Entity source, CallbackInfo ci) {
        if (!HUDComponent.getInstance().effectHUD.shouldRender())
            return;

        StatusEffectAttribute statusEffectAttribute = StatusEffectAttribute.getStatusEffectAttribute(effect);

        if (StatusEffectAttribute.shouldUpdate(effect, statusEffectAttribute))
            StatusEffectAttribute.updateStatusEffectAttribute(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient());
    }

    // using removeStatusEffectInternal() instead of removeStatusEffect() because the former worked and the latter didn't, I don't know why.
    // remove status effect from the player status effect list. Reason is just to delete unused effect from the map.
    @Inject(method = "removeEffectNoUpdate", at = @At("RETURN"))
    private void starhudforged$onStatusEffectRemoved(MobEffect type, CallbackInfoReturnable<MobEffectInstance> cir) {
        StatusEffectAttribute.removeStatusEffectAttribute(type);
    }
}
