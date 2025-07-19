package fin.starhud.hud;

import net.minecraft.client.gui.GuiGraphics;

public interface HUDInterface {

    boolean render(GuiGraphics context);

    boolean shouldRender();

    void update();
}
