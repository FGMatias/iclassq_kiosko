package org.iclassq.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.logging.Logger;

public class Fonts {
    private static String fontFamily = "System";
    private static final Logger logger = Logger.getLogger(Fonts.class.getName());

    public static void loadFonts() {
        try {
            Font interRegular = Font.loadFont(
                    Fonts.class.getResourceAsStream("/fonts/Inter_18pt-Regular.ttf"),
                    14
            );
            Font.loadFont(
                    Fonts.class.getResourceAsStream("/fonts/Inter_18pt-Medium.ttf"),
                    14
            );
            Font.loadFont(
                    Fonts.class.getResourceAsStream("/fonts/Inter_18pt-SemiBold.ttf"),
                    14
            );
            Font.loadFont(
                    Fonts.class.getResourceAsStream("/fonts/Inter_18pt-Bold.ttf"),
                    14
            );

            if (interRegular != null) {
                fontFamily = interRegular.getFamily();
                logger.info("Fuente cargada correctamente: " + fontFamily);
            } else {
                logger.info("Error: No se pudo cargar la fuente, usando fuente del sistema");
            }
        } catch (Exception e) {
            logger.severe("Error al cargar fuentes: " + e.getMessage());
        }
    }

    public static String getFontFamily() {
        return fontFamily;
    }

    public static Font regular(double size) {
        return Font.font(fontFamily, FontWeight.NORMAL, size);
    }

    public static Font medium(double size) {
        return Font.font(fontFamily, FontWeight.MEDIUM, size);
    }

    public static Font semiBold(double size) {
        return Font.font(fontFamily, FontWeight.SEMI_BOLD, size);
    }

    public static Font bold(double size) {
        return Font.font(fontFamily, FontWeight.BOLD, size);
    }

    public static Font regular() {
        return regular(14);
    }

    public static Font medium() {
        return medium(14);
    }

    public static Font semiBold() {
        return semiBold(14);
    }

    public static Font bold() {
        return bold(14);
    }

    public static Font title1() {
        return bold(32);
    }

    public static Font title2() {
        return bold(24);
    }

    public static Font title3() {
        return bold(20);
    }

    public static Font title4() {
        return bold(16);
    }

    public static Font body() {
        return regular(14);
    }

    public static Font bodyMedium() {
        return medium(14);
    }

    public static Font small() {
        return regular(12);
    }

    public static Font caption() {
        return regular(11);
    }

    public static String toCss(double size, String weight) {
        return String.format(
                "-fx-font-family: '%s'; -fx-font-size: %.0fpx; -fx-font-weight: %s;",
                fontFamily, size, weight
        );
    }

    public static String toCss(double size) {
        return toCss(size, "normal");
    }

    public static String cssRegular(double size) {
        return toCss(size, "normal");
    }

    public static String cssMedium(double size) {
        return toCss(size, "500");
    }

    public static String cssSemiBold(double size) {
        return toCss(size, "600");
    }

    public static String cssBold(double size) {
        return toCss(size, "bold");
    }
}
