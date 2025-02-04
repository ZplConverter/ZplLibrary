package io.github.divios.zpllibrary.api;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.*;
import java.util.zip.Deflater;

/**
 * Utility class for image conversion and ZPL compression.
 */
public class ConversionUtils {

    private ConversionUtils() {
        // Utility class
    }

    private static final Map<Integer, String> CompressionCountMapping = new HashMap<>();

    // Static lookup table for hexadecimal strings to byte values
    private static final Map<String, Byte> HEX_LOOKUP_TABLE = new HashMap<>();

    // Static initializer block to populate the lookup table
    static {
        for (int i = 0; i <= 255; i++) {
            String hex = String.format("%02X", i); // Format as 2-digit uppercase hexadecimal
            HEX_LOOKUP_TABLE.put(hex, (byte) i);
        }
    }

    /**
     * Converts a hexadecimal string into a byte array.
     *
     * @param input The hexadecimal string to convert.
     * @return A byte array containing the converted values.
     */
    public static byte[] hexToByteArray(String input) {
        if (input == null || input.length() % 2 != 0) {
            throw new IllegalArgumentException("Input string must be non-null and have an even length.");
        }

        List<Byte> result = new ArrayList<>();

        for (int i = 0; i < input.length(); i += 2) {
            String hexPair = input.substring(i, i + 2);
            Byte byteValue = HEX_LOOKUP_TABLE.get(hexPair);

            if (byteValue == null) {
                throw new IllegalArgumentException("Invalid hexadecimal string: " + hexPair);
            }

            result.add(byteValue);
        }

        // Convert List<Byte> to byte[]
        byte[] byteArray = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            byteArray[i] = result.get(i);
        }

