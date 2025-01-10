package report;

import common.CommonUtils;
import common.RegexParser;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import report.Document;
import report.FoBlock;
import report.KVTable;
import report.Layout;
import report.ListItem;
import report.NoBreak;
import report.Output;
import report.Piece;
import report.ReportElement;
import report.ReportUtils;
import report.Table;
import report.UnorderedList;

public class Content implements ReportElement {

    protected List elements = new LinkedList();
    protected Document parent = null;

    public static boolean isAllowed(char c) {
        if (c == '\t' || c == '\n' || c == '\r') {
            return true;
        }
        if (c >= ' ' && c <= '\ud7ff') {
            return true;
        }
        if (c >= '\ue000' && c <= '\ufffd') {
            return true;
        }
        // return c >= '\u10000' && c <= '\u10ffff';
        return c >= 65536 && c <= 1114111;
    }

    public static String fixText(String string) {
        StringBuffer stringBuffer = new StringBuffer(string.length() * 2);
        char[] arrc = string.toCharArray();
        for (int i = 0; i < arrc.length; i++) {
            if (arrc[i] == '&') {
                stringBuffer.append("&amp;");
            } else if (arrc[i] == '<') {
                stringBuffer.append("&lt;");
            } else if (arrc[i] == '>') {
                stringBuffer.append("&gt;");
            } else if (arrc[i] == '"') {
                stringBuffer.append("&quot;");
            } else if (arrc[i] == '\'') {
                stringBuffer.append("&apos;");
            } else if (isAllowed(arrc[i])) {
                // if (arrc[i] > '?') {// '\u00ff', but '?' is \u003f
                if (arrc[i] > 255) {
                    String str1 = CommonUtils.toHex(arrc[i]);
                    String str2 = CommonUtils.padr(str1, "0", 4);
                    stringBuffer.append("&#x");
                    stringBuffer.append(str2);
                    stringBuffer.append(";");
                } else {
                    stringBuffer.append(arrc[i]);
                }
            }
        }
        return stringBuffer.toString();
    }

    public Content(Document paramDocument) {
        this.parent = paramDocument;
    }

    public void h1(String string) {
        h1(string, string, "left");
    }

