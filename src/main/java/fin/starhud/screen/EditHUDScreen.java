package fin.starhud.screen;

import fin.starhud.Helper;
import fin.starhud.Main;
import fin.starhud.compat.ImmediatelyFastCompat;
import fin.starhud.config.BaseHUDSettings;
import fin.starhud.config.GeneralSettings;
import fin.starhud.config.Settings;
import fin.starhud.helper.*;
import fin.starhud.hud.AbstractHUD;
import fin.starhud.hud.HUDComponent;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EditHUDScreen extends Screen {

    private static final Minecraft CLIENT = Minecraft.getInstance();
    private static final GeneralSettings.EditHUDScreenSettings SETTINGS = Main.settings.generalSettings.screenSettings;

    private static final int PADDING = 25;
    private static final int WIDGET_WIDTH = 100;
    private static final int WIDGET_HEIGHT = 20;
    private static final int TEXT_FIELD_WIDTH = 40;
    private static final int SQUARE_WIDGET_LENGTH = 20;
    private static final int GAP = 5;

    public Screen parent;

    private final List<AbstractHUD> huds;
    private final List<HUDCoordinate> oldSettings;

    private boolean dragging = false;
    private AbstractHUD selectedHUD = null;

    private boolean isHelpActivated = false;
    private boolean isMoreOptionActivated = false;

    // widgets
    private EditBox xField;
    private EditBox yField;
    private Button alignmentXButton;
    private Button alignmentYButton;
    private Button directionXButton;
    private Button directionYButton;
    private Button scaleButton;

    private static final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

    private static final String[] HELP_KEYS = {
            "[Arrow Keys]",
            "[⇧ Shift + Arrows]",
            isMac ? "[⌘ Cmd + Arrows]" : "[Ctrl + Arrows]",
            "[⌥ Alt + Arrows]",
            isMac ? "[⌘ Cmd + R]" : "[Ctrl + R]",
            "[Click]",
            "[Drag]"
    };

    private static final String[] HELP_INFOS = {
            "Move HUD by 1",
            "Move HUD by 5",
            "Change Alignment",
            "Change Growth Direction",
            "Revert All Changes",
            "Select HUD",
            "Move HUD"
    };

    private static final int HELP_HEIGHT = 5 + (HELP_KEYS.length * 9) + 5;

    public EditHUDScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;

        HUDComponent.getInstance().setShouldRenderInGameScreen(false);
        huds = new ArrayList<>(HUDComponent.getInstance().huds);
        huds.add(HUDComponent.getInstance().effectHUD);

        oldSettings = new ArrayList<>();
        for (AbstractHUD p : huds) {
            BaseHUDSettings settings = p.getSettings();
            oldSettings.add(new HUDCoordinate(settings.x, settings.y, settings.originX, settings.originY, settings.growthDirectionX, settings.growthDirectionY, settings.scale));
        }
    }

    @Override
    protected void init() {

        final int CENTER_X = this.width / 2;
        final int CENTER_Y = this.height / 2 + (PADDING / 2);

        HUDComponent.getInstance().updateAll();

        xField = new EditBox(
                CLIENT.font,
                CENTER_X - TEXT_FIELD_WIDTH - (SQUARE_WIDGET_LENGTH / 2) - GAP,
                CENTER_Y - PADDING,
                TEXT_FIELD_WIDTH,
                WIDGET_HEIGHT,
                Component.literal("X")
        );
        yField = new EditBox(
                CLIENT.font,
                CENTER_X + (SQUARE_WIDGET_LENGTH / 2) + GAP,
                CENTER_Y - PADDING,
                TEXT_FIELD_WIDTH,
                WIDGET_HEIGHT,
                Component.literal("Y")
        );

        alignmentXButton = Button.builder(
                Component.literal("X Alignment: N/A"),
                button -> {
                    if (selectedHUD == null) return;
                    selectedHUD.getSettings().originX = selectedHUD.getSettings().originX.next();
                    selectedHUD.getSettings().growthDirectionX = selectedHUD.getSettings().growthDirectionX.recommendedScreenAlignment(selectedHUD.getSettings().originX);
                    selectedHUD.update();
                    alignmentXButton.setMessage(Component.literal("X Alignment: " + selectedHUD.getSettings().originX));
                }
        ).bounds(CENTER_X - WIDGET_WIDTH - (SQUARE_WIDGET_LENGTH / 2) - GAP, CENTER_Y - PADDING * 2, WIDGET_WIDTH, WIDGET_HEIGHT).build();

        alignmentYButton = Button.builder(
                Component.literal("Y Alignment: N/A"),
                button -> {
                    if (selectedHUD == null) return;
                    selectedHUD.getSettings().originY = selectedHUD.getSettings().originY.next();
                    selectedHUD.getSettings().growthDirectionY = selectedHUD.getSettings().growthDirectionY.recommendedScreenAlignment(selectedHUD.getSettings().originY);
                    selectedHUD.update();
                    alignmentYButton.setMessage(Component.literal("Y Alignment: " + selectedHUD.getSettings().originY));
                }
        ).bounds(CENTER_X + (SQUARE_WIDGET_LENGTH / 2) + GAP, CENTER_Y - PADDING * 2, WIDGET_WIDTH, WIDGET_HEIGHT).build();

        scaleButton = Button.builder(
                Component.literal("N/A"),
                button -> {
                    if (selectedHUD == null) return;
                    selectedHUD.getSettings().scale = (selectedHUD.getSettings().scale + 1) % 7;
                    selectedHUD.update();
                    scaleButton.setMessage(Component.literal(Integer.toString(selectedHUD.getSettings().scale)));
                }
        ).tooltip(Tooltip.create(Component.literal("Scale"))).bounds(CENTER_X - (SQUARE_WIDGET_LENGTH / 2), CENTER_Y - PADDING * 2, SQUARE_WIDGET_LENGTH, SQUARE_WIDGET_LENGTH).build();

        directionXButton = Button.builder(
                Component.literal("X Direction: N/A"),
                button -> {
                    if (selectedHUD == null) return;
                    selectedHUD.getSettings().growthDirectionX = selectedHUD.getSettings().growthDirectionX.next();
                    selectedHUD.update();
                    directionXButton.setMessage(Component.literal("X Direction: " + selectedHUD.getSettings().growthDirectionX));
                }
        ).bounds(CENTER_X - WIDGET_WIDTH - (SQUARE_WIDGET_LENGTH / 2) - GAP, CENTER_Y - PADDING * 3, WIDGET_WIDTH, WIDGET_HEIGHT).build();

        directionYButton = Button.builder(
                Component.literal("Y Direction: N/A"),
                button -> {
                    if (selectedHUD == null) return;
                    selectedHUD.getSettings().growthDirectionY = selectedHUD.getSettings().growthDirectionY.next();
                    selectedHUD.update();
                    directionYButton.setMessage(Component.literal("Y Direction: " + selectedHUD.getSettings().growthDirectionY));
                }
        ).bounds(CENTER_X + (SQUARE_WIDGET_LENGTH / 2) + GAP, CENTER_Y - PADDING * 3, WIDGET_WIDTH, WIDGET_HEIGHT).build();

        xField.setResponder(text -> {
            if (selectedHUD == null) return;
            try {
                selectedHUD.getSettings().x = Integer.parseInt(text);
                selectedHUD.update();
            } catch (NumberFormatException ignored) {}
        });

        yField.setResponder(text -> {
            if (selectedHUD == null) return;
            try {
                selectedHUD.getSettings().y = Integer.parseInt(text);
                selectedHUD.update();
            } catch (NumberFormatException ignored) {}
        });

        Button helpButton = Button.builder(
                Component.literal("?"),
                button -> {
                    isHelpActivated = !isHelpActivated;
                    onHelpSwitched();
                }
        )
                .tooltip(Tooltip.create(Component.literal("Help")))
                .bounds(CENTER_X - SQUARE_WIDGET_LENGTH - (GAP / 2), this.height - SQUARE_WIDGET_LENGTH - GAP, SQUARE_WIDGET_LENGTH, SQUARE_WIDGET_LENGTH)
                .build();

        Button moreOptionButton = Button.builder(
                Component.literal("+"),
                button -> {
                    isMoreOptionActivated = !isMoreOptionActivated;
                    onMoreOptionSwitched();
                }
        )
                .tooltip(Tooltip.create(Component.literal("More Options")))
                .bounds(CENTER_X + (GAP / 2), this.height - SQUARE_WIDGET_LENGTH - GAP, SQUARE_WIDGET_LENGTH, SQUARE_WIDGET_LENGTH)
                .build();


        int terminatorWidth = 70;
        addRenderableWidget(Button.builder(Component.literal("Save & Quit"), button -> {
            AutoConfig.getConfigHolder(Settings.class).save();
            onClose();
        }).bounds(CENTER_X + (GAP / 2) + SQUARE_WIDGET_LENGTH + GAP, this.height - WIDGET_HEIGHT - GAP, terminatorWidth, WIDGET_HEIGHT).build());

        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> {
            close();
        }).bounds(CENTER_X - (GAP / 2) - terminatorWidth - SQUARE_WIDGET_LENGTH - GAP, this.height - WIDGET_HEIGHT - GAP, terminatorWidth, WIDGET_HEIGHT).build());

        alignmentXButton.visible = false;
        directionXButton.visible = false;
        alignmentYButton.visible = false;
        directionYButton.visible = false;
        xField.visible = false;
        yField.visible = false;
        scaleButton.visible = false;

        addRenderableWidget(helpButton);
        addRenderableWidget(moreOptionButton);

        addRenderableWidget(alignmentXButton);
        addRenderableWidget(alignmentYButton);
        addRenderableWidget(directionXButton);
        addRenderableWidget(directionYButton);
        addRenderableWidget(scaleButton);
        addRenderableWidget(xField);
        addRenderableWidget(yField);

        updateFieldsFromSelectedHUD();
    }

    private void updateFieldsFromSelectedHUD() {
        if (xField == null || yField == null) return;

        if (selectedHUD == null) {
            xField.setEditable(false);
            yField.setEditable(false);
            xField.setValue("N/A");
            yField.setValue("N/A");

            alignmentXButton.setMessage(Component.literal("X Alignment: N/A"));
            directionXButton.setMessage(Component.literal("X Direction: N/A"));
            alignmentYButton.setMessage(Component.literal("Y Alignment: N/A"));
            directionYButton.setMessage(Component.literal("Y Direction: N/A"));

            scaleButton.setMessage(Component.literal("N/A"));

            alignmentXButton.active = false;
            directionXButton.active = false;
            alignmentYButton.active = false;
            directionYButton.active = false;
            scaleButton.active = false;
        } else {
            BaseHUDSettings settings = selectedHUD.getSettings();
            xField.setValue(String.valueOf(settings.x));
            yField.setValue(String.valueOf(settings.y));

            alignmentXButton.setMessage(Component.literal("X Alignment: " + selectedHUD.getSettings().originX));
            directionXButton.setMessage(Component.literal("X Direction: " + selectedHUD.getSettings().growthDirectionX));
            alignmentYButton.setMessage(Component.literal("Y Alignment: " + selectedHUD.getSettings().originY));
            directionYButton.setMessage(Component.literal("Y Direction: " + selectedHUD.getSettings().growthDirectionY));
            scaleButton.setMessage(Component.literal(Integer.toString(selectedHUD.getSettings().scale)));

            alignmentXButton.active = true;
            directionXButton.active = true;
            alignmentYButton.active = true;
            directionYButton.active = true;
            scaleButton.active = true;
            xField.setEditable(true);
            yField.setEditable(true);

            if (isMoreOptionActivated) {
                alignmentXButton.visible = true;
                directionXButton.visible = true;
                alignmentYButton.visible = true;
                directionYButton.visible = true;
                xField.visible = true;
                yField.visible = true;
                scaleButton.visible = true;
            }
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {

        if (SETTINGS.shouldBatchHUDWithImmediatelyFast && Helper.isModPresent("immediatelyfast")) {
            ImmediatelyFastCompat.beginHudBatching();
            renderElements(context, mouseX, mouseY, delta);
            ImmediatelyFastCompat.endHudBatching();
        } else {
            renderElements(context, mouseX, mouseY, delta);
        }

    }

    public void renderElements(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x80000000);

        super.render(context, mouseX, mouseY, delta);

        // draw help
        if (isHelpActivated) {
            final int CENTER_X = this.width / 2;
            final int CENTER_Y = this.height / 2 + (PADDING / 2);
            renderHelp(context, CENTER_X, CENTER_Y + GAP);
            if (selectedHUD != null)
                renderHUDInformation(context, CENTER_X, CENTER_Y + GAP  + HELP_HEIGHT + GAP);
        }

        // draw X and Y next to their textField.
        if (xField.isVisible() && yField.isVisible()) {
            context.drawString(CLIENT.font, "X:", xField.getX() - 5 - 2 - 3, xField.getY() + 6, 0xFFFFFFFF, true);
            context.drawString(CLIENT.font, ":Y", yField.getX() + yField.getWidth() + 3, yField.getY() + 6, 0xFFFFFFFF, true);
        }

        // draw all visible hud bounding boxes.
        renderBoundingBoxes(context, mouseX, mouseY);
    }

    private void renderBoundingBoxes(GuiGraphics context, int mouseX, int mouseY) {
        for (AbstractHUD p: huds) {
            if (!p.shouldRender()) continue; // if not rendered
            if (!p.render(context)) continue; // if ALSO not rendered (either failed or no information to render)

            Box boundingBox = p.getBoundingBox();

            int x = boundingBox.getX();
            int y = boundingBox.getY();
            int width = boundingBox.width();
            int height = boundingBox.getHeight();
            int color = boundingBox.getColor();
            int selectedColor = SETTINGS.selectedBoxColor | 0x80000000;

            if (p.isScaled()) {
                context.pose().pushPose();
                p.setHUDScale(context);

                context.renderOutline(x, y, width, height, color);
                if (isHovered(x, y, width, height, mouseX, mouseY, p.getSettings().scale)) {
                    context.fill(x, y, x + width, y + height, (color & 0x00FFFFFF) | 0x80000000);
                }
                if (p == selectedHUD) {
                    context.fill(x, y, x + width, y + height, selectedColor);
                }
                context.pose().popPose();
                continue;
            }

            context.renderOutline(x, y, width, height, color);
            if (isHovered(x, y, width, height, mouseX, mouseY, p.getSettings().scale)) {
                context.fill(x, y, x + width, y + height, (color & 0x00FFFFFF) | 0x80000000);
            }
            if (p == selectedHUD) {
                context.fill(x, y, x + width, y + height, selectedColor);
            }
        }
    }

    private void renderHelp(GuiGraphics context, int x, int y) {
        int padding = 5;

        int lineHeight = CLIENT.font.lineHeight;
        int maxKeyWidth = CLIENT.font.width(HELP_KEYS[1]);
        int maxInfoWidth = CLIENT.font.width(HELP_INFOS[3]);

        int width = padding + maxKeyWidth + padding + 1 + padding + maxInfoWidth + padding;
        int height = padding + (lineHeight * HELP_KEYS.length) + padding - 2;

        x -= width / 2;
        y -= padding;

        context.fill(x, y, x + width, y + height, 0x80000000);

        for (int i = 0; i < HELP_KEYS.length; ++i) {
            String key = HELP_KEYS[i];
            String info = HELP_INFOS[i];

            context.drawString(CLIENT.font, key, x + padding, y + padding, 0xFFFFFFFF, false);
            context.drawString(CLIENT.font, info, x + padding + maxKeyWidth + padding + 1 + padding, y + padding, 0xFFFFFFFF, false);

            y += lineHeight;
        }
    }

    private void renderHUDInformation(GuiGraphics context, int x, int y) {
        String text = selectedHUD.getName();
        int textWidth = CLIENT.font.width(text);
        int padding = 5;

        x -= (textWidth / 2);

        context.fill(x - padding, y - padding, x + textWidth + padding, y + CLIENT.font.lineHeight - 2 + padding, 0x80000000);
        context.drawString(CLIENT.font, text, x, y, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (AbstractHUD hud : huds) {
                if (!hud.shouldRender())
                    continue;

                Box boundingBox = hud.getBoundingBox();
                int scale = hud.getSettings().scale;
                int x = boundingBox.getX();
                int y = boundingBox.getY();
                int width = boundingBox.width();
                int height = boundingBox.getHeight();


                if (isHovered(x, y, width, height, (int) mouseX, (int) mouseY, scale)) {
                    selectedHUD = hud;
                    dragging = true;
                    updateFieldsFromSelectedHUD();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            xField.setValue(String.valueOf(selectedHUD.getSettings().x));
            yField.setValue(String.valueOf(selectedHUD.getSettings().y));
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private double accumulatedX = 0, accumulatedY = 0;

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && selectedHUD != null && button == 0) {
            double scaleFactor = selectedHUD.getSettings().getScaledFactor();

            accumulatedX += deltaX * scaleFactor;
            accumulatedY += deltaY * scaleFactor;

            int dx = 0;
            int dy = 0;

            if (Math.abs(accumulatedX) >= 1) {
                dx = (int) accumulatedX;
                accumulatedX -= dx;
            }

            if (Math.abs(accumulatedY) >= 1) {
                dy = (int) accumulatedY;
                accumulatedY -= dy;
            }

            if (dx != 0 || dy != 0) {
                selectedHUD.getSettings().x += dx;
                selectedHUD.getSettings().y += dy;

                selectedHUD.update();
            }

            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedHUD != null) {

            BaseHUDSettings settings = selectedHUD.getSettings();

            boolean handled = false;
            boolean isCtrl = isMac
                    ? (modifiers & GLFW.GLFW_MOD_SUPER) != 0
                    : (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            boolean isShift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            boolean isAlt = (modifiers & GLFW.GLFW_MOD_ALT) != 0;

            int step = isShift ? 5 : 1;

            switch (keyCode) {
                case GLFW.GLFW_KEY_LEFT -> {
                    if (isCtrl) {
                        settings.originX = settings.originX.prev();
                        settings.growthDirectionX = settings.growthDirectionX.recommendedScreenAlignment(settings.originX);
                    }
                    else if (isAlt) settings.growthDirectionX = settings.growthDirectionX.prev();
                    else settings.x -= step;

                    handled = true;
                }

                case GLFW.GLFW_KEY_RIGHT -> {
                    if (isCtrl) {
                        settings.originX = settings.originX.next();
                        settings.growthDirectionX = settings.growthDirectionX.recommendedScreenAlignment(settings.originX);
                    }
                    else if (isAlt) settings.growthDirectionX = settings.growthDirectionX.next();
                    else settings.x += step;

                    handled = true;
                }

                case GLFW.GLFW_KEY_UP -> {
                    if (isCtrl) {
                        settings.originY = settings.originY.prev();
                        settings.growthDirectionY = settings.growthDirectionY.recommendedScreenAlignment(settings.originY);
                    }
                    else if (isAlt) {
                        settings.growthDirectionY = settings.growthDirectionY.prev();
                    }
                    else settings.y -= step;

                    handled = true;
                }

                case GLFW.GLFW_KEY_DOWN -> {
                    if (isCtrl) {
                        settings.originY = settings.originY.next();
                        settings.growthDirectionY = settings.growthDirectionY.recommendedScreenAlignment(settings.originY);
                    }
                    else if (isAlt) settings.growthDirectionY = settings.growthDirectionY.next();
                    else settings.y += step;

                    handled = true;
                }

                case GLFW.GLFW_KEY_R -> {
                    if (isCtrl) {
                        revertChanges();
                        handled = true;
                    }
                }

                case GLFW.GLFW_KEY_MINUS ->  {
                    if (!yField.isFocused() && !xField.isFocused()) {
                        settings.scale = (settings.scale + 6) % 7;
                        handled = true;
                    }
                }

                case GLFW.GLFW_KEY_EQUAL ->  {
                    if (isShift && !yField.isFocused() && !xField.isFocused()) {
                        settings.scale = (settings.scale + 1) % 7;
                        handled = true;
                    }
                }
            }

            if (handled) {
                selectedHUD.update();
                updateFieldsFromSelectedHUD();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean isHovered(int x, int y, int width, int height, int mouseX, int mouseY, int HUDScale) {
        float scale = HUDScale == 0 ? 1 : (float) CLIENT.getWindow().getGuiScale() / HUDScale;
        int scaledMouseX = (int) (mouseX * scale);
        int scaledMouseY = (int) (mouseY * scale);
        return scaledMouseX >= x && scaledMouseX <= x + width &&
                scaledMouseY >= y && scaledMouseY <= y + height;
    }

    private boolean isDirty() {
        for (int i = 0; i < huds.size(); i++) {
            AbstractHUD hud = huds.get(i);
            BaseHUDSettings current = hud.getSettings();
            HUDCoordinate original = oldSettings.get(i);

            if (current.x != original.x || current.y != original.y
                    || current.originX != original.alignmentX
                    || current.originY != original.alignmentY
                    || current.scale != original.scale) {
                return true;
            }
        }
        return false;
    }

    private void revertChanges() {
        for (int i = 0; i < huds.size(); ++i) {
            BaseHUDSettings current = huds.get(i).getSettings();
            HUDCoordinate original = oldSettings.get(i);

            current.x = original.x;
            current.y = original.y;
            current.originX = original.alignmentX;
            current.originY = original.alignmentY;
            current.growthDirectionX = original.growthDirectionX;
            current.growthDirectionY = original.growthDirectionY;
            current.scale = original.scale;
        }
        AutoConfig.getConfigHolder(Settings.class).save();
    }

    @Override
    public void onClose() {
        if (isDirty()) {
            this.minecraft.setScreen(new ConfirmScreen(
                    result -> {
                        if (result) {
                            revertChanges();
                            this.close();
                        } else {
                            this.minecraft.setScreen(this);
                        }
                    },
                    Component.literal("Discard Changes?"),
                    Component.literal("You have unsaved changes. Do you want to discard them?")
            ));
        } else {
            this.close();
        }
    }

    public void close() {
        this.minecraft.setScreen(this.parent);
        HUDComponent.getInstance().setShouldRenderInGameScreen(true);
    }

    private void onHelpSwitched() {
    }

    private void onMoreOptionSwitched() {
        if (isMoreOptionActivated) {
            alignmentXButton.visible = true;
            directionXButton.visible = true;
            alignmentYButton.visible = true;
            directionYButton.visible = true;
            xField.visible = true;
            yField.visible = true;
            scaleButton.visible = true;
        } else {
            alignmentXButton.visible = false;
            directionXButton.visible = false;
            alignmentYButton.visible = false;
            directionYButton.visible = false;
            xField.visible = false;
            yField.visible = false;
            scaleButton.visible = false;
        }
    }

    private static class HUDCoordinate {
        int x, y;
        ScreenAlignmentX alignmentX;
        ScreenAlignmentY alignmentY;
        GrowthDirectionX growthDirectionX;
        GrowthDirectionY growthDirectionY;
        int scale;

        public HUDCoordinate(int x, int y, ScreenAlignmentX alignmentX, ScreenAlignmentY alignmentY, GrowthDirectionX growthDirectionX, GrowthDirectionY growthDirectionY, int scale) {
            this.x = x;
            this.y = y;
            this.alignmentX = alignmentX;
            this.alignmentY = alignmentY;
            this.growthDirectionX = growthDirectionX;
            this.growthDirectionY = growthDirectionY;
            this.scale = scale;
        }
    }
}
