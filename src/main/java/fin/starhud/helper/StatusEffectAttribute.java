package fin.starhud.helper;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashMap;
import java.util.Map;

public record StatusEffectAttribute(int maxDuration, int amplifier, boolean isAmbient) {

    // ---------------------------------------------------------------------------------------------- //
    // this Implementation is inspired from @SoRadGaming Simple-HUD-Enhanced StatusEffectTracker class
    // see: https://github.com/SoRadGaming/Simple-HUD-Enhanced/blob/main/src/main/java/com/soradgaming/simplehudenhanced/utli/StatusEffectsTracker.java

    private static final Map<MobEffect, StatusEffectAttribute> STATUS_EFFECT_ATTRIBUTE_MAP = new HashMap<>();

    // maxDuration for maxDuration, amplifier and isAmbient to help updating the map.

    public static StatusEffectAttribute getStatusEffectAttribute(MobEffectInstance effect) {
        return STATUS_EFFECT_ATTRIBUTE_MAP.computeIfAbsent(effect.getEffect(), key ->
                new StatusEffectAttribute(
                        effect.getDuration(),
                        effect.getAmplifier(),
                        effect.isAmbient()
                )
        );
    }

    public static void updateStatusEffectAttribute(MobEffect effectRegistry, int maxDuration, int amplifier, boolean isAmbient) {
        StatusEffectAttribute newEffect = new StatusEffectAttribute(
                maxDuration,
                amplifier,
                isAmbient
        );

        STATUS_EFFECT_ATTRIBUTE_MAP.put(effectRegistry, newEffect);
    }

    // used when status effect no longer present in player's status effect list.
    public static void removeStatusEffectAttribute(MobEffect effectRegistry) {
        STATUS_EFFECT_ATTRIBUTE_MAP.remove(effectRegistry);
    }

    public static boolean shouldUpdate(MobEffectInstance current, StatusEffectAttribute cached) {
        return  current.getAmplifier() != cached.amplifier() || // different Amplifier: update
                current.isAmbient() != cached.isAmbient() || // different Ambient: update
                current.getDuration() > cached.maxDuration(); // higher Duration: update
    }

    public static Map<MobEffect, StatusEffectAttribute> getStatusEffectAttributeMap() {
        return STATUS_EFFECT_ATTRIBUTE_MAP;
    }
}