        return byteArray;
    }

    // Convert to Monochrome
    public static BufferedImage toMonochrome(BufferedImage image) {
        BufferedImage monochrome = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = monochrome.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return monochrome;
    }

    public static String convertBitmapToHex(BufferedImage image, int threshold, int[] binaryByteCount, int[] bytesPerRow) {
        StringBuilder zplBuilder = new StringBuilder();

        int width = image.getWidth();
        int height = image.getHeight();

        // Calculate bytes per row (8 pixels per byte)
        bytesPerRow[0] = (width % 8 > 0) ? (width / 8 + 1) : (width / 8);
        binaryByteCount[0] = height * bytesPerRow[0];

        int colorBits = 0;
        int j = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Extract RGB components of the pixel
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                // Calculate grayscale value and determine if the pixel is black
                boolean blackPixel = ((red + green + blue) / 3) < threshold;

                // Set the corresponding bit if the pixel is black
                if (blackPixel) {
                    colorBits |= 1 << (7 - j);
                }

                j++;

                // Add the byte to the output when 8 bits are processed or at the end of a row
                if (j == 8 || x == (width - 1)) {
                    zplBuilder.append(String.format("%02X", colorBits)); // Append as hexadecimal
                    colorBits = 0;
                    j = 0;
                }
            }
            zplBuilder.append('\n'); // Newline for each row (optional, for readability)
        }

        return zplBuilder.toString();
    }

    public static String compressHex(String code, int widthBytes) {
        int maxlinea = widthBytes * 2;
        StringBuilder sbCode = new StringBuilder();
        StringBuilder sbLinea = new StringBuilder();
        String previousLine = null;
        int counter = 0;
        char aux = code.charAt(0);
        boolean firstChar = false;

        for (char item : code.toCharArray()) {
            if (firstChar) {
                aux = item;
                firstChar = false;
                continue;
            }
            if (item == '\n') {
                if (counter >= maxlinea && aux == '0') {
                    sbLinea.append(',');
                } else if (counter >= maxlinea && aux == 'F') {
                    sbLinea.append('!');
                } else if (counter > 20) {
                    int multi20 = (counter / 20) * 20;
                    sbLinea.append(CompressionCountMapping.getOrDefault(Math.min(multi20, 400), ""));

                    int restover400 = multi20 / 400;
                    if (restover400 > 0) {
                        for (; restover400 > 1; restover400--) {
                            sbLinea.append(CompressionCountMapping.getOrDefault(400, ""));
                        }

                        int restto400 = (counter % 400) / 20 * 20;

                        if (restto400 > 0) {
                            sbLinea.append(CompressionCountMapping.getOrDefault(restto400, ""));
                        }
                    }

                    int resto20 = (counter % 20);

                    if (resto20 != 0) {
                        sbLinea.append(CompressionCountMapping.getOrDefault(resto20, "")).append(aux);
                    } else {
                        sbLinea.append(aux);
                    }
                } else {
                    sbLinea.append(CompressionCountMapping.getOrDefault(counter, "")).append(aux);
                }
                counter = 1;
                firstChar = true;

                if (Objects.equals(sbLinea.toString(), previousLine)) {
                    sbCode.append(':');
                } else {
                    sbCode.append(sbLinea);
                }

                previousLine = sbLinea.toString();
                sbLinea.setLength(0);
                continue;
            }

            if (aux == item) {
                counter++;
            } else {
                if (counter > 20) {
                    int multi20 = (counter / 20) * 20;
                    sbLinea.append(CompressionCountMapping.getOrDefault(Math.min(multi20, 400), ""));

                    int restover400 = multi20 / 400;
                    if (restover400 > 0) {
                        for (; restover400 > 1; restover400--) {
                            sbLinea.append(CompressionCountMapping.getOrDefault(400, ""));
                        }

                        int restto400 = (counter % 400) / 20 * 20;

                        if (restto400 > 0) {
                            sbLinea.append(CompressionCountMapping.getOrDefault(restto400, ""));
                        }
                    }

                    int resto20 = (counter % 20);

                    if (resto20 != 0) {
                        sbLinea.append(CompressionCountMapping.getOrDefault(resto20, "")).append(aux);
                    } else {
                        sbLinea.append(aux);
                    }
                } else {
                    sbLinea.append(CompressionCountMapping.getOrDefault(counter, "")).append(aux);
                }
                counter = 1;
                aux = item;
            }
        }

        return sbCode.toString();
    }

    /**
     * Computes the CRC-16 checksum for the given input string using the CCITT polynomial.
     *
     * @param input The input string to compute the checksum for.
     * @return The CRC-16 checksum as a 16-bit unsigned integer (represented as an int).
     */
    public static int computeBitmapChecksum(String input) {
        byte[] array = input.getBytes(java.nio.charset.StandardCharsets.US_ASCII);

        // CRC-CCIIT 0xFFFF
        // Polynomial: 1 + x + x^5 + x^12 + x^16 (0x1021)
        final int polynomial = 0x1021;
        int crc = 0x0000;

        for (byte b : array) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i)) & 1) == 1;
                boolean c15 = ((crc >> 15) & 1) == 1;
                crc <<= 1;

                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF; // Ensure the result is 16 bits
        return crc;
    }

    /**
     * Compresses the input byte array using the DEFLATE algorithm.
     *
     * @param input The input byte array to compress.
     * @return The compressed byte array.
     */
    public static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        deflater.end();

        return outputStream.toByteArray();
    }

    static {
        // Initialize CompressionCountMapping with the provided values
        CompressionCountMapping.put(1, "G");
        CompressionCountMapping.put(2, "H");
        CompressionCountMapping.put(3, "I");
        CompressionCountMapping.put(4, "J");
        CompressionCountMapping.put(5, "K");
        CompressionCountMapping.put(6, "L");
        CompressionCountMapping.put(7, "M");
        CompressionCountMapping.put(8, "N");
        CompressionCountMapping.put(9, "O");
        CompressionCountMapping.put(10, "P");
        CompressionCountMapping.put(11, "Q");
        CompressionCountMapping.put(12, "R");
        CompressionCountMapping.put(13, "S");
        CompressionCountMapping.put(14, "T");
        CompressionCountMapping.put(15, "U");
        CompressionCountMapping.put(16, "V");
        CompressionCountMapping.put(17, "W");
        CompressionCountMapping.put(18, "X");
        CompressionCountMapping.put(19, "Y");
        CompressionCountMapping.put(20, "g");
        CompressionCountMapping.put(40, "h");
        CompressionCountMapping.put(60, "i");
        CompressionCountMapping.put(80, "j");
        CompressionCountMapping.put(100, "k");
        CompressionCountMapping.put(120, "l");
        CompressionCountMapping.put(140, "m");
        CompressionCountMapping.put(160, "n");
        CompressionCountMapping.put(180, "o");
        CompressionCountMapping.put(200, "p");
        CompressionCountMapping.put(220, "q");
        CompressionCountMapping.put(240, "r");
        CompressionCountMapping.put(260, "s");
        CompressionCountMapping.put(280, "t");
        CompressionCountMapping.put(300, "u");
        CompressionCountMapping.put(320, "v");
        CompressionCountMapping.put(340, "w");
        CompressionCountMapping.put(360, "x");
        CompressionCountMapping.put(380, "y");
        CompressionCountMapping.put(400, "z");
    }

}
