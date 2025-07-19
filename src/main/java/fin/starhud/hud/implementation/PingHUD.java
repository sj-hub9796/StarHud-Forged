package fin.starhud.hud.implementation;

import fin.starhud.Main;
import fin.starhud.config.hud.PingSettings;
import fin.starhud.helper.RenderUtils;
import fin.starhud.hud.AbstractHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;

public class PingHUD extends AbstractHUD {

    private static final PingSettings PING_SETTINGS = Main.settings.pingSettings;

    private static final ResourceLocation PING_TEXTURE = ResourceLocation.tryBuild("starhud", "hud/ping.png");

    private static final int TEXTURE_WIDTH = 63;
    private static final int TEXTURE_HEIGHT = 13;

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public PingHUD() {
        super(PING_SETTINGS.base);
    }

    @Override
    public String getName() {
        return "Ping HUD";
    }


    @Override
    public boolean shouldRender() {
        return super.shouldRender()
                && !CLIENT.isLocalServer()
                && CLIENT.getConnection() != null
                && CLIENT.getConnection().getPlayerInfo(CLIENT.player.getUUID()) != null;
    }

    @Override
    public boolean renderHUD(GuiGraphics context) {
        PlayerInfo playerListEntry = CLIENT.getConnection().getPlayerInfo(CLIENT.player.getUUID());

        int currentPing = playerListEntry.getLatency();
        String pingStr = currentPing + " ms";

        // 0, 150, 300, 450
        int step = Math.min(currentPing / 150, 3);
        int color = getPingColor(step) | 0xFF000000;

        RenderUtils.drawTextureHUD(context, PING_TEXTURE, x, y, 0.0F, step * 13, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT * 4, color);
        RenderUtils.drawTextHUD(context, pingStr, x + 19, y + 3, color, false);

        setBoundingBox(x, y, TEXTURE_WIDTH, TEXTURE_HEIGHT, color);
        return true;
    }

    public static int getPingColor(int step) {
        return switch (step) {
            case 0 -> PING_SETTINGS.pingColor.first;
            case 1 -> PING_SETTINGS.pingColor.second;
            case 2 -> PING_SETTINGS.pingColor.third;
            case 3 -> PING_SETTINGS.pingColor.fourth;
            default -> 0xFFFFFFFF;
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
