package fin.starhud.hud.implementation;

import fin.starhud.config.hud.HandSettings;
import fin.starhud.helper.Box;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

// i dont like this one.

public abstract class AbstractHandHUD extends AbstractHUD {

    private static final ResourceLocation HAND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/hand.png");
    private static final ResourceLocation BIG_HAND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/big_hand.png");

    // base HUD Width (Icon width = 13, 1 for the gap between icon and text, 5 for the gap on left side of the attribute, 5 for the gap on the right of the attribute).
    private static final int TEXTURE_WIDTH = 13 + 1 + 5 + 5;

    // count string is at max 4 digits, each digit may have 5 pixels.
    // 5 + 1 + 5 + 1 + 5 + 1 + 5 = 23.
    // count string adds 23 additional width.
    private static final int COUNT_WIDTH = 23;
    private static final int TEXTURE_HEIGHT = 13;

    private static final int ITEM_TEXTURE_WIDTH = 22 + 1 + 5 + 5;
    private static final int ITEM_TEXTURE_HEIGHT = 3 + 16 + 3;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    private final HandSettings handSettings;
    private final HumanoidArm arm;

    public AbstractHandHUD(HandSettings handSettings, HumanoidArm arm) {
        super(handSettings.base);
        this.handSettings = handSettings;
        this.arm = arm;
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender()
                && !CLIENT.player.getItemInHand(CLIENT.player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).isEmpty();
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        return renderHandHUD(context, arm, x, y);
    }

    public boolean renderHandHUD(GuiGraphics context, HumanoidArm arm, int x, int y) {
        Inventory playerInventory = CLIENT.player.getInventory();

        ItemStack item = CLIENT.player.getItemInHand(CLIENT.player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);

        // either draw the durability or the amount of item in the inventory.
        if (handSettings.showDurability && item.isDamageableItem()) {
            Box box = RenderUtils.renderDurabilityHUD(context, HAND_TEXTURE, item, x, y, getV(), COUNT_WIDTH + TEXTURE_WIDTH, 27, handSettings.color | 0xFF000000, handSettings.drawBar, handSettings.renderItem, handSettings.base.growthDirectionX);
            copyBoundingBox(box);
        } else if (handSettings.showCount) {
            x -= handSettings.base.growthDirectionX.getGrowthDirection(COUNT_WIDTH);
            renderStackCountHUD(context, playerInventory, item, x, y, getV(), handSettings.color | 0xFF000000);
        }
        return true;
    }

    private void renderStackCountHUD(GuiGraphics context, Inventory playerInventory, ItemStack stack, int x, int y, float v, int color) {
        if (handSettings.renderItem) {
            renderStackCountItemHUD(context, playerInventory, stack, x, y, color);
        } else {
            renderStackCountIconHUD(context, playerInventory, stack, x, y, v, color);
        }
    }

    private void renderStackCountItemHUD(GuiGraphics context, Inventory playerInventory, ItemStack stack, int x, int y, int color) {
        int stackAmount = getItemCount(playerInventory, stack);
        String amountStr = Integer.toString(stackAmount);

        RenderUtils.drawTextureHUD(context, BIG_HAND_TEXTURE, x, y, 0.0F, 0.0F, ITEM_TEXTURE_WIDTH + COUNT_WIDTH, ITEM_TEXTURE_HEIGHT,ITEM_TEXTURE_WIDTH + COUNT_WIDTH, ITEM_TEXTURE_HEIGHT);
        context.renderItem(stack, x + 3, y + 3);
        RenderUtils.drawTextHUD(context, amountStr, x + 22 + 1 + (5 + COUNT_WIDTH - CLIENT.font.width(amountStr) + 5) / 2, y + 7, color, false);

        setBoundingBox(x, y, ITEM_TEXTURE_WIDTH + COUNT_WIDTH, ITEM_TEXTURE_HEIGHT, color);
    }

    private void renderStackCountIconHUD(GuiGraphics context, Inventory playerInventory, ItemStack stack, int x, int y, float v, int color) {
        int stackAmount = getItemCount(playerInventory, stack);

        RenderUtils.drawTextureHUD(context, HAND_TEXTURE, x, y, 0.0F, v, COUNT_WIDTH + TEXTURE_WIDTH, TEXTURE_HEIGHT, COUNT_WIDTH + TEXTURE_WIDTH, 27, color);
        RenderUtils.drawTextHUD(context, Integer.toString(stackAmount), x + 19, y + 3, color, false);

        setBoundingBox(x, y, COUNT_WIDTH + TEXTURE_WIDTH, TEXTURE_HEIGHT, color);
    }

    private static int getItemCount(Inventory inventory, ItemStack stack) {
        int stackAmount = 0;

        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty() && areItemsAndComponentsEqual(item, stack))
                stackAmount += item.getCount();
        }

        return stackAmount;
    }

    private static boolean areItemsAndComponentsEqual(ItemStack x, ItemStack y) {
        return x.is(y.getItem()) && Objects.equals(x.getTag(), y.getTag());
    }

    @Override
    public int getBaseHUDWidth() {
        return handSettings.renderItem ? ITEM_TEXTURE_WIDTH : TEXTURE_WIDTH;
    }

    @Override
    public int getBaseHUDHeight() {
        return handSettings.renderItem ? ITEM_TEXTURE_HEIGHT :TEXTURE_HEIGHT;
    }

    // V as in the texture, where does left texture y point starts?
    // in the texture, i placed both Left and Right hand texture in the same file, but in a different place. this is just to differentiate them
    abstract int getV();

}
