package report;

public class Output extends Content {
    protected String width;

    public Output(Document paramDocument, String string) {
        super(paramDocument);
        this.width = string;
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:block background-color=\"#eeeeee\" content-width=\"" + this.width + "\" linefeed-treatment=\"preserve\" padding=\"8pt\">");
        super.publish(stringBuffer);
        stringBuffer.append("</fo:block>");
    }
}
