package fin.starhud.hud.implementation;

import fin.starhud.Main;
import fin.starhud.config.hud.ArmorSettings;
import fin.starhud.helper.Box;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class ArmorHUD extends AbstractHUD {

    private static final ArmorSettings ARMOR_SETTINGS = Main.settings.armorSettings;

    private static final ResourceLocation ARMOR_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/armor.png");

    private static final ArmorSettings.ArmorPieceSetting[] PIECE_SETTINGS = {
            ARMOR_SETTINGS.helmet,
            ARMOR_SETTINGS.chestplate,
            ARMOR_SETTINGS.leggings,
            ARMOR_SETTINGS.boots
    };

    private static final int TEXTURE_WIDTH = 13 + 1 + 5 + 5;
    private static final int TEXTURE_HEIGHT = 13;

    private static final int ITEM_TEXTURE_WIDTH = 22 + 1 + 5 + 5;
    private static final int ITEM_TEXTURE_HEIGHT = 3 + 16 + 3;

    private static boolean needBoxUpdate = true;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public ArmorHUD() {
        super(ARMOR_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Armor HUD";
    }

    @Override
    public int getBaseHUDWidth() {
        return (PIECE_SETTINGS[0].renderItem || PIECE_SETTINGS[1].renderItem || PIECE_SETTINGS[2].renderItem || PIECE_SETTINGS[3].renderItem) ? ITEM_TEXTURE_WIDTH : TEXTURE_WIDTH;
    }

    @Override
    public int getBaseHUDHeight() {
        return (PIECE_SETTINGS[0].renderItem || PIECE_SETTINGS[1].renderItem || PIECE_SETTINGS[2].renderItem || PIECE_SETTINGS[3].renderItem) ? ITEM_TEXTURE_HEIGHT : TEXTURE_HEIGHT;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender()
                && (PIECE_SETTINGS[0].shouldRender || PIECE_SETTINGS[1].shouldRender || PIECE_SETTINGS[2].shouldRender || PIECE_SETTINGS[3].shouldRender);
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        int armorIndex = 3;

        boolean rendered = false;
        for (ItemStack armor : CLIENT.player.getArmorSlots()) {
            if (PIECE_SETTINGS[armorIndex].shouldRender && !armor.isEmpty() && armor.isDamageableItem()) {
                Box tempBox = RenderUtils.renderDurabilityHUD(
                        context,
                        ARMOR_BACKGROUND_TEXTURE,
                        armor,
                        x + PIECE_SETTINGS[armorIndex].xOffset,
                        y + PIECE_SETTINGS[armorIndex].yOffset,
                        14 * armorIndex,
                        13,
                        TEXTURE_HEIGHT * 4 + 3,
                        PIECE_SETTINGS[armorIndex].color | 0xFF000000,
                        PIECE_SETTINGS[armorIndex].drawBar,
                        PIECE_SETTINGS[armorIndex].renderItem,
                        ARMOR_SETTINGS.base.growthDirectionX
                );
                rendered = true;

                if (needBoxUpdate) {
                    if (super.boundingBox.isEmpty()) {
                        super.boundingBox.setBoundingBox(tempBox.getX(), tempBox.getY(), tempBox.width(), tempBox.getHeight(), tempBox.getColor());
                    } else {
                        super.boundingBox.mergeWith(tempBox);
                    }
                }
            }
            --armorIndex;
        }

        needBoxUpdate = false;
        return rendered;
    }

    @Override
    public void update() {
        super.update();

        needBoxUpdate = true;
        super.boundingBox.setEmpty(true);
    }
}
