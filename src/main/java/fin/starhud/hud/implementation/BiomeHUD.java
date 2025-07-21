package fin.starhud.hud.implementation;

import fin.starhud.Helper;
import fin.starhud.Main;
import fin.starhud.config.hud.BiomeSettings;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Optional;

public class  BiomeHUD extends AbstractHUD {

    private static final BiomeSettings BIOME_SETTINGS = Main.settings.biomeSettings;

    private static final ResourceLocation DIMENSION_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/biome.png");

    private static Component cachedBiomeNameText;
    private static Holder<net.minecraft.world.level.biome.Biome> cachedBiome;
    private static int cachedTextWidth;

    private static final int TEXTURE_WIDTH = 24;
    private static final int TEXTURE_HEIGHT = 13;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public BiomeHUD() {
        super(BIOME_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Biome HUD";
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        Font font = CLIENT.font;

        BlockPos blockPos = CLIENT.player.blockPosition();
        Holder<Biome> currentBiome = CLIENT.level.getBiome(blockPos);

        if (cachedBiome != currentBiome) {
            Optional<ResourceKey<Biome>> biomeKey = currentBiome.unwrapKey();

            if (biomeKey.isPresent()) {
                ResourceLocation biomeId = biomeKey.get().location();
                String translatableKey = "biome." + biomeId.getNamespace() + '.' + biomeId.getPath();

                // if it has translation we get the translation, else we just convert it to Pascal Case manually.
                if (Language.getInstance().has(translatableKey))
                    cachedBiomeNameText = Component.translatable(translatableKey);
                else
                    cachedBiomeNameText = Component.literal(Helper.idNameFormatter(getBiomeIdAsString(currentBiome)));

            } else {
                cachedBiomeNameText = Component.literal("Unregistered");
            }

            cachedBiome = currentBiome;
            cachedTextWidth = font.width(cachedBiomeNameText);
        }

        int dimensionIndex = getDimensionIndex(CLIENT.level.dimension());
        int color = getTextColorFromDimension(dimensionIndex) | 0xFF000000;

        int xTemp = x - BIOME_SETTINGS.base.growthDirectionX.getGrowthDirection(cachedTextWidth);

        RenderUtils.drawTextureHUD(context, DIMENSION_TEXTURE, xTemp, y, 0.0F, dimensionIndex * TEXTURE_HEIGHT, 13, TEXTURE_HEIGHT, 13, 52);
        RenderUtils.fillRoundedRightSide(context, xTemp + 14, y, xTemp + 14 + cachedTextWidth + 9, y + TEXTURE_HEIGHT, 0x80000000);
        RenderUtils.drawTextHUD(context, cachedBiomeNameText.getVisualOrderText(), xTemp + 19, y + 3, color, false);

        setBoundingBox(xTemp, y, 14 + cachedTextWidth + 9, TEXTURE_HEIGHT, color);
        return true;
    }

    private static String getBiomeIdAsString(Holder<Biome> biome) {
        return biome.unwrap().map((biomeKey) -> biomeKey.location().toString(), (biome_) -> "[unregistered " + biome_ + "]");
    }

    private static int getDimensionIndex(ResourceKey<Level> registryKey) {
        if (registryKey == Level.OVERWORLD) return 0;
        else if (registryKey == Level.NETHER) return 1;
        else if (registryKey == Level.END) return 2;
        else return 3;
    }

    private static int getTextColorFromDimension(int dimensionIndex) {
        return switch (dimensionIndex) {
            case 0 -> BIOME_SETTINGS.color.overworld;
            case 1 -> BIOME_SETTINGS.color.nether;
            case 2 -> BIOME_SETTINGS.color.end;
            default -> BIOME_SETTINGS.color.custom;
        };
    }

    @Override
    public int getBaseHUDWidth() {
        return TEXTURE_WIDTH;
    }

    @Override
    public int getBaseHUDHeight() {
        return TEXTURE_HEIGHT;
    }
}
