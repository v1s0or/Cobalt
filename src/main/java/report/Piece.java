package report;

public class Piece implements ReportElement {
    protected String text;

    public Piece(String string) {
        this.text = string;
    }

    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append(this.text);
    }
}
