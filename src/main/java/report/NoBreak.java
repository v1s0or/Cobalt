package report;

public class NoBreak extends Content {
    public NoBreak(Document paramDocument) {
        super(paramDocument);
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:block page-break-inside=\"avoid\" padding=\"0\" margin=\"0\">");
        super.publish(stringBuffer);
        stringBuffer.append("</fo:block>");
    }
}
