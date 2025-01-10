package report;

import aggressor.Prefs;
import common.CommonUtils;
import common.MudgeSanity;
import encoders.Base64;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.imageio.ImageIO;

public class ReportUtils {
    public static String accent() {
        return Prefs.getPreferences().getString("reporting.accent.color", "#003562");
    }

    public static String a(String string1, String string2) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<fo:basic-link color=\"" + accent() + "\" text-decoration=\"underline\" external-destination=\"" + string2 + "\">");
        stringBuffer.append("<fo:inline>");
        stringBuffer.append(string1);
        stringBuffer.append("</fo:inline>");
        stringBuffer.append("</fo:basic-link>");
        return stringBuffer.toString();
    }

    public static String b(String string) {
        return "<fo:inline font-weight=\"bold\">" + string + "</fo:inline>";
    }

    public static String br() {
        return "<fo:block> </fo:block>";
    }

    public static String u(String string) {
        return "<fo:inline text-decoration=\"underline\">" + string + "</fo:inline>";
    }

    public static String i(String string) {
        return "<fo:inline font-style=\"italic\">" + string + "</fo:inline>";
    }

    public static String code(String string) {
        return "<fo:inline font-weight=\"monospace\">" + string + "</fo:inline>";
    }

    public static String logo() {
        String str = Prefs.getPreferences().getString("reporting.header_image.file", "");
        if ("".equals(str)) {
            byte[] arrby = CommonUtils.readResource("resources/fso/logo2.png");
            return "url(&#34;data:image/png;base64," + Base64.encode(arrby) + "&#xA;&#34;)";
        }
        return str;
    }

    public static String image(RenderedImage renderedImage) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(524288);
            ImageIO.write(renderedImage, "png", byteArrayOutputStream);
            return "url(&#34;data:image/png;base64," + Base64.encode(byteArrayOutputStream.toByteArray()) + "&#xA;&#34;)";
        } catch (Exception exception) {
            MudgeSanity.logException("could not transform image", exception, false);
            return "";
        }
    }

    public static String image(String string) {
        byte[] arrby = CommonUtils.readResource(string);
        return "url(&#34;data:image/png;base64," + Base64.encode(arrby) + "&#xA;&#34;)";
    }

    public static String ColumnWidth(String string) {
        return "\t<fo:table-column column-width=\"" + string + "\" />";
    }

    public static void PublishAll(StringBuffer stringBuffer, List<ReportElement> list) {
        for (ReportElement reportElement : list) {
            reportElement.publish(stringBuffer);
        }
    }
}
