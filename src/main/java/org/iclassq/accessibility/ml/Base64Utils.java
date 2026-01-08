package org.iclassq.accessibility.ml;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class Base64Utils {
    private static final Logger logger = Logger.getLogger(Base64Utils.class.getName());

    public static String toBase64(BufferedImage image, String format) {
        if (image == null) {
            logger.warning("Imagen null, no se puede convertir");
            return null;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            byte[] imageBytes = baos.toByteArray();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            logger.fine(String.format("Imagen convertida a Base64: %d bytes -> %d chars",
                    imageBytes.length, base64.length()));

            return base64;
        } catch (Exception e) {
            logger.severe("Error convirtiendo imagen a base64: " + e.getMessage());
            return null;
        }
    }

    public static String toBase64(BufferedImage image) {
        return toBase64(image, "jpg");
    }

    public static List<String> toBase64List(List<BufferedImage> images, String format) {
        List<String> base64List = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            logger.warning("Lista de imagenes vacia");
            return base64List;
        }

        for (int i = 0; i < images.size(); i++) {
            BufferedImage image = images.get(i);
            String base64 = toBase64(image, format);

            if (base64 != null) {
                base64List.add(base64);
            } else {
                logger.warning(String.format("Imagen %d no se pudo convertir", i));
            }
        }

        logger.info(String.format("%d/%d imágenes convertidas a Base64",
                base64List.size(), images.size()));

        return base64List;
    }

    public static List<String> toBase64List(List<BufferedImage> images) {
        return toBase64List(images, "jpg");
    }

    public static int getBase64Size(String base64String) {
        if (base64String == null) return 0;
        return base64String.length();
    }

    public static int getTotalBase64Size(List<String> base64List) {
        if (base64List == null || base64List.isEmpty()) return 0;

        return base64List.stream()
                .mapToInt(Base64Utils::getBase64Size)
                .sum();
    }

    public static String formatBytes(int bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
