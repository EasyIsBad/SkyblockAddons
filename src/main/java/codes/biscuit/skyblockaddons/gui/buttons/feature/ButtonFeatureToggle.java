package codes.biscuit.skyblockaddons.gui.buttons.feature;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;

public class ButtonFeatureToggle extends ButtonFeature {

    private static final ResourceLocation TOGGLE_INSIDE_CIRCLE = new ResourceLocation("skyblockaddons", "gui/toggleinsidecircle.png");
    private static final ResourceLocation TOGGLE_BORDER = new ResourceLocation("skyblockaddons", "gui/toggleborder.png");
    private static final ResourceLocation TOGGLE_INSIDE_BACKGROUND = new ResourceLocation("skyblockaddons", "gui/toggleinsidebackground.png");

    private static final int CIRCLE_PADDING_LEFT = 5;
    private static final int ANIMATION_SLIDE_DISTANCE = 12;
    private static final int ANIMATION_SLIDE_TIME = 150;

    private long animationButtonClicked = -1;

    /**
     * Create a button for toggling a feature on or off. This includes all the {@link Feature}s that have a proper ID.
     */
    public ButtonFeatureToggle(double x, double y, Feature feature) {
        super(0, (int)x, (int)y, "", feature);
        this.width = 31;
        this.height = 15;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        hovered = isHovered(mouseX, mouseY);

        GlStateManager.enableBlend();
        ColorUtils.bindColor(0xFF1E252E);
        mc.getTextureManager().bindTexture(TOGGLE_BORDER);
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,width,height,width,height, true);

        boolean enabled = feature.isEnabled();
        boolean remoteDisabled = feature.isRemoteDisabled();

        if (enabled) {
            ColorUtils.bindColor(36, 255, 98, remoteDisabled ? 25 : 255); // Green
        } else {
            ColorUtils.bindColor(222, 68, 76, remoteDisabled ? 25 : 255); // Red
        }

        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_BACKGROUND);
        DrawUtils.drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0, width, height, width, height, true);

        int startingX = getStartingPosition(enabled);
        int slideAnimationOffset = 0;

        if (animationButtonClicked != -1) {
            startingX = getStartingPosition(!enabled); // They toggled so start from the opposite side.

            int timeSinceOpen = (int)(System.currentTimeMillis() - animationButtonClicked);
            int animationTime = ANIMATION_SLIDE_TIME;
            if (timeSinceOpen > animationTime) {
                timeSinceOpen = animationTime;
            }

            slideAnimationOffset = ANIMATION_SLIDE_DISTANCE * timeSinceOpen/animationTime;
        }

        startingX += enabled ? slideAnimationOffset : -slideAnimationOffset;

        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TOGGLE_INSIDE_CIRCLE);
        DrawUtils.drawModalRectWithCustomSizedTexture(startingX, yPosition+3, 0, 0, 9, 9, 9, 9, true);
        GlStateManager.disableBlend();
    }

    private int getStartingPosition(boolean enabled) {
        if (!enabled)  {
            return xPosition+CIRCLE_PADDING_LEFT;
        } else {
            return getStartingPosition(false)+ANIMATION_SLIDE_DISTANCE;
        }
    }

    public void onClick() {
        this.animationButtonClicked = System.currentTimeMillis();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && this.feature != null && !this.feature.isRemoteDisabled()) {
            if (feature.isDisabled()) {
                feature.setEnabled(true);
                switch (feature) {
                    case DISCORD_RPC:
                        if (main.getUtils().isOnSkyblock()) {
                            main.getDiscordRPCManager().start();
                        }
                        break;
                    case ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT:
                        Feature.DISABLE_ENDERMAN_TELEPORTATION_EFFECT.setEnabled(true);
                        break;
                    case TURN_ALL_TEXTS_CHROMA:
                        main.getConfigValues().getChromaFeatures().add(feature);
                        break;
                }
            } else {
                feature.setEnabled(false);
                switch (feature) {
                    // Reset the vanilla bars when disabling these two features.
                    case HIDE_FOOD_ARMOR_BAR:
                        // The food gets automatically enabled, no need to include it.
                        GuiIngameForge.renderArmor = true;
                        break;
                    case HIDE_HEALTH_BAR:
                        GuiIngameForge.renderHealth = true;
                        break;
                    case FULL_INVENTORY_WARNING:
                        main.getInventoryUtils().setInventoryWarningShown(false);
                        break;
                    case DISCORD_RPC:
                        main.getDiscordRPCManager().stop();
                        break;
                    case DISABLE_ENDERMAN_TELEPORTATION_EFFECT:
                        Feature.ZEALOT_COUNTER_EXPLOSIVE_BOW_SUPPORT.setEnabled(true);
                        break;
                    case TURN_ALL_TEXTS_CHROMA:
                        main.getConfigValues().getChromaFeatures().remove(feature);
                        break;
                }
            }
            onClick();
            return true;
        }
        return false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandler) {
        if (!feature.isRemoteDisabled()) {
            super.playPressSound(soundHandler);
        }
    }
}
