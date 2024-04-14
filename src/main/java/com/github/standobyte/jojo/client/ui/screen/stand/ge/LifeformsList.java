package com.github.standobyte.jojo.client.ui.screen.stand.ge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen.widgets.TextButton;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class LifeformsList<V> extends ExtendedList<LifeformsList.LifeformsListEntry> {
    private static final Set<String> COLLAPSED_MOD_NAMES = new HashSet<>();
    
    protected ChooseLifeformListScreen screen;
    
    private List<V> allValues = new ArrayList<>();
    private Map<String, List<LifeformEntry>> allVisibleEntries = new HashMap<>();
    
    public LifeformsList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight, ChooseLifeformListScreen screen) {
        super(mc, width, height, y0, y1, itemHeight);
        this.screen = screen;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }
    
    public void setAllLegalValues(Collection<V> allValues) {
        this.allValues.clear();
        this.allValues.addAll(allValues);
    }
    
    public void update(Collection<V> lifeformValues) {
        update(lifeformValues.stream());
    }
    
    public void update(Stream<V> lifeformValues) {
        clearEntries();
        allVisibleEntries.clear();
//        maxWidth = -1;
        
        Map<String, List<V>> map = lifeformValues
                .collect(Collectors.groupingBy(this::getModName));
        map.keySet().stream().sorted(ChooseLifeformScreen.MOD_NAMES_ORDER).forEach(modName -> {
            ModCategoryEntry category = new ModCategoryEntry(modName);
            category.isExpanded = !COLLAPSED_MOD_NAMES.contains(modName);
            category.addExpandButton(new ModCategoryEntry.ExpandCollapseButton(
                    -1, -1, 10, 10, screen, button -> {
                        if (category.isExpanded) {
                            for (LifeformEntry entryToHide : allVisibleEntries.get(modName)) {
                                removeEntry(entryToHide);
                            }
                            setScrollAmount(getScrollAmount());
                            COLLAPSED_MOD_NAMES.add(modName);
                            category.isExpanded = false;
                        }
                        else {
                            int index = children().indexOf(category);
                            if (index > -1) {
                                children().addAll(index + 1, allVisibleEntries.get(modName));
                            }
                            COLLAPSED_MOD_NAMES.remove(modName);
                            category.isExpanded = true;
                        }
                    }, () -> category.isExpanded));
            addEntry(category);
//            maxWidth = Math.max(maxWidth, minecraft.font.width(new StringTextComponent(modName)));
            
            List<LifeformEntry> entriesWithHidden = new ArrayList<>();
            allVisibleEntries.put(modName, entriesWithHidden);
            
            List<V> values = map.get(modName);
            values.stream().sorted(Comparator.comparing(
                    ((Function<V, ITextComponent>) (this::getValueName))
                    .andThen(ITextComponent::getString), String::compareTo)).forEach(entryVal -> {
                        
                ITextComponent name = getValueName(entryVal);
                
                LifeformEntry entry = makeLifeformEntry(entryVal, name);
                if (isNew(entryVal)) {
                    entry.unseenEntry = true;
                }
                
                TextButton textButton = new TextButton(-1, -1, name, 
                        button -> {
                            select(entryVal);
                            screen.onClose();
                        }, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            renderHoveredTooltip(matrixStack, entryVal, mouseX, mouseY);
                        },
                        minecraft.font) {
                    
                    @Override
                    public ITextComponent makeText() {
                        ITextComponent text = super.makeText();
                        if (entry.unseenEntry) {
                            text = new TranslationTextComponent("gold_experience.lifeform_unseen", text).withStyle(TextFormatting.AQUA);
                        }
                        return text;
                    }
                };
                textButton.setHeight(itemHeight);
                LifeformEntry.FavoriteButton favoritesButton = new LifeformEntry.FavoriteButton(-1, -1, 9, 9, 
                        b -> {
                            LifeformEntry.FavoriteButton button = (LifeformEntry.FavoriteButton) b;
                            if (button.isFavorited) {
                                removeFavorite(entryVal);
                                button.isFavorited = false;
                            }
                            else {
                                addFavorite(entryVal);
                                button.isFavorited = true;
                            }
                        }, 
                        screen);
                favoritesButton.isFavorited = isInFavorites(entryVal);
                entry.addButtons(textButton, favoritesButton);

//                ITextComponent widthCheck = name;
//                if (entry.unseenEntry) {
//                    widthCheck = new TranslationTextComponent("gold_experience.lifeform_unseen", name);
//                }
//                maxWidth = Math.max(maxWidth, minecraft.font.width(widthCheck));
                
                entriesWithHidden.add(entry);
                if (category.isExpanded) {
                    addEntry(entry);
                }
            });
        });
        
