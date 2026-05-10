package com.hrmanager.util;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    public enum Theme {
        LIGHT, DARK
    }

    private static final Color BRAND = new Color(79, 70, 229);
    private static final Color SUCCESS = new Color(16, 185, 129);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color INFO = new Color(59, 130, 246);

    private static final Color LIGHT_BG = new Color(248, 250, 255);
    private static final Color LIGHT_SURFACE = new Color(255, 255, 255);
    private static final Color LIGHT_TEXT = new Color(15, 23, 42);
    private static final Color LIGHT_SECONDARY = new Color(100, 116, 139);
    private static final Color LIGHT_BORDER = new Color(226, 232, 240);

    private static final Color DARK_BG = new Color(2, 6, 23);
    private static final Color DARK_SURFACE = new Color(15, 23, 42);
    private static final Color DARK_TEXT = new Color(248, 250, 252);
    private static final Color DARK_SECONDARY = new Color(148, 163, 184);
    private static final Color DARK_BORDER = new Color(51, 65, 85);

    public static Color getBackgroundColor(Theme theme) {
        return theme == Theme.DARK ? DARK_BG : LIGHT_BG;
    }

    public static Color getCardBackgroundColor(Theme theme) {
        return theme == Theme.DARK ? DARK_SURFACE : LIGHT_SURFACE;
    }

    public static Color getTextColor(Theme theme) {
        return theme == Theme.DARK ? DARK_TEXT : LIGHT_TEXT;
    }

    public static Color getSecondaryTextColor(Theme theme) {
        return theme == Theme.DARK ? DARK_SECONDARY : LIGHT_SECONDARY;
    }

    public static Color getBorderColor(Theme theme) {
        return theme == Theme.DARK ? DARK_BORDER : LIGHT_BORDER;
    }

    public static Color getAccentColor(Theme theme) {
        return BRAND;
    }

    public static Color getSuccessColor() {
        return SUCCESS;
    }

    public static Color getDangerColor() {
        return DANGER;
    }

    public static Color getWarningColor() {
        return WARNING;
    }

    public static Color getInfoColor() {
        return INFO;
    }
}