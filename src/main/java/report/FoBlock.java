package report;

public class FoBlock extends Content {
    protected String align;

    public FoBlock(Document paramDocument, String string) {
        super(paramDocument);
        this.align = string;
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:block linefeed-treatment=\"preserve\" text-align=\"" + this.align + "\">");
        super.publish(stringBuffer);
        stringBuffer.append("</fo:block>");
    }
}
