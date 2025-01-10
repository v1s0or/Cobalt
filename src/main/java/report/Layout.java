package report;

import dialog.DialogUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Layout implements ReportElement {
    protected List<String> columns;

    protected List<String> widths;

    protected List rows;

    public Layout(List list1, List list2, List list3) {
        this.columns = list1;
        this.widths = list2;
        this.rows = list3;
    }

    public void publish(StringBuffer stringBuffer) {
        if (this.rows.size() == 0) {
            stringBuffer.append(ReportUtils.br());
            return;
        }
        stringBuffer.append("<fo:table border-bottom=\"none\" border-left=\"none\" border-right=\"none\" width=\"100%\" border-separation=\"0\">\n");
        for (String str : this.widths) {
            stringBuffer.append(ReportUtils.ColumnWidth(str) + "\n");
        }
        stringBuffer.append("\t<fo:table-body>\n");
        Iterator iterator = this.rows.iterator();
        for (int b = 0; iterator.hasNext(); b++) {
            Map map = (Map) iterator.next();
            stringBuffer.append("\t\t<fo:table-row>\n");
            Iterator iterator1 = this.columns.iterator();
            for (int b1 = 0; iterator1.hasNext(); b1++) {
                String str1 = (String) iterator1.next();
                stringBuffer.append("\t\t\t<fo:table-cell>\n");
                String str2 = DialogUtils.string(map, str1);
                if (str2 == null) {
                    str2 = "";
                }
                stringBuffer.append("\t\t\t\t<fo:block font-family=\"sans-serif\">" + str2 + "</fo:block>\n");
                stringBuffer.append("\t\t\t</fo:table-cell>\n");
            }
            stringBuffer.append("\t\t</fo:table-row>\n");
        }
        stringBuffer.append("\t</fo:table-body>\n");
        stringBuffer.append("</fo:table>");
    }
}
