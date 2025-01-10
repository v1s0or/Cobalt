package report;

public class ListItem extends Content {
    public ListItem(Document paramDocument) {
        super(paramDocument);
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:list-item>\n");
        stringBuffer.append("\t<fo:list-item-label end-indent=\"label-end()\">\n");
        stringBuffer.append("\t\t<fo:block font-family=\"sans-serif\">&#x2022;</fo:block>\n");
        stringBuffer.append("\t</fo:list-item-label>\n");
        stringBuffer.append("\t<fo:list-item-body start-indent=\"body-start()\">\n");
        stringBuffer.append("\t\t<fo:block margin-left=\"0.05in\" font-family=\"sans-serif\">");
        super.publish(stringBuffer);
        stringBuffer.append("\t\t</fo:block>");
        stringBuffer.append("\t</fo:list-item-body>");
        stringBuffer.append("</fo:list-item>");
    }
}