    public void h1(String string1, String string2, String string3) {
        string1 = fixText(string1);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<fo:block font-size=\"18pt\"\n");
        stringBuffer.append("\t\tfont-family=\"sans-serif\"\n");
        stringBuffer.append("\t\tid=\"" + this.parent.register(string2) + "\"\n");
        stringBuffer.append("\t\tfont-weight=\"bold\"\n");
        stringBuffer.append("\t\tline-height=\"24pt\"\n");
        stringBuffer.append("\t\tspace-after.optimum=\"15pt\"\n");
        stringBuffer.append("\t\tcolor=\"black\"\n");
        stringBuffer.append("\t\ttext-align=\"" + string3 + "\"\n");
        stringBuffer.append("\t\tpadding-top=\"12pt\">\n");
        stringBuffer.append("\t" + string1 + "\n");
        stringBuffer.append("</fo:block>");
        this.elements.add(new Piece(stringBuffer.toString()));
    }

    public void h2(String string) {
        h2(string, string);
    }

    public void h2(String string1, String string2) {
        string1 = fixText(string1);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<fo:block font-size=\"15pt\"\n");
        stringBuffer.append("\t\tfont-family=\"sans-serif\"\n");
        stringBuffer.append("\t\tfont-weight=\"bold\"\n");
        stringBuffer.append("\t\tid=\"" + this.parent.register(string2) + "\"\n");
        stringBuffer.append("\t\tline-height=\"24pt\"\n");
        stringBuffer.append("\t\tspace-after.optimum=\"15pt\"\n");
        stringBuffer.append("\t\tcolor=\"black\"\n");
        stringBuffer.append("\t\ttext-align=\"left\"\n");
        stringBuffer.append("\t\tpadding-top=\"6pt\"\n");
        stringBuffer.append("\t\tpadding-bottom=\"6pt\"\n");
        stringBuffer.append("\t\tmargin-bottom=\"0\">\n");
        stringBuffer.append("\t<fo:inline text-decoration=\"underline\">" + string1 + "</fo:inline>\n");
        stringBuffer.append("</fo:block>");
        this.elements.add(new Piece(stringBuffer.toString()));
    }

    public void img(String string1, String string2) {
        this.elements.add(new Piece("<fo:external-graphic src=\"" + string1 + "\" content-width=\"" + string2 + "\" />"));
    }

    public void h2_img(BufferedImage bufferedImage, String string) {
        h2_img(bufferedImage, string, string);
    }

    public void h2_img(BufferedImage bufferedImage, String string1, String string2) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<fo:table border-separation=\"0\" margin-top=\"4pt\" margin-bottom=\"8pt\" width=\"100%\">\n");
        stringBuffer.append("\t<fo:table-body>\n");
        stringBuffer.append("\t\t<fo:table-row>\n");
        stringBuffer.append("\t\t\t<fo:table-cell display-align=\"after\" width=\"0.6in\">\n");
        stringBuffer.append("\t\t\t\t<fo:block padding=\"0\" margin=\"0\">\n");
        this.elements.add(new Piece(stringBuffer.toString()));
        img(ReportUtils.image(bufferedImage), "0.5in");
        stringBuffer = new StringBuffer();
        stringBuffer.append("\t\t\t\t</fo:block>\n");
        stringBuffer.append("\t\t\t</fo:table-cell>\n");
        stringBuffer.append("\t\t\t<fo:table-cell display-align=\"center\" width=\"6in\">\n");
        this.elements.add(new Piece(stringBuffer.toString()));
        h2(string1, string2);
        stringBuffer = new StringBuffer();
        stringBuffer.append("\t\t\t</fo:table-cell>\n");
        stringBuffer.append("\t\t</fo:table-row>\n");
        stringBuffer.append("\t</fo:table-body>\n");
        stringBuffer.append("</fo:table>\n");
        this.elements.add(new Piece(stringBuffer.toString()));
    }

    public void h3(String string) {
        string = fixText(string);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<fo:block font-size=\"14pt\"\n");
        stringBuffer.append("\t\tfont-family=\"sans-serif\"\n");
        stringBuffer.append("\t\tfont-weight=\"bold\"\n");
        stringBuffer.append("\t\tline-height=\"24pt\"\n");
        stringBuffer.append("\t\tspace-after.optimum=\"15pt\"\n");
        stringBuffer.append("\t\tcolor=\"black\"\n");
        stringBuffer.append("\t\ttext-align=\"left\"\n");
        stringBuffer.append("\t\tpadding-top=\"6pt\"\n");
        stringBuffer.append("\t\tpadding-bottom=\"6pt\"\n");
        stringBuffer.append("\t\tmargin-bottom=\"0\">\n");
        stringBuffer.append("\t" + string + "\n");
        stringBuffer.append("</fo:block>");
        this.elements.add(new Piece(stringBuffer.toString()));
    }

    public void kvtable(Map map) {
        this.elements.add(new KVTable(map));
    }

    public Content block(String string) {
        FoBlock foBlock = new FoBlock(this.parent, string);
        this.elements.add(foBlock);
        return foBlock;
    }

    public Content string() {
        return new Content(this.parent);
    }

    public Content output(String string) {
        Output output = new Output(this.parent, string);
        this.elements.add(output);
        return output;
    }

    public Content nobreak() {
        NoBreak noBreak = new NoBreak(this.parent);
        this.elements.add(noBreak);
        return noBreak;
    }

    public void list(List list) {
        Content content = ul();
        for (Object object : list) {
            if (object != null) {
                content.li().text(object.toString());
            }
        }
    }

    public void list_formatted(List list) {
        Content content = ul();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            String str = (iterator.next() + "").trim();
            RegexParser regexParser = new RegexParser(str);
            if (regexParser.matches("'''(.*?)'''(.*?)")) {
                Content content1 = content.li();
                content1.b(regexParser.group(1));
                content1.text(regexParser.group(2));
                continue;
            }
            if (!"".equals(str)) {
                content.li().text(str);
            }
        }
    }

    public void link(String string1, String string2) {
        string1 = fixText(string1);
        this.elements.add(new Piece(ReportUtils.a(string1, string2)));
    }

    public void link_bullet(String string1, String string2) {
        Content content = ul();
        content.li().link(string1, string2);
    }

    public Content li() {
        ListItem listItem = new ListItem(this.parent);
        this.elements.add(listItem);
        return listItem;
    }

    public Content ul() {
        UnorderedList unorderedList = new UnorderedList(this.parent);
        this.elements.add(unorderedList);
        return unorderedList;
    }

    public void b(String string) {
        string = fixText(string);
        this.elements.add(new Piece("<fo:inline font-weight=\"bold\">" + string + "</fo:inline>"));
    }

    public void text(String string) {
        string = fixText(string);
        this.elements.add(new Piece("<fo:inline>" + string + "</fo:inline>"));
    }

    public void color(String string1, String string2) {
        string1 = fixText(string1);
        this.elements.add(new Piece("<fo:inline color=\"" + string2 + "\">" + string1 + "</fo:inline>"));
    }

    public void color2(String string1, String string2, String string3) {
        string1 = fixText(string1);
        this.elements.add(new Piece("<fo:inline color=\"" + string2 + "\" background-color=\"" + string3 + "\">" + string1 + "</fo:inline>"));
    }

    public void h4(String string1, String string2) {
        string1 = fixText(string1);
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"bold\" color=\"black\" text-align=\"" + string2 + "\">" + string1 + "</fo:block>"));
    }

    public void p(String string1, String string2) {
        string1 = fixText(string1);
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"normal\" color=\"black\" text-align=\"" + string2 + "\">" + string1 + "</fo:block>"));
    }

    public void br() {
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"normal\" color=\"black\" text-align=\"left\">&#160;</fo:block>"));
    }

    public void table(List list1, List list2, List list3) {
        this.elements.add(new Table(list1, list2, list3));
    }

    public void layout(List list1, List list2, List list3) {
        this.elements.add(new Layout(list1, list2, list3));
    }

    public void ts() {
        String str = DateFormat.getDateInstance(2).format(new Date());
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" padding-bottom=\"8pt\" font-family=\"sans-serif\" font-style=\"italic\" font-weight=\"normal\" color=\"black\" text-align=\"left\">" + str + "</fo:block>"));
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        ReportUtils.PublishAll(stringBuffer, this.elements);
    }

    public boolean isEmpty() {
        return (this.elements.size() == 0);
    }

    public void bookmark(String string) {
        this.parent.getBookmarks().bookmark(string);
    }

    public void bookmark(String string1, String string2) {
        this.parent.getBookmarks().bookmark(string1, string2);
    }
}
