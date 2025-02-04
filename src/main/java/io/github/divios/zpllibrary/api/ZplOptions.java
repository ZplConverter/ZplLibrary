package io.github.divios.zpllibrary.api;

public final class ZplOptions {
    private final BitmapEncodingKind encodingKind;
    private final boolean graphicFieldOnly;
    private final boolean setLabelLength;
    private final int threshold;
    private final DitheringKind ditheringKind;
    private final int printQuantity;
    private final byte labelTop;
    private final short labelShift;
    private final long originalDpi;
    private final long targetDpi;

    // Constructor with all fields
    public ZplOptions(
            BitmapEncodingKind encodingKind,
            boolean graphicFieldOnly,
            boolean setLabelLength,
            int threshold,
            DitheringKind ditheringKind,
            int printQuantity,
            byte labelTop,
            short labelShift,
            long originalDpi,
            long targetDpi
    ) {
        this.encodingKind = encodingKind;
        this.graphicFieldOnly = graphicFieldOnly;
        this.setLabelLength = setLabelLength;
        this.threshold = threshold;
        this.ditheringKind = ditheringKind;
        this.printQuantity = printQuantity;
        this.labelTop = labelTop;
        this.labelShift = labelShift;
        this.originalDpi = originalDpi;
        this.targetDpi = targetDpi;
    }

    // Default constructor with default values
    public ZplOptions() {
        this(
                BitmapEncodingKind.HEXADECIMAL_COMPRESSED,
                false,
                false,
                128,
                DitheringKind.NONE,
                0,
                (byte) 0,
                (short) 0,
                300,
                300
        );
    }

    private ZplOptions(Builder builder) {
        this.encodingKind = builder.encodingKind;
        this.graphicFieldOnly = builder.graphicFieldOnly;
        this.setLabelLength = builder.setLabelLength;
        this.threshold = builder.threshold;
        this.ditheringKind = builder.ditheringKind;
        this.printQuantity = builder.printQuantity;
        this.labelTop = builder.labelTop;
        this.labelShift = builder.labelShift;
        this.originalDpi = builder.originalDpi;
        this.targetDpi = builder.targetDpi;
    }

    public static ZplOptions DEFAULT() {
        return new ZplOptions();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public BitmapEncodingKind getEncodingKind() {
        return encodingKind;
    }

    public boolean isGraphicFieldOnly() {
        return graphicFieldOnly;
    }

    public boolean isSetLabelLength() {
        return setLabelLength;
    }

    public int getThreshold() {
        return threshold;
    }

    public DitheringKind getDitheringKind() {
        return ditheringKind;
    }

    public int getPrintQuantity() {
        return printQuantity;
    }

    public byte getLabelTop() {
        return labelTop;
    }

    public short getLabelShift() {
        return labelShift;
    }

    public long getOriginalDpi() {
        return originalDpi;
    }

    public long getTargetDpi() {
        return targetDpi;
    }

    public Builder toBuilder() {
        return new Builder()
                .setEncodingKind(encodingKind)
                .setGraphicFieldOnly(graphicFieldOnly)
                .setLabelTop(labelTop)
                .setLabelShift(labelShift)
                .setTargetDpi(targetDpi)
                .setDitheringKind(ditheringKind)
                .setPrintQuantity(printQuantity)
                .setThreshold(threshold)
                .setSetLabelLength(setLabelLength);
    }

    // Optional: Override toString, equals, and hashCode if needed
    @Override
    public String toString() {
        return String.format(
                "ZplOptions[encodingKind=%s, graphicFieldOnly=%b, setLabelLength=%b, threshold=%d, ditheringKind=%s, printQuantity=%d, labelTop=%d, labelShift=%d]",
                encodingKind, graphicFieldOnly, setLabelLength, threshold, ditheringKind, printQuantity, labelTop, labelShift
        );
    }

    public enum BitmapEncodingKind {
        HEXADECIMAL,
        HEXADECIMAL_COMPRESSED,
        BASE64,
        BASE64_COMPRESSED
    }

    public enum DitheringKind {
        NONE,
        FLOYD_STEINBERG,
        ATKINSON,
        // Add other dithering algorithms as needed
    }

    // Builder class
    public static class Builder {
        // Default values
        private BitmapEncodingKind encodingKind = BitmapEncodingKind.HEXADECIMAL_COMPRESSED;
        private boolean graphicFieldOnly = false;
        private boolean setLabelLength = false;
        private int threshold = 128;
        private DitheringKind ditheringKind = DitheringKind.NONE;
        private int printQuantity = 0;
        private byte labelTop = 0;
        private short labelShift = 0;
        private long originalDpi = 300;
        private long targetDpi = 300;

        // Setters for all fields (return the builder for method chaining)
        public Builder setEncodingKind(BitmapEncodingKind encodingKind) {
            this.encodingKind = encodingKind;
            return this;
        }

        public Builder setGraphicFieldOnly(boolean graphicFieldOnly) {
            this.graphicFieldOnly = graphicFieldOnly;
            return this;
        }

        public Builder setSetLabelLength(boolean setLabelLength) {
            this.setLabelLength = setLabelLength;
            return this;
        }

        public Builder setThreshold(int threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder setDitheringKind(DitheringKind ditheringKind) {
            this.ditheringKind = ditheringKind;
            return this;
        }

        public Builder setPrintQuantity(int printQuantity) {
            this.printQuantity = printQuantity;
            return this;
        }

        public Builder setLabelTop(byte labelTop) {
            this.labelTop = labelTop;
            return this;
        }

        public Builder setLabelShift(short labelShift) {
            this.labelShift = labelShift;
            return this;
        }

        public Builder setOriginalDpi(long originalDpi) {
            this.originalDpi = originalDpi;
            return this;
        }

        public Builder setTargetDpi(long targetDpi) {
            this.targetDpi = targetDpi;
            return this;
        }

        // Build method to create the ZplOptions object
        public ZplOptions build() {
            return new ZplOptions(this);
        }
    }
}
