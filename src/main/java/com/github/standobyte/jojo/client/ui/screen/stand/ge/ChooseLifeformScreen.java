package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.action.stand.GoldExperienceChooseLifeform;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.client.InputHandler;
import com.github.standobyte.jojo.client.InputHandler.MouseButton;
import com.github.standobyte.jojo.client.ui.screen.GridList;
import com.github.standobyte.jojo.client.ui.screen.ScreenCloseMode;
import com.github.standobyte.jojo.client.ui.screen.WasdAllowingScreen;
import com.github.standobyte.jojo.util.general.GeneralUtil;
import com.github.standobyte.jojo.util.mc.EntityTypeToInstance;
import com.github.standobyte.jojo.util.mc.MobAggroCategory;
import com.github.standobyte.jojo.util.mod.JojoModUtil.Direction2D;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public class ChooseLifeformScreen extends WasdAllowingScreen {
    public static EntityType<?> chosenTypeTmp = null;
    public static Set<EntityType<?>> hiddenEntriesTmp = new HashSet<>();
    
    public static final ResourceLocation LIFEFORM_CHOOSE_LOCATION = new ResourceLocation(JojoMod.MOD_ID, "textures/gui/lifeform_choose.png");

    private List<EntityType<?>> allAllowedEntityTypes;
    private List<EntityType<?>> entityTypes;
    private GridList<SelectorWidget> entitySelectionGrid;
    
    private Optional<SelectorWidget> currentlyHovered = Optional.empty();
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    
    private Button filterListButton;
    private FilterList filterList;
    private Button unlockAllButton;
    
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
        LazyOptional<PlayerUtilCap> metEntityTypesCap = minecraft.player.getCapability(PlayerUtilCapProvider.CAPABILITY);
        allAllowedEntityTypes = ForgeRegistries.ENTITIES.getValues()
                .stream()
                .filter(type -> 
                    GeneralUtil.orElseFalse(metEntityTypesCap, cap -> cap.metEntityType(type))
                    && GoldExperienceChooseLifeform.isValidLifeform(type))
                .sorted(widgetSortComparator())
                .collect(Collectors.toList());
        entityTypes = allAllowedEntityTypes
                .stream()
                .filter(type -> !hiddenEntriesTmp.contains(type))
                .collect(Collectors.toList());
        
        initSelectionGrid();
        MobAggroCategory.requestCategoryOnClient(allAllowedEntityTypes);
        
        addButton(filterListButton = new Button(width - 100, height - 48, 96, 20, new TranslationTextComponent("jojo.ge_lifeform.filter_list"), 
                button -> filterList.visible = !filterList.visible));
        
        addButton(unlockAllButton = new Button(width - 100, height - 24, 96, 20, new TranslationTextComponent("jojo.ge_lifeform.unlock_all"), 
                button -> {
                    // TODO unlock all
                }));
        
        filterList = new FilterList(allAllowedEntityTypes, width - 8, height - 52, 100, height - 128, this);
    }
    
    private Comparator<EntityType<?>> widgetSortComparator() {
        return Comparator.comparing(type -> type.getDescription().getString(), String::compareTo);
    }
    
    private void initSelectionGrid() {
        entitySelectionGrid = GridList.create(entityTypes, Math.max((height - 24) / 30, 1), 
                (entityType, row, column) -> {
                    SelectorWidget widget = new SelectorWidget(entityType, 8 + column * 30, 8 + row * 30, row, column);
                    return widget;
                });
    }
    
    
    
    private int ticks = 0;
    private final int keyHeld;
    private ScreenCloseMode mode = ScreenCloseMode.CLICK;
    private boolean holdsButton = true;
    
    @Override
    public void tick() {
        if (holdsButton && ++ticks == 5) {
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
            
            entitySelectionGrid.forEach(widget -> {
                widget.render(matrixStack, mouseX, mouseY, partialTicks);
                currentlyHovered.ifPresent(w -> {
                    widget.setSelected(widget == w);
                });
                if (movedMouse && widget.isHovered()) {
                    currentlyHovered = Optional.of(widget);
                }
            });
            
            filterList.render(matrixStack, minecraft, mouseX, mouseY, partialTicks);
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonId) {
        if (filterList.mouseClicked(mouseX, mouseY, buttonId)) {
            return true;
        }
        
        if (currentlyHovered.isPresent()) {
            SelectorWidget hovered = currentlyHovered.get();
            MouseButton button = MouseButton.getButtonFromId(buttonId);
            switch (button) {
            case LEFT:
                chooseAndClose();
                return true;
            case RIGHT:
                hideEntry(hovered.entityType);
                return true;
            default:
                break;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, buttonId);
    }
    
    private static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
        map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
        map.put(GLFW.GLFW_KEY_UP,    Direction2D.UP);
        map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
        map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
    });
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (handleArrowKey(pKeyCode, pScanCode, pModifiers)) {
            setFirstMousePos = false;
            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
    
    private boolean handleArrowKey(int pKeyCode, int pScanCode, int pModifiers) {
        if (!ARROW_KEYS.containsKey(pKeyCode)) return false;
        
        boolean control = (pModifiers & GLFW.GLFW_MOD_CONTROL) > 0;
        Direction2D direction = ARROW_KEYS.get(pKeyCode);
        if (!currentlyHovered.isPresent()) {
            currentlyHovered = entitySelectionGrid.get(0, 0);
        }
        
        currentlyHovered = entitySelectionGrid.move(currentlyHovered, direction, control);
        
        return true;
    }
    
    
    private void chooseAndClose() {
        this.currentlyHovered.ifPresent(widget -> {
            chosenTypeTmp = widget.entityType;
        });
        this.minecraft.setScreen((Screen)null);
    }
    
    public void hideEntry(EntityType<?> entityType) {
        if (!hiddenEntriesTmp.contains(entityType)) {
            hiddenEntriesTmp.add(entityType);
            
            EntityType<?> setHovered = entityType;
            if (currentlyHovered.isPresent()) {
                SelectorWidget hovered = currentlyHovered.get();
                if (hovered.entityType == entityType) {
                    int index = entityTypes.indexOf(entityType) - 1;
                    if (index >= 0) {
                        setHovered = entityTypes.get(index);
                    }
                }
                else {
                    setHovered = hovered.entityType;
                }
            }
            
            entityTypes.remove(entityType);
            initSelectionGrid();
            currentlyHovered = Optional.ofNullable(setHovered).flatMap(
                    type -> entitySelectionGrid.findFirst(widget -> widget.entityType == type));
        }
    }
    
    public void showEntry(EntityType<?> entityType) {
        if (hiddenEntriesTmp.contains(entityType)) {
            hiddenEntriesTmp.remove(entityType);
            
            EntityType<?> setHovered = entityType;
            
            entityTypes.add(entityType);
            entityTypes = entityTypes.stream().sorted(widgetSortComparator()).collect(Collectors.toList());
            initSelectionGrid();
            currentlyHovered = Optional.ofNullable(setHovered).flatMap(
                    type -> entitySelectionGrid.findFirst(widget -> widget.entityType == type));
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (filterList.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private class SelectorWidget extends Widget implements GridList.IGridElement {
        private final EntityType<?> entityType;
        private boolean isSelected;
        public final int row;
        public final int column;
        
        private SelectorWidget(EntityType<?> entityType, int x, int y, int row, int column) {
            super(x, y, 24, 24, entityType.getDescription());
            this.entityType = entityType;
            this.row = row;
            this.column = column;
        }
        
        @Override
        public void renderButton(MatrixStack matrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
            Minecraft mc = Minecraft.getInstance();
            RenderSystem.enableBlend();
            matrixStack.pushPose();
            matrixStack.translate((double)this.x, (double)this.y, 0.0D);

            mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            blit(matrixStack, 0, 0, 0, 0, 24, 24, 128, 128);

            ResourceLocation iconTexture = EntityTypeIcon.getIcon(entityType);
            mc.getTextureManager().bind(iconTexture);
            blit(matrixStack, 4, 4, 0, 0, 16, 16, 16, 16);
            
            mc.getTextureManager().bind(LIFEFORM_CHOOSE_LOCATION);
            if (this.isSelected) {
                blit(matrixStack, 0, 0, 24, 0, 24, 24, 128, 128);
            }
            else if (this.entityType == chosenTypeTmp) {
                blit(matrixStack, 0, 0, 0, 24, 24, 24, 128, 128);
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

        @Override
        public int getRow() {
            return row;
        }

        @Override
        public int getColumn() {
            return column;
        }
    }
}
