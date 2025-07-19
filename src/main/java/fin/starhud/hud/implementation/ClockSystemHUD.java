package fin.starhud.hud.implementation;

import fin.starhud.Main;
import fin.starhud.config.hud.ClockSystemSettings;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClockSystemHUD extends AbstractHUD {

    private static final ClockSystemSettings CLOCK_SYSTEM_SETTINGS = Main.settings.clockSettings.systemSetting;

    private static final ResourceLocation CLOCK_12_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/clock_12.png");
    private static final ResourceLocation CLOCK_24_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/clock_24.png");

    private static final int TEXTURE_HEIGHT = 13;

    private static final SimpleDateFormat CLOCK_24_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat CLOCK_12_FORMAT = new SimpleDateFormat("hh:mm a");

    private static String cachedSystemTimeString = buildSystemTime24String(System.currentTimeMillis());
    private static long cachedSystemMinute = -1;

    private static final int TEXTURE_SYSTEM_12_WIDTH = 65;
    private static final int TEXTURE_SYSTEM_24_WIDTH = 49;

    public ClockSystemHUD() {
        super(CLOCK_SYSTEM_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Clock System HUD";
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        // update each minute
        long currentTime = System.currentTimeMillis();
        long minute = currentTime / 60000;

        boolean use12Hour = CLOCK_SYSTEM_SETTINGS.use12Hour;

        // update on either a new minute or user updated the config
        if (minute != cachedSystemMinute) {
            cachedSystemMinute = minute;

            cachedSystemTimeString = use12Hour ?
                    buildSystemTime12String(currentTime) :
                    buildSystemTime24String(currentTime);
        }

        int color = CLOCK_SYSTEM_SETTINGS.color | 0xFF000000;
        super.boundingBox.setColor(color);

        if (use12Hour) {
            RenderUtils.drawTextureHUD(context, CLOCK_12_TEXTURE, x, y, 0.0F, 0.0F, TEXTURE_SYSTEM_12_WIDTH, TEXTURE_HEIGHT, TEXTURE_SYSTEM_12_WIDTH, TEXTURE_HEIGHT * 5, color);
            RenderUtils.drawTextHUD(context, cachedSystemTimeString, x + 19, y + 3, color, false);
            setBoundingBox(x, y, TEXTURE_SYSTEM_12_WIDTH, TEXTURE_HEIGHT, color);
        } else {
            RenderUtils.drawTextureHUD(context, CLOCK_24_TEXTURE, x, y, 0.0F, 0.0F, TEXTURE_SYSTEM_24_WIDTH, TEXTURE_HEIGHT, TEXTURE_SYSTEM_24_WIDTH, TEXTURE_HEIGHT * 5, color);
            RenderUtils.drawTextHUD(context, cachedSystemTimeString, x + 19, y + 3, color, false);
            setBoundingBox(x, y, TEXTURE_SYSTEM_24_WIDTH, TEXTURE_HEIGHT, color);
        }
        return true;
    }

    private static String buildSystemTime24String(long time) {
        return CLOCK_24_FORMAT.format(new Date(time));
    }

    private static String buildSystemTime12String(long time) {
        return CLOCK_12_FORMAT.format(new Date(time));
    }

    @Override
    public int getBaseHUDWidth() {
        return CLOCK_SYSTEM_SETTINGS.use12Hour ? TEXTURE_SYSTEM_12_WIDTH : TEXTURE_SYSTEM_24_WIDTH;
    }

    @Override
    public int getBaseHUDHeight() {
        return TEXTURE_HEIGHT;
    }

    @Override
    public void update() {
        super.update();
        cachedSystemMinute = -1;
    }
}
