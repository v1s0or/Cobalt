package report;

import java.util.Map;

public class KVTable implements ReportElement {
    protected Map<Object, Object> entries;

    public KVTable(Map map) {
        this.entries = map;
    }

    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:table width=\"100%\" border-separation=\"0\" margin-top=\"8pt\" margin-bottom=\"8pt\">\n");
        stringBuffer.append(ReportUtils.ColumnWidth("2in") + "\n");
        stringBuffer.append(ReportUtils.ColumnWidth("4.5in") + "\n");
        stringBuffer.append("<fo:table-body>\n");
        for (Map.Entry entry : this.entries.entrySet()) {
            stringBuffer.append("\t\t<fo:table-row>\n");
            stringBuffer.append("\t\t\t<fo:table-cell>\n");
            stringBuffer.append("<fo:block font-weight=\"bold\" font-family=\"sans-serif\">" + Content.fixText((String) entry.getKey()) + ":</fo:block>\n");
            stringBuffer.append("\t\t\t</fo:table-cell>\n");
            stringBuffer.append("\t\t\t<fo:table-cell>\n");
            stringBuffer.append("<fo:block font-family=\"sans-serif\">" + Content.fixText((String) entry.getValue()) + "</fo:block>\n");
            stringBuffer.append("\t\t\t</fo:table-cell>\n");
            stringBuffer.append("\t\t</fo:table-row>\n");
        }
        stringBuffer.append("\t</fo:table-body>\n");
        stringBuffer.append("</fo:table>");
    }
}
