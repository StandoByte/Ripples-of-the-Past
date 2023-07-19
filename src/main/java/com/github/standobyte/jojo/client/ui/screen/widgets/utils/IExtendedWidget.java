package com.github.standobyte.jojo.client.ui.screen.widgets.utils;

import net.minecraft.client.gui.widget.Widget;

public interface IExtendedWidget {
    WidgetExtension getWidgetExtension();
    Widget thisAsWidget();
}
