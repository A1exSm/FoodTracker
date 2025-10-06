package org.alexander.gui;

import org.alexander.gui.tab.WeekManager;

import javax.swing.*;
import java.awt.*;

public class GUIHandler {
    private final AppFrame appFrame;
    private ToolBar toolBar;
    private JTabbedPane tabbedPane;

    public GUIHandler(AppFrame appFrame) {
        this.appFrame = appFrame;
        setup();
    }

    private void setup() {
        appFrame.setLayout(new BorderLayout());
        // JTabbedPane
        tabbedPane = new JTabbedPane();
        appFrame.add(tabbedPane, BorderLayout.CENTER);
        // Toolbar
        toolBar = new ToolBar(new WeekManager(tabbedPane));
        appFrame.add(toolBar, BorderLayout.NORTH);
    }
}
