package fin.starhud.helper;

import fin.starhud.hud.implementation.TargetedCrosshairHUD;
import fin.starhud.mixin.accessor.BossHealthOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.tags.FluidTags;

public class Conditions {

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static boolean isChatFocused() {
        return CLIENT.screen instanceof ChatScreen;
    }
    public static boolean isDebugHUDOpen() {
        return CLIENT.options.renderDebug;
    }

    public static boolean isBossBarShown() {
        return !((BossHealthOverlayAccessor) CLIENT.gui.getBossOverlay()).getBossBars().isEmpty();
    }

    public static boolean isScoreBoardShown() {
        return !(CLIENT.level != null && CLIENT.level.getScoreboard().getObjectives().isEmpty());
    }

    public static boolean isBeneficialEffectOverlayShown() {
        return CLIENT.player.getActiveEffects().stream()
                .anyMatch(effect -> effect.getEffect().isBeneficial());
    }

    public static boolean isHarmEffectOverlayShown() {
        return CLIENT.player.getActiveEffects().stream()
                .anyMatch(effect -> !effect.getEffect().isBeneficial());
    }

    public static boolean isOffHandOverlayShown() {
        return !CLIENT.player.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty();
    }

    public static boolean isHealthBarShown() {
        return CLIENT.gameMode.canHurtPlayer();
    }

    public static boolean isExperienceBarShown() {
        return CLIENT.gameMode.hasExperience();
    }

    public static boolean isArmorBarShown() {
        return isHealthBarShown() && CLIENT.player.getArmorValue() > 0;
    }

    public static boolean isAirBubbleBarShown() {
        return isHealthBarShown() && CLIENT.player.isEyeInFluid(FluidTags.WATER) || CLIENT.player.getAirSupply() < CLIENT.player.getMaxAirSupply();
    }

    public static boolean isTargetedCrosshairHUDShown() {
        return TargetedCrosshairHUD.shouldHUDRender();
    }
}