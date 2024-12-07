package rocks.m2x.demo.service.customhtmlrender;

import lombok.RequiredArgsConstructor;
import org.ddr.poi.html.ElementRenderer;
import org.ddr.poi.html.HtmlConstants;
import org.ddr.poi.html.HtmlRenderContext;
import org.ddr.poi.html.tag.ListRenderer;
import org.ddr.poi.html.util.CSSLength;
import org.ddr.poi.html.util.ListStyle;
import org.ddr.poi.html.util.ListStyleType;
import org.ddr.poi.html.util.RenderUtils;
import org.jsoup.nodes.Element;

/**
 * Copy of {@link ListRenderer} with some modifications, to use a nicer unordered list style.
 *
 * @author Michael Mrotzek
 */

@RequiredArgsConstructor
public class NicerListRenderer implements ElementRenderer {
    private static final String[] TAGS = {HtmlConstants.TAG_UL, HtmlConstants.TAG_OL};

    final NicerListStyleType.NicerUnordered listStyleType;
    int left = 150;

   public  NicerListRenderer setLeft(int left) {
        this.left = left;
        return this;
    }

    @Override
    public boolean renderStart(Element element, HtmlRenderContext context) {
        boolean hanging = true;

        CSSLength marginRight = CSSLength.of(context.currentElementStyle().getMarginRight().toLowerCase());
        int right = marginRight.isValid() && !marginRight.isPercent()
                ? RenderUtils.emuToTwips(context.lengthToEMU(marginRight)) : 0;
        ListStyle listStyle = new ListStyle(determineNumberFormat(context, element), hanging, left, right);
        context.getNumberingContext().startLevel(listStyle);
        return true;
    }

    /*
     * Modified: standard numbering list style and configurable unordered list style
     */
    private ListStyleType determineNumberFormat(HtmlRenderContext context, Element element) {
        return switch (element.tag().normalName()) {
            case HtmlConstants.TAG_OL -> {
                String type = element.attr(HtmlConstants.ATTR_TYPE);
                yield ListStyleType.Ordered.of(type);
            }
            case HtmlConstants.TAG_UL -> listStyleType;
            default -> ListStyleType.Unordered.NONE;
        };
    }

    @Override
    public void renderEnd(Element element, HtmlRenderContext context) {
        context.getNumberingContext().endLevel();
    }

    @Override
    public String[] supportedTags() {
        return TAGS;
    }

    @Override
    public boolean renderAsBlock() {
        return false;
    }
}
