package report;

import dialog.DialogUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Table implements ReportElement {
    protected List<String> columns;

    protected List<String> widths;

    protected List<Map> rows;

    public Table(List list1, List list2, List list3) {
        this.columns = list1;
        this.widths = list2;
        this.rows = list3;
    }

    public void publish(StringBuffer stringBuffer) {
        if (this.rows.size() == 0) {
            stringBuffer.append(ReportUtils.br());
            return;
        }
        stringBuffer.append("<fo:table border-bottom=\"1pt solid black\" margin-bottom=\"12pt\" border-left=\"none\" border-right=\"none\" width=\"100%\" border-separation=\"0\">\n");
        for (String str : this.widths) {
            stringBuffer.append(ReportUtils.ColumnWidth(str) + "\n");
        }
        stringBuffer.append("\t<fo:table-header border-bottom=\"1pt solid black\" background-color=\"#cccccc\">\n");
        Iterator iterator1 = this.columns.iterator();
        for (int n = 0; iterator1.hasNext(); n++) {
            String str = (String) iterator1.next();
            stringBuffer.append("\t\t<fo:table-cell>\n");
            stringBuffer.append("\t\t\t<fo:block font-weight=\"bold\" ");
            if (n == 0) {
                stringBuffer.append("margin-left=\"2pt\"");
            }
            stringBuffer.append(" font-family=\"sans-serif\" padding=\"0.02in\" padding-left=\"0in\">\n");
            stringBuffer.append(Content.fixText(str));
            stringBuffer.append("\t\t\t</fo:block>\n");
            stringBuffer.append("\t\t</fo:table-cell>\n");
        }
        stringBuffer.append("\t</fo:table-header>\n");
        stringBuffer.append("\t<fo:table-body>\n");
        Iterator iterator2 = this.rows.iterator();
        for (int b = 0; iterator2.hasNext(); b++) {
            Map map = (Map) iterator2.next();
            if (b % 2 == 1) {
                stringBuffer.append("\t\t<fo:table-row background-color=\"#eeeeee\" border=\"none\" margin=\"0\" padding=\"0\">\n");
            } else {
                stringBuffer.append("\t\t<fo:table-row>\n");
            }
            Iterator iterator = this.columns.iterator();
            for (int b1 = 0; iterator.hasNext(); b1++) {
                String str1 = (String) iterator.next();
                if (b1==0) {
                    stringBuffer.append("\t\t\t<fo:table-cell margin-left=\"2pt\">\n");
                } else {
                    stringBuffer.append("\t\t\t<fo:table-cell>\n");
                }
                String str2 = DialogUtils.string(map, str1);
                if (str2 == null) {
                    str2 = "";
                }
                stringBuffer.append("\t\t\t\t<fo:block padding-top=\"2pt\" padding-bottom=\"1pt\" font-family=\"sans-serif\">" + str2 + "</fo:block>\n");
                stringBuffer.append("\t\t\t</fo:table-cell>\n");
            }
            stringBuffer.append("\t\t</fo:table-row>\n");
        }
        stringBuffer.append("\t</fo:table-body>\n");
        stringBuffer.append("</fo:table>");
    }
}
