package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mc.MobAggroCategory;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformScreen extends WasdAllowingScreen {
    public static EntityType<?> chosenTypeTmp = null;
    
    private static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");
    
    private final BiMap<EntityType<?>, SelectorWidget> entityTypeOptions = HashBiMap.create();
    private Optional<SelectorWidget> prevSelected = Optional.empty();
    private Optional<SelectorWidget> currentlyHovered = Optional.empty();
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    
    public ChooseLifeformScreen() {
        this(InputHandler.getInstance().tmp.getKey().getValue());
    }
    
    public ChooseLifeformScreen(int keyHeld) {
        super(StringTextComponent.EMPTY);
        this.keyHeld = keyHeld;
    }
    
    
    
    @Override
    protected void init() {
        super.init();
//        this.currentlyHovered = this.previousHovered.isPresent() ? this.previousHovered : Mode.getFromGameType(this.minecraft.gameMode.getPlayerMode());

        Collection<EntityType<?>> entityTypes = ForgeRegistries.ENTITIES.getValues()
                .stream().filter(GoldExperienceChooseLifeform::isValidLifeform)
                .collect(Collectors.toList());
        int x = 8;
        int y = 8;
        for (EntityType<?> entityType : entityTypes) {
            SelectorWidget widget = new SelectorWidget(entityType, x, y);
            entityTypeOptions.put(entityType, widget);
            if (entityType == chosenTypeTmp) {
                prevSelected = Optional.of(widget);
            }
            y += 30;
            if (y >= height - 24) {
                x += 30;
                y = 8;
            }
        }
        MobAggroCategory.requestCategoryOnClient(entityTypes);
    }
    
    
    
    private int ticks = 0;
    private final int keyHeld;
    private ScreenCloseMode mode = ScreenCloseMode.CLICK;
    private boolean holdsButton = true;
    
    @Override
    public void tick() {
        if (holdsButton && ++ticks == 4) {
            mode = ScreenCloseMode.HOLD;
        }
    }

    private boolean checkToClose() {
        if (holdsButton && !isKeyBeingHeld()) {
            holdsButton = false;
        }
        
        return !holdsButton && mode == ScreenCloseMode.HOLD;
    }
    
    private boolean isKeyBeingHeld() {
        return InputMappings.isKeyDown(minecraft.getWindow().getWindow(), keyHeld);
    }
    
    
    
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.checkToClose()) {
            chooseAndClose();
        }
        else {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
            
            if (!setFirstMousePos) {
                firstMouseX = mouseX;
                firstMouseY = mouseY;
                setFirstMousePos = true;
            }
            boolean movedMouse = firstMouseX != mouseX || firstMouseY != mouseY;
            
            renderHoveredTooltip(matrixStack);
            
            for (SelectorWidget entityTypeWidget : entityTypeOptions.values()) {
                entityTypeWidget.render(matrixStack, mouseX, mouseY, partialTicks);
                currentlyHovered.ifPresent(widget -> {
                    entityTypeWidget.setSelected(widget == entityTypeWidget);
                });
                if (movedMouse && entityTypeWidget.isHovered()) {
                    currentlyHovered = Optional.of(entityTypeWidget);
                }
            }
        }
    }

    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.0");
    private void renderHoveredTooltip(MatrixStack matrixStack) {
        currentlyHovered.ifPresent(widget -> {
            int x = widget.x;
            int y = widget.y;
            renderTooltip(matrixStack, widget.getMessage(), x, y);
            
            
            List<ITextComponent> rightSideInfo = new ArrayList<>();
            rightSideInfo.add(widget.getMessage());
            
            MobAggroCategory aggroCategory = MobAggroCategory.getCategoryOnClient(widget.entityType);
            if (aggroCategory != null) {
                rightSideInfo.add(aggroCategory.getName());
            }
            
            Entity entity = EntityTypeToInstance.getEntityInstance(widget.entityType);
            String width = SIZE_FORMAT.format(entity.getBbWidth());
            String height = SIZE_FORMAT.format(entity.getBbHeight());
            rightSideInfo.add(new StringTextComponent(width + "x" + height + "x" + width + "m"));
            
            rightSideInfo.stream().map(line -> font.width(line)).max(Comparator.naturalOrder()).ifPresent(tooltipWidth -> {
                renderComponentTooltip(matrixStack, rightSideInfo, this.width + 4, 24);
            });
        });
    }
    
    // TODO arrow keys
//    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
//        if (pKeyCode == GLFW.GLFW_KEY_F4 && this.currentlyHovered.isPresent()) {
//            this.setFirstMousePos = false;
//            this.currentlyHovered = this.currentlyHovered.get().getNext();
//            return true;
//        } else {
//            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
//        }
//    }
    
    private void chooseAndClose() {
        this.currentlyHovered.ifPresent(widget -> {
            chosenTypeTmp = widget.entityType;
        });
        this.minecraft.setScreen((Screen)null);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private class SelectorWidget extends Widget {
        private final EntityType<?> entityType;
        private boolean isSelected;
        
        private SelectorWidget(EntityType<?> entityType, int x, int y) {
            super(x, y, 24, 24, entityType.getDescription());
            this.entityType = entityType;
        }
        
        @Override
        public void renderButton(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.enableBlend();
            matrixStack.pushPose();
            matrixStack.translate((double)this.x, (double)this.y, 0.0D);

            mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            blit(matrixStack, 0, 0, 0, 0, 24, 24, 64, 64);

            ResourceLocation iconTexture = EntityTypeIcon.getIcon(entityType);
            mc.getTextureManager().bind(iconTexture);
            blit(matrixStack, 4, 4, 0, 0, 16, 16, 16, 16);
            
            mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            if (this.isSelected) {
                blit(matrixStack, 0, 0, 24, 0, 24, 24, 64, 64);
            }
            else if (GeneralUtil.orElseFalse(prevSelected, slot -> slot == this)) {
                blit(matrixStack, 0, 0, 0, 24, 24, 24, 64, 64);
            }
            
            matrixStack.popPose();
            RenderSystem.disableBlend();
        }
        
        @Override
        public boolean isHovered() {
            return super.isHovered() || this.isSelected;
        }
        
        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            this.narrate();
        }
    }
}
