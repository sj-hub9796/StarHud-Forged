package fin.starhud.hud.implementation;

import fin.starhud.Main;
import fin.starhud.config.hud.DaySettings;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class DayHUD extends AbstractHUD {

    private static final DaySettings DAY_SETTINGS = Main.settings.daySettings;

    private static final ResourceLocation DAY_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/day.png");

    private static final int TEXTURE_HEIGHT = 13;
    private static final int TEXTURE_WIDTH = 13 + 1 + 5 + 5;

    private long lastDay = -1;
    private int cachedTextWidth;
    private String cachedDayString;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public DayHUD() {
        super(DAY_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Day HUD";
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {

        long day = CLIENT.level.dayTime() / 24000L;

        // I cached these because textRendered.width() is expensive.
        // And since day count hardly updates at all, doing this is reasonable.
        if (day != lastDay) {
            lastDay = day;
            cachedDayString = Long.toString(day);
            cachedTextWidth = CLIENT.font.width(cachedDayString);
        }

        int xTemp = x - DAY_SETTINGS.base.growthDirectionX.getGrowthDirection(cachedTextWidth);
        int color = DAY_SETTINGS.color | 0xFF000000;

        RenderUtils.drawTextureHUD(context, DAY_TEXTURE, xTemp, y, 0.0F, 0, 13, TEXTURE_HEIGHT, 13,  TEXTURE_HEIGHT);
        RenderUtils.fillRoundedRightSide(context, xTemp + 14, y, xTemp + 14 + cachedTextWidth + 9, y + TEXTURE_HEIGHT, 0x80000000);
        RenderUtils.drawTextHUD(context, cachedDayString, xTemp + 19, y + 3, color, false);

        setBoundingBox(xTemp, y, 14 + cachedTextWidth + 9, TEXTURE_HEIGHT, color);
        return true;
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
