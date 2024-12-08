package rocks.m2x.demo.service.report.customhtmlrender;

import org.ddr.poi.html.util.ListStyleType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STNumberFormat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NicerListStyleType implements ListStyleType {
    static String SYMBOL_DISC_SMALL = "\u009F"; // •⚫
    static String SYMBOL_SQUARE_SMALL = "\u00A7"; // •⚫

    @Override
    public String getName() {
        return "";
    }

    @Override
    public STNumberFormat.Enum getFormat() {
        return null;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public String getFont() {
        return "";
    }

    public enum NicerUnordered implements ListStyleType {
        DISC("disc", STNumberFormat.BULLET, SYMBOL_DISC_SMALL, FONT_WINGDINGS),
        HIVEN("hiven", STNumberFormat.BULLET, "-", null),
        DECIMAL("decimal", STNumberFormat.DECIMAL, "", null),
        SQUARE("square", STNumberFormat.BULLET, SYMBOL_SQUARE_SMALL, FONT_WINGDINGS),
        NONE("none", STNumberFormat.NONE, null, null);

        private static final Map<String, ListStyleType> TYPE_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(NicerUnordered::getName, Function.identity()));

        private final String name;
        private final STNumberFormat.Enum format;
        private final String text;
        private final String font;

        NicerUnordered(String name, STNumberFormat.Enum format, String text, String font) {
            this.name = name;
            this.format = format;
            this.text = text;
            this.font = font;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public STNumberFormat.Enum getFormat() {
            return format;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public String getFont() {
            return font;
        }

        public static ListStyleType of(String type) {
            return TYPE_MAP.getOrDefault(type, DISC);
        }
    }
}
