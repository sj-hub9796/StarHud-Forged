package fin.starhud.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import fin.starhud.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class RenderUtils {

    private static final ResourceLocation DURABILITY_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/durability_bar.png");
    private static final ResourceLocation DURABILITY_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/durability_background.png");

    private static final ResourceLocation ITEM_DURABILITY_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/big_durability_bar.png");
    private static final ResourceLocation ITEM_DURABILITY_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/big_durability_background.png");

    // 10 bars + 9 gaps.
    private static final int DURABILITY_WIDTH = (3 * 10) + 9;
    private static final int ITEM_DURABILITY_WIDTH = (5 * 10) + (2 * 9);

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final Box tempBox = new Box(0,0);

    public static void fillRoundedRightSide(GuiGraphics context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2 - 1, y2, color);
        context.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
    }

    // get the durability "steps" or progress.
    public static int getItemBarStep(ItemStack stack, int maxStep) {
        return Mth.clamp(Math.round(maxStep - (float) stack.getDamageValue() * maxStep / (float) stack.getMaxDamage()), 0, maxStep);
    }

    // color transition from pastel (red to green).
    public static int getItemBarColor(int stackStep, int maxStep) {
        return Mth.hsvToRgb(0.35F * stackStep / (float) maxStep, 0.45F, 0.95F);
    }

    public static Box renderDurabilityHUD(GuiGraphics context, ResourceLocation ICON, ItemStack stack, int x, int y, float v, int textureWidth, int textureHeight, int color, boolean drawBar, boolean renderItem, GrowthDirectionX textureGrowth) {
        if (renderItem) {
            return renderItemDurability(context, stack, x , y, drawBar, textureGrowth);
        } else {
            return renderDurability(context, ICON, stack, x, y, v, textureWidth, textureHeight, color, drawBar, textureGrowth);
        }
    }

    public static Box renderItemDurability(GuiGraphics context, ItemStack stack, int x, int y, boolean drawBar, GrowthDirectionX textureGrowth) {
        if (drawBar) {
            return renderItemDurabilityBar(context, stack, x, y,textureGrowth);
        } else {
            return renderItemDurabilityNumber(context, stack, x, y, textureGrowth);
        }
    }

    public static Box renderDurability(GuiGraphics context, ResourceLocation ICON, ItemStack stack, int x, int y, float v, int textureWidth, int textureHeight, int color, boolean drawBar, GrowthDirectionX textureGrowth) {
        if (drawBar) {
            return renderDurabilityBar(context, ICON, stack, x, y, v, textureWidth, textureHeight, color, textureGrowth);
        } else {
            return renderDurabilityNumber(context, ICON, stack, x, y, v, textureWidth, textureHeight, color, textureGrowth);
        }
    }

    public static Box renderItemDurabilityBar(GuiGraphics context, ItemStack stack, int x, int y, GrowthDirectionX textureGrowth) {
        int step = getItemBarStep(stack, 10);
        int durabilityColor = getItemBarColor(step, 10) | 0xFF000000;

        x -= textureGrowth.getGrowthDirection(ITEM_DURABILITY_WIDTH);

        // draw the durability background and item
        RenderUtils.drawTextureHUD(context, ITEM_DURABILITY_BACKGROUND_TEXTURE, x, y, 0.0F, 0.0F, 101, 22, 101, 22);
        context.renderItem(stack, x + 3, y + 3);

        if (step != 0) RenderUtils.drawTextureHUD(context, ITEM_DURABILITY_TEXTURE, x + 28, y + 4, 0, 0, 7 * step, 14, 70, 14, durabilityColor);

        tempBox.setBoundingBox(x, y, 101, 22, durabilityColor);
        return tempBox;
    }

    public static Box renderItemDurabilityNumber(GuiGraphics context, ItemStack stack, int x, int y, GrowthDirectionX textureGrowth) {
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        int remaining = maxDamage - damage;

        String durability = remaining + "/" + maxDamage;

        int durabilityWidth = CLIENT.font.width(durability) - 1;

        int textColor = getItemBarColor(remaining, maxDamage) | 0xFF000000;

        x -= textureGrowth.getGrowthDirection(durabilityWidth);

        RenderUtils.drawTextureHUD(context,  ITEM_DURABILITY_BACKGROUND_TEXTURE, x, y, 0.0F, 0.0F, 22, 22, 101, 22);
        context.renderItem(stack, x + 3, y + 3);

        fillRoundedRightSide(context, x + 23,  y, x + 22 + 1 + 5 + durabilityWidth + 5, y + 22, 0x80000000);
        RenderUtils.drawTextHUD(context, durability, x + 22 + 1 + 5, y + 7, textColor, false);

        tempBox.setBoundingBox(x, y, 22 + 1 + 5 + durabilityWidth + 5, 22, textColor);
        return tempBox;
    }

    public static Box renderDurabilityBar(GuiGraphics context, ResourceLocation ICON, ItemStack stack, int x, int y, float v, int textureWidth, int textureHeight, int color, GrowthDirectionX textureGrowth) {
        int step = getItemBarStep(stack, 10);
        int durabilityColor = getItemBarColor(step, 10) | 0xFF000000;

        x -= textureGrowth.getGrowthDirection(DURABILITY_WIDTH);

        // draw the icon
        RenderUtils.drawTextureHUD(context, ICON, x, y, 0.0F, v, 13, 13, textureWidth, textureHeight, color);

        // draw the durability background and steps
        RenderUtils.drawTextureHUD(context, DURABILITY_BACKGROUND_TEXTURE, x + 14, y, 0.0F, 0.0F, 49, 13, 49, 13);
        if (step != 0) RenderUtils.drawTextureHUD(context, DURABILITY_TEXTURE, x + 19, y + 3, 0, 0, 4 * step, 7, 40, 7, durabilityColor);

        tempBox.setBoundingBox(x, y, 13 + 1 + 49, 13, color);
        return tempBox;
    }

    // example render: ¹²³⁴/₅₆₇₈
    public static Box renderDurabilityNumber(GuiGraphics context, ResourceLocation ICON, ItemStack stack, int x, int y, float v, int textureWidth, int textureHeight, int color, GrowthDirectionX textureGrowth) {
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        int remaining = maxDamage - damage;

        String remainingStr = Helper.toSuperscript(Integer.toString(remaining)) + '/';
        String maxDamageStr = Helper.toSubscript(Integer.toString(maxDamage));

        int remainingTextWidth = CLIENT.font.width(remainingStr);
        int maxDamageTextWidth = CLIENT.font.width(maxDamageStr);

        int textColor = getItemBarColor(remaining, maxDamage) | 0xFF000000;

        x -= textureGrowth.getGrowthDirection(remainingTextWidth + maxDamageTextWidth);

        RenderUtils.drawTextureHUD(context, ICON, x, y, 0.0F, v, 13, 13, textureWidth, textureHeight, color);
        fillRoundedRightSide(context, x + 14,  y, x + 14 + remainingTextWidth + maxDamageTextWidth + 10, y + 13, 0x80000000);

        // this is gore of my comfort character, call drawString twice except the second one has a -1 pixel offset.
        // Minecraft default subscript's font is 1 pixel to deep for my liking, so I have to shift them up.
        RenderUtils.drawTextHUD(context, remainingStr, x + 14 + 5, y + 3, textColor, false);
        RenderUtils.drawTextHUD(context, maxDamageStr, x + 14 + 5 + remainingTextWidth, y + 3 - 1, textColor, false);

        tempBox.setBoundingBox(x, y, 14 + remainingTextWidth + maxDamageTextWidth + 10, 13, color);
        return tempBox;
    }

    // for easier version porting.

    public static void drawTextureAlphaColor(GuiGraphics context, ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {

        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        context.blit(identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    public static void drawTextureAlpha(GuiGraphics context, ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.enableBlend();
        context.blit(identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.disableBlend();
    }

    public static void drawTextureColor(GuiGraphics context, ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
        context.blit(identifier, x, y, u, v, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // for easier version porting.

    public static void drawTextureHUD(GuiGraphics context, ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, int color) {
        drawTextureAlphaColor(context, identifier, x, y, u, v, width, height, textureWidth, textureHeight, color);
    }

    public static void drawTextureHUD(GuiGraphics context, ResourceLocation identifier, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        drawTextureAlpha(context, identifier, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void drawTextHUD(GuiGraphics context, String str, int x, int y, int color, boolean shadow) {
        FormattedCharSequence orderedText = FormattedCharSequence.forward(str, Style.EMPTY);
        context.drawString(CLIENT.font, orderedText, x , y, color, shadow);
    }

    public static void drawTextHUD(GuiGraphics context, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        context.drawString(CLIENT.font, text, x, y, color, shadow);
    }
}
