package io.github.divios.zpllibrary.api;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;

import static io.github.divios.zpllibrary.api.ConversionUtils.*;

public class ZPLConversion {

    private ZPLConversion() {
        // Utility class
    }

    public static String convertPdf(String pdfAsBase64String, ZplOptions zplOptions) {
        if (pdfAsBase64String == null) {
            throw new IllegalArgumentException("pdfAsBase64String cannot be null");
        }

        byte[] pdfBytes = Base64.getDecoder().decode(pdfAsBase64String);
        return convertPdf(pdfBytes, zplOptions);
    }

    public static String convertPdf(byte[] pdfAsByteArray, ZplOptions zplOptions) {
        if (pdfAsByteArray == null) {
            throw new IllegalArgumentException("pdfAsByteArray cannot be null");
        }

        try (InputStream pdfStream = new ByteArrayInputStream(pdfAsByteArray)) {
            return convertPdf(pdfStream, zplOptions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF stream", e);
        }
    }

    public static String convertPdf(InputStream pdfStream, ZplOptions zplOptions) {
        if (pdfStream == null) {
            throw new IllegalArgumentException("pdfStream cannot be null");
        }

        if (zplOptions == null) {
            zplOptions = ZplOptions.DEFAULT();
        }

        // Placeholder for PDF to images conversion
        BufferedImage image = convertPdfToImages(pdfStream, zplOptions.getTargetDpi());
        return convertBitmap(image, zplOptions);
    }

    public static BufferedImage convertPdfToImages(InputStream pdfStream, long dpi) {
        try (PDDocument document = PDDocument.load(pdfStream)) {
            // Create a PDFRenderer
            PDFRenderer renderer = new PDFRenderer(document);

            return renderer.renderImageWithDPI(0, dpi);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF stream", e);
        }
    }

    public static String convertBitmap(String bitmapPath, ZplOptions zplOptions) {
        try {
            BufferedImage bitmap = ImageIO.read(new File(bitmapPath));
            return convertBitmap(bitmap, zplOptions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bitmap file", e);
        }
    }

    public static String convertBitmap(InputStream bitmapAsStream, ZplOptions zplOptions) {
        if (bitmapAsStream == null) {
            throw new IllegalArgumentException("bitmapAsStream cannot be null");
        }

        try (bitmapAsStream) {
            BufferedImage bitmap = ImageIO.read(bitmapAsStream);
            return convertBitmap(bitmap, zplOptions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bitmap stream", e);
        }
    }


    public static String convertBitmap(byte[] bitmapAsByteArray, ZplOptions zplOptions) {
        try (InputStream inputStream = new ByteArrayInputStream(bitmapAsByteArray)) {
            BufferedImage bitmap = ImageIO.read(inputStream);
            return convertBitmap(bitmap, zplOptions);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read bitmap byte array", e);
        }
    }

    public static String convertBitmap(BufferedImage bitmap, ZplOptions zplOptions) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap cannot be null");
        }

        return convertBitmapImpl(bitmap, zplOptions);
    }

    private static String convertBitmapImpl(BufferedImage bitmap, ZplOptions zplOptions) {
        if (zplOptions == null) {
            zplOptions = new ZplOptions();
        }

        try {

            //bitmap = resizeImage(bitmap, zplOptions.getOriginalDpi(), zplOptions.getTargetDpi());

            int[] binaryByteCount = new int[1];
            int[] bytesPerRow = new int[1];

            // Placeholder for bitmap processing
            bitmap = toMonochrome(bitmap);
            String bitmapAsHex = convertBitmapToHex(bitmap, zplOptions.getThreshold(), binaryByteCount, bytesPerRow);
            String bitmapPayload;

            switch (zplOptions.getEncodingKind()) {
                case HEXADECIMAL:
                    bitmapPayload = bitmapAsHex;
                    break;
                case HEXADECIMAL_COMPRESSED:
                    bitmapPayload = compressHex(bitmapAsHex, bitmap.getWidth() / 8);
                    break;
                case BASE64:
                case BASE64_COMPRESSED:
                    bitmapPayload = bitmapAsHex.replace("\n", "");
                    String encodingId = "B64";
                    byte[] bitmapAsBytes = hexToByteArray(bitmapPayload);

                    if (zplOptions.getEncodingKind() == ZplOptions.BitmapEncodingKind.BASE64_COMPRESSED) {
                        encodingId = "Z64";
                        bitmapAsBytes = deflate(bitmapAsBytes);
                    }

                    String base64 = Base64.getEncoder().encodeToString(bitmapAsBytes);
                    int csc = computeBitmapChecksum(base64);

                    bitmapPayload = String.format(":%s:%s:%04X", encodingId, base64, csc);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown encoding kind: " + zplOptions.getEncodingKind());
            }

            String graphicField = String.format("^GFA,%d,%d,%d,%s",
                    binaryByteCount[0], binaryByteCount[0], bytesPerRow[0], bitmapPayload);

            return String.format("^XA%s^FS", graphicField);      // ^XA%s^FS^XZ

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert bitmap to ZPL", e);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, long originalDPI, long targetDPI) {
        int newWidth =  BigDecimal.valueOf(originalImage.getWidth())
                .divide(BigDecimal.valueOf(originalDPI), RoundingMode.FLOOR)
                .multiply(BigDecimal.valueOf(targetDPI))
                .intValue();

        int newHeight = BigDecimal.valueOf(originalImage.getHeight())
                .divide(BigDecimal.valueOf(originalDPI), RoundingMode.FLOOR)
                .multiply(BigDecimal.valueOf(targetDPI))
                .intValue();

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resizedImage;
    }

}
