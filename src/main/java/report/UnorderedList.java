package report;

public class UnorderedList extends Content {
    public UnorderedList(Document paramDocument) {
        super(paramDocument);
    }

    @Override
    public void publish(StringBuffer stringBuffer) {
        stringBuffer.append("<fo:list-block provisional-distance-between-starts=\"0.2cm\" provisional-label-separation=\"0.5cm\" padding-top=\"6pt\" space-after=\"12pt\" start-indent=\"1cm\">");
        super.publish(stringBuffer);
        stringBuffer.append("</fo:list-block>");
    }
}
