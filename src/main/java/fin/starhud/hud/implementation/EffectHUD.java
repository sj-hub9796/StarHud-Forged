package fin.starhud.hud.implementation;

import fin.starhud.Helper;
import fin.starhud.Main;
import fin.starhud.config.hud.EffectSettings;
import fin.starhud.helper.*;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EffectHUD extends AbstractHUD {

    private static final EffectSettings effectSettings = Main.settings.effectSettings;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final ResourceLocation STATUS_EFFECT_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/effect.png");
    private static final ResourceLocation STATUS_EFFECT_BAR_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/effect_bar.png");
    private static final ResourceLocation STATUS_EFFECT_AMBIENT_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/effect_ambient.png");

    private static final int STATUS_EFFECT_TEXTURE_WIDTH = 24;
    private static final int STATUS_EFFECT_TEXTURE_HEIGHT = 32;
    private static final int STATUS_EFFECT_BAR_TEXTURE_WIDTH = 21;
    private static final int STATUS_EFFECT_BAR_TEXTURE_HEIGHT = 3;

    private static final Map<MobEffect, ResourceLocation> STATUS_EFFECT_TEXTURE_MAP = new HashMap<>();

    private static final Box tempBox = new Box(0,0);
    private static int cachedSize = -1;
    private static boolean needBoxUpdate = true;

    public EffectHUD() {
        super(effectSettings.base);
    }

    @Override
    public String getName() {
        return "Status Effect HUD";
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender()
                && !CLIENT.player.getActiveEffects().isEmpty();
    }

    @Override
    public void update() {
        super.update();

        needBoxUpdate = true;
        super.boundingBox.setEmpty(true);
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {

        // straight up copied from minecraft's own status effect rendering system.
        // but with 20x more mess!!!!

        Collection<MobEffectInstance> collection = CLIENT.player.getActiveEffects();

        int beneficialIndex = 0;
        int harmIndex = 0;

        boolean drawVertical = effectSettings.drawVertical;

        // sameTypeGap = the gap between each beneficial / harm effect.
        int sameTypeGap = effectSettings.sameTypeGap;

        /* differentTypeGap = the gap between beneficial and harm effect.
        * if HUD on the right screen and is drawn Vertically, We change the differentTypeGap from going right, to left.so that the harm effect hud does not go out of screen
        * if HUD on the bottom screen and is drawn horizontally, We change the differentTypeGap from going down, to up. so that the harm effect hud does not go out of screen
        * */
        int differentTypeGap = ((drawVertical && effectSettings.base.originX == ScreenAlignmentX.RIGHT) || (!drawVertical && effectSettings.base.originY == ScreenAlignmentY.BOTTOM)) ? -effectSettings.differentTypeGap :effectSettings.differentTypeGap;

        int effectSize = collection.size();
        int beneficialSize = getBeneficialSize();
        int harmSize = effectSize - beneficialSize;

        // xBeneficial, yBeneficial = Starting point for beneficial effect HUD.
        int xBeneficial = x - effectSettings.base.growthDirectionX.getGrowthDirection(getDynamicWidth(true, beneficialSize, harmSize));
        int yBeneficial = y - effectSettings.base.growthDirectionY.getGrowthDirection(getDynamicHeight(true, beneficialSize, harmSize));

        // xHarm, yHarm = Starting point for harm effect HUD.
        // this is just a way to say
        // "if the beneficial effect is empty, we place harm effect in the same place as beneficial effect, yes, replacing its position"
        int xHarm = (beneficialSize == 0 && drawVertical) ? xBeneficial :
                x - effectSettings.base.growthDirectionX.getGrowthDirection(getDynamicWidth(false, beneficialSize, harmSize));
        int yHarm = (beneficialSize == 0 && !drawVertical) ? yBeneficial :
                y - effectSettings.base.growthDirectionY.getGrowthDirection(getDynamicHeight(false, beneficialSize, harmSize));

        boolean shouldBoxUpdate = (needBoxUpdate || cachedSize != StatusEffectAttribute.getStatusEffectAttributeMap().size());

        if (shouldBoxUpdate)
            cachedSize = StatusEffectAttribute.getStatusEffectAttributeMap().size();

        for (MobEffectInstance statusEffectInstance : collection) {
            if (!statusEffectInstance.showIcon())
                continue;

            MobEffect statusEffect = statusEffectInstance.getEffect();
            StatusEffectAttribute statusEffectAttribute = StatusEffectAttribute.getStatusEffectAttribute(statusEffectInstance);

            int x2;
            int y2;

            if (statusEffect.isBeneficial()) {

                // if the hud is drawn vertically, we definitely do not want to move the beneficial effect horizontally.
                x2 = (xBeneficial) + ((drawVertical ? 0 : sameTypeGap) * beneficialIndex);

                // if the hud is drawn horizontally, we definitely do not want to move the beneficial effect vertically.
                y2 = (yBeneficial) + ((drawVertical ? sameTypeGap : 0) * beneficialIndex);

                ++beneficialIndex;

            } else {

                x2 = (xHarm)
                        // if beneficial is empty, we replace the position to harm effect. else we shift the harm effect hud accordingly.
                        + (beneficialSize == 0 ? 0 : (drawVertical ? differentTypeGap : 0))
                        // if hud is drawn vertically, we do not want to move the effect horizontally.
                        + ((drawVertical ? 0 : sameTypeGap) * harmIndex);

                y2 = (yHarm)
                        // if beneficial is empty, we replace the position to harm effect. else we shift the harm effect hud accordingly.
                        + (beneficialSize == 0 ? 0 : (drawVertical ? 0 : differentTypeGap))
                        // if hud is drawn vertically, we do not want to move the effect horizontally.
                        + ((drawVertical ? sameTypeGap : 0) * harmIndex);

                ++harmIndex;
            }

            // Final State: x2 and y2 contains the correct placement for the effect HUD, ready to be drawn.

            if (shouldBoxUpdate) {
                tempBox.setBoundingBox(x2, y2, STATUS_EFFECT_TEXTURE_WIDTH, STATUS_EFFECT_TEXTURE_HEIGHT);
                if (super.boundingBox.isEmpty())
                    super.boundingBox.setBoundingBox(tempBox.getX(), tempBox.getY(), tempBox.width(), tempBox.getHeight(), effectSettings.ambientColor | 0xFF000000);
                else
                    super.boundingBox.mergeWith(tempBox);
            }

            if (statusEffectInstance.isAmbient()) {

                // draw soft blue outlined background...
                RenderUtils.drawTextureHUD(
                        context,
                        STATUS_EFFECT_AMBIENT_TEXTURE,
                        x2, y2,
                        0, 0,
                        STATUS_EFFECT_TEXTURE_WIDTH, STATUS_EFFECT_TEXTURE_HEIGHT,
                        STATUS_EFFECT_TEXTURE_WIDTH, STATUS_EFFECT_TEXTURE_HEIGHT,
                        effectSettings.ambientColor | 0xFF000000
                );

            } else {

                // draw background
                RenderUtils.drawTextureHUD(
                        context,
                        STATUS_EFFECT_BACKGROUND_TEXTURE,
                        x2, y2,
                        0, 0,
                        STATUS_EFFECT_TEXTURE_WIDTH, STATUS_EFFECT_TEXTURE_HEIGHT,
                        STATUS_EFFECT_TEXTURE_WIDTH, STATUS_EFFECT_TEXTURE_HEIGHT
                );
            }

            int duration = statusEffectInstance.getDuration();

            int step, color;
            if (statusEffectInstance.isInfiniteDuration()) {
                step = 7;
                color = effectSettings.infiniteColor | 0xFF000000;
            } else {
                int maxDuration = statusEffectAttribute.maxDuration();

                step = Helper.getStep(duration, maxDuration, 7);
                color = (effectSettings.useEffectColor ? statusEffect.getColor() : RenderUtils.getItemBarColor(step, 7)) | 0xFF000000;
            }

            // draw timer bar
            RenderUtils.drawTextureHUD(
                    context,
                    STATUS_EFFECT_BAR_TEXTURE,
                    x2 + 2, y2 + 27,
                    0, 0,
                    3 * step, STATUS_EFFECT_BAR_TEXTURE_HEIGHT,
                    STATUS_EFFECT_BAR_TEXTURE_WIDTH, STATUS_EFFECT_BAR_TEXTURE_HEIGHT,
                    color
            );

            float alpha = 1.0F;
            if (duration <= 200 && !statusEffectInstance.isInfiniteDuration()) { // minecraft's status effect blinking.
                int n = 10 - duration / 20;
                alpha = Mth.clamp((float)duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float)duration * (float)Math.PI / 5.0F) * Mth.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
                alpha = Mth.clamp(alpha, 0.0F, 1.0F);
            }

            // draw effect texture.
            RenderUtils.drawTextureHUD(
                    context,
                    getStatusEffectTexture(statusEffect),
                    x2 + 3, y2 + 3,
                    0,0,
                    18, 18,
                    18,18,
                    (Math.round(alpha * 255) << 24) | 0xFFFFFF
            );

            // draw amplifier text.
            int amplifier = statusEffectAttribute.amplifier() + 1;
            if (amplifier == 1)
                continue;

            String amplifierStr = Helper.toSubscript(Integer.toString(amplifier));

            RenderUtils.drawTextHUD(
                    context,
                    amplifierStr,
                    x2 + 3 + 18 - CLIENT.font.width(amplifierStr) + 1, y2 + 2 + 18 - 7,
                    0xFFFFFFFF,
                    true
            );

        }

        needBoxUpdate = false;
        return true;
    }

    public int getDynamicWidth(boolean isBeneficial, int beneficialSize, int harmSize) {
                 // if we draw the HUD vertically, essentially the width should be the texture width
         return effectSettings.drawVertical ? STATUS_EFFECT_TEXTURE_WIDTH
                 // else, the width should be the whole column of Effect HUDs.
                 : ((isBeneficial ? beneficialSize : harmSize) * effectSettings.sameTypeGap);
    }

    public int getDynamicHeight(boolean isBeneficial, int beneficialSize, int harmSize) {
                // if the HUD is drawn Vertically, the Height should be the whole row of Effect HUDs
        return effectSettings.drawVertical ? ((isBeneficial ? beneficialSize : harmSize) * effectSettings.sameTypeGap)
                // else, the height is just the same as the texture height.
                : STATUS_EFFECT_TEXTURE_HEIGHT;
    }

    public static int getBeneficialSize() {
        int size = 0;
        for (MobEffectInstance collection : CLIENT.player.getActiveEffects()) {
            if (collection.getEffect().isBeneficial())
                ++size;
        }
        return size;
    }

    // 0 because the width is dependent to how many status effect are present.
    @Override
    public int getBaseHUDWidth() {
        return 0;
    }

    @Override
    public int getBaseHUDHeight() {
        return 0;
    }

    public static ResourceLocation getStatusEffectTexture(MobEffect effect) {
        return STATUS_EFFECT_TEXTURE_MAP.computeIfAbsent(effect, e -> {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(e);
            return ResourceLocation.tryBuild(id.getNamespace(), "textures/mob_effect/" + id.getPath() + ".png");
        });
    }

}
