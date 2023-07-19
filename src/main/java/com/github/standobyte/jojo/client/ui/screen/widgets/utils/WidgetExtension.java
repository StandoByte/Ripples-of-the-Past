package com.github.standobyte.jojo.client.ui.screen.widgets.utils;

import net.minecraft.client.gui.widget.Widget;

public class WidgetExtension {
    private final Widget originWidget;
    
    private int yStarting;
    
    public WidgetExtension(Widget originWidget) {
        this.originWidget = originWidget;
        this.yStarting = originWidget.y;
    }
    
    
    public void setY(int y) {
        originWidget.y = y;
        this.yStarting = y;
    }
    
    public int getYStarting() {
        return yStarting;
    }
    
    public void updateY(int scrollY) {
        originWidget.y = this.yStarting + scrollY;
    }
}
