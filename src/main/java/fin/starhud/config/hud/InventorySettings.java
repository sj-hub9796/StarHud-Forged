package fin.starhud.config.hud;

import fin.starhud.config.BaseHUDSettings;
import fin.starhud.helper.GrowthDirectionX;
import fin.starhud.helper.GrowthDirectionY;
import fin.starhud.helper.ScreenAlignmentX;
import fin.starhud.helper.ScreenAlignmentY;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class InventorySettings {

    @ConfigEntry.Gui.TransitiveObject
    public BaseHUDSettings base = new BaseHUDSettings(false, -5, 0, ScreenAlignmentX.RIGHT, ScreenAlignmentY.MIDDLE, GrowthDirectionX.LEFT, GrowthDirectionY.MIDDLE);

    public boolean drawVertical = true;

}
