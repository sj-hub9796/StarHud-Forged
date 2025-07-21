package fin.starhud.hud.implementation;

import fin.starhud.Helper;
import fin.starhud.Main;
import fin.starhud.config.hud.TargetedCrosshairSettings;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;


// HUD similar to JADE's. TargetedCrosshairHUD.
public class TargetedCrosshairHUD extends AbstractHUD {

    private static final TargetedCrosshairSettings TARGETED_CROSSHAIR_SETTINGS = Main.settings.targetedCrosshairSettings;

    private static final ResourceLocation ICON_BACKGROUND_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/item.png");
    private static final ResourceLocation ENTITY_ICON_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/targeted_icon_entity.png");

    // left padding + texture + right padding
    private static final int ICON_BACKGROUND_WIDTH = 3 + 16 + 3;
    private static final int ICON_BACKGROUND_HEIGHT = 3 + 16 + 3;

    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final int BASE_HUD_WIDTH =
            ICON_BACKGROUND_WIDTH
                    + 1     // gap
                    + 5     // left text padding
                    + 5;    // right text padding

    private static final int BASE_HUD_HEIGHT = ICON_BACKGROUND_HEIGHT;

    public TargetedCrosshairHUD() {
        super(TARGETED_CROSSHAIR_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Targeted Crosshair HUD";
    }

    @Override
    public boolean shouldRender() {
        return super.shouldRender()
                && CLIENT.hitResult != null
                && CLIENT.hitResult.getType() != HitResult.Type.MISS;
    }

    public static boolean shouldHUDRender() {

        if (!TARGETED_CROSSHAIR_SETTINGS.base.shouldRender())
            return false;

        return CLIENT.hitResult != null && CLIENT.hitResult.getType() != HitResult.Type.MISS;
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        return switch (CLIENT.hitResult.getType()) {
            case BLOCK -> renderBlockInfoHUD(context);
            case ENTITY -> renderEntityInfoHUD(context);
            default -> false;
        };
    }

    private Block cachedBlock = null;
    private FormattedCharSequence cachedBlockName = null;
    private String cachedBlockModName = null;
    private int cachedBlockMaxWidth = -1;

    public boolean renderBlockInfoHUD(GuiGraphics context) {
        BlockPos pos = ((BlockHitResult) CLIENT.hitResult).getBlockPos();

        BlockState blockState = CLIENT.level.getBlockState(pos);
        Block block = blockState.getBlock();
        Item blockItem = block.asItem();
        ItemStack blockStack = blockItem.getDefaultInstance();

        if (!block.equals(cachedBlock)) {
            cachedBlock = block;

            if (blockItem == Items.AIR) cachedBlockName = Component.translatable(block.getDescriptionId()).getVisualOrderText();
            else cachedBlockName = blockStack.getHoverName().getVisualOrderText();

            cachedBlockModName = Helper.getModName(ForgeRegistries.BLOCKS.getKey(block));

            int blockNameWidth = CLIENT.font.width(cachedBlockName);
            int modNameWidth = CLIENT.font.width(cachedBlockModName);
            cachedBlockMaxWidth = Math.max(modNameWidth, blockNameWidth) - 1;
        }

        int xTemp = x - TARGETED_CROSSHAIR_SETTINGS.base.growthDirectionX.getGrowthDirection(cachedBlockMaxWidth);

        RenderUtils.drawTextureHUD(
                context,
                ICON_BACKGROUND_TEXTURE,
                xTemp, y,
                0, 0,
                ICON_BACKGROUND_WIDTH, ICON_BACKGROUND_HEIGHT,
                ICON_BACKGROUND_WIDTH, ICON_BACKGROUND_HEIGHT
        );
        RenderUtils.fillRoundedRightSide(
                context,
                xTemp + ICON_BACKGROUND_WIDTH + 1, y,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5 + cachedBlockMaxWidth + 5, y + ICON_BACKGROUND_HEIGHT,
                0x80000000
        );

        context.renderItem(blockStack, xTemp + 3, y + 3);
        RenderUtils.drawTextHUD(
                context,
                cachedBlockName,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5, y + 3,
                TARGETED_CROSSHAIR_SETTINGS.targetedNameColor | 0xFF000000,
                false
        );
        RenderUtils.drawTextHUD(
                context,
                cachedBlockModName,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5,
                y + ICON_BACKGROUND_HEIGHT - 3 - 7,
                TARGETED_CROSSHAIR_SETTINGS.modNameColor | 0xFF000000,
                false
        );

        setBoundingBox(xTemp, y, ICON_BACKGROUND_WIDTH + 1 + 5 + cachedBlockMaxWidth + 5, ICON_BACKGROUND_HEIGHT);
        return true;
    }

    private Entity cachedTargetedEntity = null;
    private FormattedCharSequence cachedEntityName = null;
    private String cachedEntityModName = null;
    private int cachedEntityMaxWidth = -1;
    private int cachedIndex = -1;

    public boolean renderEntityInfoHUD(GuiGraphics context) {
        Entity targetedEntity = ((EntityHitResult) CLIENT.hitResult).getEntity();

        if (!targetedEntity.equals(cachedTargetedEntity)) {
            cachedTargetedEntity = targetedEntity;
            cachedEntityName = targetedEntity.getName().getVisualOrderText();
            cachedEntityModName = Helper.getModName(ForgeRegistries.ENTITY_TYPES.getKey(targetedEntity.getType()));

            int entityNameWidth = CLIENT.font.width(cachedEntityName);
            int modNameWidth = CLIENT.font.width(cachedEntityModName);
            cachedEntityMaxWidth = Math.max(entityNameWidth, modNameWidth) - 1;

            cachedIndex = getEntityIconIndex(targetedEntity);
        }

        int xTemp = x - TARGETED_CROSSHAIR_SETTINGS.base.growthDirectionX.getGrowthDirection(cachedEntityMaxWidth);
        int color = getEntityIconColor(cachedIndex) | 0xFF000000;

        RenderUtils.drawTextureHUD(
                context,
                ENTITY_ICON_TEXTURE,
                xTemp, y,
                0, 22 * cachedIndex,
                ICON_BACKGROUND_WIDTH, ICON_BACKGROUND_HEIGHT,
                ICON_BACKGROUND_WIDTH, ICON_BACKGROUND_HEIGHT * 5,
                color
        );
        RenderUtils.fillRoundedRightSide(
                context,
                xTemp + ICON_BACKGROUND_WIDTH + 1, y,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5 + cachedEntityMaxWidth + 5, y + ICON_BACKGROUND_HEIGHT,
                0x80000000
        );

        RenderUtils.drawTextHUD(
                context,
                cachedEntityName,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5, y + 3,
                color,
                false
        );
        RenderUtils.drawTextHUD(
                context,
                cachedEntityModName,
                xTemp + ICON_BACKGROUND_WIDTH + 1 + 5, y + ICON_BACKGROUND_HEIGHT - 3 - 7,
                TARGETED_CROSSHAIR_SETTINGS.modNameColor | 0xFF000000,
                false
        );

        setBoundingBox(xTemp, y, ICON_BACKGROUND_WIDTH + 1 + 5 + cachedEntityMaxWidth + 5, ICON_BACKGROUND_HEIGHT, color);
        return true;
    }

    private int getEntityIconIndex(Entity e) {
        if (isHostileMob(e)) return 0;
        else if (isAngerableMob(e)) return 1;
        else if (isPassiveMob(e)) return 2;
        else if (isPlayerEntity(e)) return 3;
        else return 4;
    }

    private int getEntityIconColor(int index) {
        return switch (index) {
            case 0 -> TARGETED_CROSSHAIR_SETTINGS.entityColors.hostile;
            case 1 -> TARGETED_CROSSHAIR_SETTINGS.entityColors.angerable;
            case 2 -> TARGETED_CROSSHAIR_SETTINGS.entityColors.passive;
            case 3 -> TARGETED_CROSSHAIR_SETTINGS.entityColors.player;
            default -> TARGETED_CROSSHAIR_SETTINGS.entityColors.unknown;
        };
    }

    private static boolean isHostileMob(Entity e) {
        if (e instanceof EnderDragon) return true;
        else if (e instanceof EnderDragonPart) return true;
        else if (e instanceof Enemy) return true;
        else return false;

    }

    private static boolean isAngerableMob(Entity e) {
        return e instanceof NeutralMob;
    }

    private static boolean isPassiveMob(Entity e) {
        if (e instanceof AgeableMob) return true;
        else if (e instanceof WaterAnimal) return true;
        else if (e instanceof Allay) return true;
        else if (e instanceof SnowGolem) return true;
        else return false;
    }

    private static boolean isPlayerEntity(Entity e) {
        return e instanceof Player;
    }

    @Override
    public int getBaseHUDWidth() {
        return BASE_HUD_WIDTH;
    }

    @Override
    public int getBaseHUDHeight() {
        return BASE_HUD_HEIGHT;
    }
}