//        int leftPos = this.x0;
//        updateSize(maxWidth + 54, this.height, y0, y1);
//        setLeftPos(leftPos);
    }

    protected abstract String getModName(V lifeformType);
    protected abstract ITextComponent getValueName(V lifeformType);
    protected abstract LifeformEntry makeLifeformEntry(V lifeformType, ITextComponent name);
    protected abstract void select(V lifeformType);
    protected abstract void addFavorite(V lifeformType);
    protected abstract void removeFavorite(V lifeformType);
    protected abstract boolean isInFavorites(V lifeformType);
    protected abstract boolean isNew(V lifeformType);
    protected abstract void renderHoveredTooltip(MatrixStack matrixStack, V lifeformType, int mouseX, int mouseY);
    
    private Predicate<V> filter = null;
    public void setFilter(@Nullable Predicate<V> filter) {
        this.filter = filter;
        if (filter == null) {
            update(allValues);
        }
        else {
            update(allValues.stream().filter(filter));
        }
        setScrollAmount(getScrollAmount());
    }
    
    @Override
    public int getRowWidth() {
        return width;
    }
    
    @Override
    protected int getScrollbarPosition() {
        return x1 - 6;
    }
    
    @Override
    public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
        // Ctrl + C, Ctrl + V
        this.renderBackground(pMatrixStack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();
//        if (this.renderBackground) {
//            this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
//            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            float f = 32.0F;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
//            tessellator.end();
//        }

        int j1 = this.getRowLeft();
        int k = this.y0 + 4 - (int)this.getScrollAmount();
//        if (this.renderHeader) {
//            this.renderHeader(pMatrixStack, j1, k, tessellator);
//        }

//        if (this.renderTopAndBottom) {
//            this.minecraft.getTextureManager().bind(AbstractGui.BACKGROUND_LOCATION);
//            RenderSystem.enableDepthTest();
//            RenderSystem.depthFunc(519);
//            float f1 = 32.0F;
//            int l = -100;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
//            tessellator.end();
//            RenderSystem.depthFunc(515);
//            RenderSystem.disableDepthTest();
//            RenderSystem.enableBlend();
//            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
//            RenderSystem.disableAlphaTest();
//            RenderSystem.shadeModel(7425);
//            RenderSystem.disableTexture();
//            int i1 = 4;
//            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
//            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
//            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
//            tessellator.end();
//        }

        int k1 = this.getMaxScroll();
        if (k1 > 0) {
            int scrollBarAlpha = 127;
            RenderSystem.disableTexture();
            int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            l1 = MathHelper.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;
            if (i2 < this.y0) {
                i2 = this.y0;
            }
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).uv(1.0F, 0.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).uv(0.0F, 0.0F).color(0, 0, 0, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)j, (double)i2, 0.0D).uv(1.0F, 0.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(128, 128, 128, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).uv(1.0F, 0.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).uv(0.0F, 0.0F).color(192, 192, 192, scrollBarAlpha).endVertex();
            tessellator.end();

            RenderSystem.disableBlend();
        }

        // moved it lower to render tooltips on top of the scroll bar
        this.renderList(pMatrixStack, j1, k, pMouseX, pMouseY, pPartialTicks);

        this.renderDecorations(pMatrixStack, pMouseX, pMouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }
    
    protected abstract static class LifeformsListEntry extends AbstractOptionList.Entry<LifeformsListEntry> {}
    
    public static class ModCategoryEntry extends LifeformsListEntry {
        private final String modName;
        private Widget expandButton;
        private boolean isExpanded = true;
        private List<Widget> buttons = Collections.emptyList();
        
        public ModCategoryEntry(String modName) {
            this.modName = modName;
        }
        
        private void addExpandButton(Widget expandButton) {
            this.expandButton = expandButton;
            this.buttons = ImmutableList.of(expandButton);
        }
        
        @Override
        public List<? extends IGuiEventListener> children() {
            return buttons;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            FontRenderer font = Minecraft.getInstance().font;
            ITextComponent name = new StringTextComponent(modName).withStyle(TextFormatting.BLUE, TextFormatting.ITALIC);
            font.drawShadow(pMatrixStack, name, pLeft + 43, pTop + 1, 0xFFFFFF);
            
            if (expandButton != null) {
                expandButton.x = pLeft + 29;
                expandButton.y = pTop;
                expandButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
        }
        
        
        private static class ExpandCollapseButton extends Button {
            private Supplier<Boolean> isExpanded;
            
            public ExpandCollapseButton(int pX, int pY, int pWidth, int pHeight, Screen screen, 
                    IPressable pOnPress, Supplier<Boolean> isExpanded) {
                super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY, pOnPress, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            ITextComponent text = isExpanded.get() ? new TranslationTextComponent("jojo.ui.list_collapse") : new TranslationTextComponent("jojo.ui.list_expand");
                            screen.renderTooltip(matrixStack, text, mouseX, mouseY);
                        });
                this.isExpanded = isExpanded;
            }
            
            @Override
            public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.textureManager.bind(ChooseLifeformListScreen.LIFEFORM_CHOOSE_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
                
                int texX = isHovered ? 118 : 108;
                int texY = isExpanded.get() ? 20 : 30;
                
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                blit(matrixStack, x, y, texX, texY, width, height, 128, 128);
                
                if (isHovered()) {
                    renderToolTip(matrixStack, mouseX, mouseY);
                }
            }
        }
    }
    
    public static class LifeformEntry extends LifeformsListEntry {
        protected final ITextComponent valueName;
        private Widget lifeformButton;
        private Widget favoriteButton;
        private List<Widget> buttons = Collections.emptyList();
        private boolean unseenEntry = false;
        
        public LifeformEntry(ITextComponent valueName) {
            this.valueName = valueName;
        }
        
        void addButtons(Widget lifeformButton, Widget favoriteButton) {
            this.lifeformButton = lifeformButton;
            this.favoriteButton = favoriteButton;
            this.buttons = ImmutableList.of(lifeformButton, favoriteButton);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return buttons;
        }

        @Override
        public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
                int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            if (lifeformButton != null) {
                lifeformButton.x = pLeft + 25;
                lifeformButton.y = pTop + 1;
                lifeformButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
            if (favoriteButton != null) {
                favoriteButton.x = pLeft + 2;
                favoriteButton.y = pTop + 1;
                favoriteButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
            }
        }
        
        
        private static class FavoriteButton extends Button {
            private boolean isFavorited;

            public FavoriteButton(int pX, int pY, int pWidth, int pHeight, 
                    IPressable pOnPress, Screen screen) {
                super(pX, pY, pWidth, pHeight, StringTextComponent.EMPTY, pOnPress, 
                        (button, matrixStack, mouseX, mouseY) -> {
                            ITextComponent text = ((FavoriteButton) button).isFavorited ? new TranslationTextComponent("jojo.ui.favorite_remove") : new TranslationTextComponent("jojo.ui.favorite");
                            screen.renderTooltip(matrixStack, text, mouseX, mouseY);
                        });
            }

            @Override
            public void renderButton(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
                if (!(isFavorited || isHovered())) return;
                
                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bind(ChooseLifeformListScreen.LIFEFORM_CHOOSE_LOCATION);
                int texY = isFavorited ? 9 : 0;
                RenderSystem.enableDepthTest();
                blit(pMatrixStack, x, y, 119, texY, width, height, 128, 128);
                
                if (isHovered()) {
                    renderToolTip(pMatrixStack, pMouseX, pMouseY);
                }
            }
        }
        
    }
}
