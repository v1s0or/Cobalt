package report;

import common.CommonUtils;

public class Page extends Content {
    public static final int PAGE_FIRST = 0;

    public static final int PAGE_REST = 1;

    public static final int PAGE_FIRST_CENTER = 2;

    public static final int PAGE_SINGLE = 3;

    protected int type;

    protected String title;

    public Page(Document paramDocument, int n, String string) {
        super(paramDocument);
        this.type = n;
        this.title = string;
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        if (isEmpty())
            return;
        if (this.type == 0) {
            stringBuffer.append("<fo:page-sequence master-reference=\"first\">\n");
            stringBuffer.append("<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            stringBuffer.append("\t<fo:block border-bottom=\"2pt solid " + ReportUtils.accent() + "\">\n");
            stringBuffer.append("\t\t<fo:external-graphic src=\"" + ReportUtils.logo() + "\" />\n");
            stringBuffer.append("\t</fo:block>\n");
            stringBuffer.append("</fo:static-content>");
        } else if (this.type == 2) {
            stringBuffer.append("<fo:page-sequence master-reference=\"first\">\n");
            stringBuffer.append("\t<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            stringBuffer.append("\t\t<fo:block>\n");
            stringBuffer.append("\t\t\t<fo:table border-bottom=\"2pt solid " + ReportUtils.accent() + "\">");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("2.35in") + "\n");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("4.0in") + "\n");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("2.35in") + "\n");
            stringBuffer.append("\t\t\t\t<fo:table-body>\n");
            stringBuffer.append("\t\t\t\t\t<fo:table-row>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t\t<fo:block>\n");
            stringBuffer.append("\t\t\t\t\t\t\t\t<fo:external-graphic src=\"" + ReportUtils.logo() + "\" />\n");
            stringBuffer.append("\t\t\t\t\t\t\t</fo:block>\n");
            stringBuffer.append("\t\t\t\t\t\t</fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t</fo:table-row>\n");
            stringBuffer.append("\t\t\t\t</fo:table-body>\n");
            stringBuffer.append("\t\t\t</fo:table>\n");
            stringBuffer.append("\t\t</fo:block>\n");
            stringBuffer.append("\t</fo:static-content>\n");
        } else if (this.type == 1) {
            stringBuffer.append("<fo:page-sequence master-reference=\"rest\">\n");
            stringBuffer.append("<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            stringBuffer.append("\t<fo:block border-bottom=\"2pt solid black\" font-family=\"sans-serif\">\n");
            stringBuffer.append("\t\t\t" + Content.fixText(this.title) + "\n");
            stringBuffer.append("\t</fo:block>");
            stringBuffer.append("</fo:static-content>");
            stringBuffer.append(CommonUtils.readResourceAsString("resources/fso/page_footer.fso"));
        } else if (this.type == 3) {
            stringBuffer.append("<fo:page-sequence master-reference=\"first\">\n");
            stringBuffer.append("\t<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            stringBuffer.append("\t\t<fo:block>\n");
            stringBuffer.append("\t\t\t<fo:table>");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("1.1in") + "\n");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("4.0in") + "\n");
            stringBuffer.append("\t\t\t\t" + ReportUtils.ColumnWidth("1.1in") + "\n");
            stringBuffer.append("\t\t\t\t<fo:table-body>\n");
            stringBuffer.append("\t\t\t\t\t<fo:table-row>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t\t<fo:block>\n");
            stringBuffer.append("\t\t\t\t\t\t\t\t<fo:external-graphic src=\"" + ReportUtils.logo() + "\" />\n");
            stringBuffer.append("\t\t\t\t\t\t\t</fo:block>\n");
            stringBuffer.append("\t\t\t\t\t\t</fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            stringBuffer.append("\t\t\t\t\t</fo:table-row>\n");
            stringBuffer.append("\t\t\t\t</fo:table-body>\n");
            stringBuffer.append("\t\t\t</fo:table>\n");
            stringBuffer.append("\t\t</fo:block>\n");
            stringBuffer.append("</fo:static-content>");
        }
        stringBuffer.append("\t\t<fo:flow flow-name=\"xsl-region-body\">");
        super.publish(stringBuffer);
        stringBuffer.append("\t</fo:flow>\n</fo:page-sequence>");
    }
}
