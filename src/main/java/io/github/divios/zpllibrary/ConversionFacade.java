package io.github.divios.zpllibrary;

import io.github.divios.zpllibrary.api.ZPLConversion;
import io.github.divios.zpllibrary.api.ZplOptions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static io.github.divios.zpllibrary.api.Utils.checkState;
import static io.github.divios.zpllibrary.api.Utils.isNotBlank;

/**
 * Collection of utility methods to convert any type of format (PDF, PNG, JPG, etc.) to ZPL.
 */
public class ConversionFacade {

    private ConversionFacade() {
        // Utility class
    }

    public static String convertFileToZPL(String path, ZplOptions zplOptions) throws IOException {
        return convertFileToZPL(Path.of(path), zplOptions);
    }

    public static String convertFileToZPL(File file, ZplOptions zplOptions) throws IOException {
        return convertFileToZPL(file.toPath(), zplOptions);
    }

    public static String convertFileToZPL(Path path, ZplOptions zplOptions) throws IOException {
        checkState(path.toFile().exists(), "File does not exist: %s", path);
        var extension = extractExtension(path);

        switch (extension) {
            case "pdf":
                var pdfInputStream = Files.newInputStream(path);
                return ZPLConversion.convertPdf(pdfInputStream, zplOptions);
            case "png":
            case "jpg":
            case "jpeg":
                var imageInputStream = Files.newInputStream(path);
                return ZPLConversion.convertBitmap(imageInputStream, zplOptions);
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }

    private static String extractExtension(Path path) {
        var name = path.getFileName().toString();
        var index = name.lastIndexOf('.');
        return index == -1 ? "" : name.substring(index + 1);
    }

    public static String convertBase64ToZpl(String base64, ZplOptions zplOptions) {
        checkState(isNotBlank(base64), "Base64 string cannot be null or empty");

        // Decode the Base64 string into a byte array
        byte[] data = Base64.getDecoder().decode(base64);
        var type = getTypeOfImage(data);

        switch (type) {
            case "pdf":
                return ZPLConversion.convertPdf(data, zplOptions);
            case "png":
            case "jpg":
            case "jpeg":
                return ZPLConversion.convertBitmap(data, zplOptions);
            default:
                throw new IllegalArgumentException("Unsupported image type: " + type);
        }
    }

    private static String getTypeOfImage(byte[] data) {
        if (startsWith(data, new byte[]{0x25, 0x50, 0x44, 0x46})) { // %PDF
            return "pdf";
        } else if (startsWith(data, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) { // FF D8 FF
            return "jpg";
        } else if (startsWith(data, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) { // ‰PNG....
            return "png";
        } else if (startsWith(data, new byte[]{0x47, 0x49, 0x46, 0x38})) { // GIF8
            return "gif";
        } else if (startsWith(data, new byte[]{0x42, 0x4D})) { // BM
            return "bmp";
        } else {
            return "unknown";
        }
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data == null || prefix == null || data.length < prefix.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }

        return true;
    }

}
