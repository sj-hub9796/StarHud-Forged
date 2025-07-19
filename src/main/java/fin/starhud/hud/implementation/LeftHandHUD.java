package fin.starhud.hud.implementation;

import fin.starhud.Main;
import fin.starhud.config.hud.HandSettings;
import net.minecraft.world.entity.HumanoidArm;

public class LeftHandHUD extends AbstractHandHUD {

    private static final HandSettings LEFT_HAND_SETTINGS = Main.settings.handSettings.leftHandSettings;

    public LeftHandHUD() {
        super(LEFT_HAND_SETTINGS, HumanoidArm.LEFT);
    }

    @Override
    public String getName() {
        return "Left Hand HUD";
    }

    @Override
    int getV() {
        return 0;
    }
}
