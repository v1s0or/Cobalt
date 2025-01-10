package report;

import com.xmlmind.fo.converter.Converter;
import com.xmlmind.fo.converter.OutputDestination;
import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.InputSource;

public class Document extends AObject implements ReportElement {

    public static final int ORIENTATION_PORTRAIT = 0;

    public static final int ORIENTATION_LANDSCAPE = 1;

    protected int orientation = 0;

    protected List pages = new LinkedList();

    protected String title;

    protected Bookmarks bookmarks = new Bookmarks();

    protected FopFactory fopFactory = FopFactory.newInstance();

    public String register(String string) {
        return this.bookmarks.register(string);
    }

    public Bookmarks getBookmarks() {
        return this.bookmarks;
    }

    public void setOrientation(int n) {
        this.orientation = n;
    }

    public Document(String string, int n) {
        this.orientation = n;
        this.title = string;
    }

    public Page addPage(int n) {
        Page page = new Page(this, n, this.title);
        this.pages.add(page);
        return page;
    }

    public void toWord(File file) {
        try {
            Converter converter = new Converter();
            converter.setProperty("outputFormat", "docx");
            converter.setProperty("outputEncoding", "UTF-8");
            InputSource inputSource = new InputSource(toStream());
            OutputDestination outputDestination = new OutputDestination(file.getPath());
            converter.convert(inputSource, outputDestination);
        } catch (Exception exception) {
            MudgeSanity.logException("document -> toWord failed [see out.fso]: "
                    + file, exception, false);
            toFSO(new File("out.fso"));
        }
    }

    public void toFSO(File file) {
        try {
            StringBuffer stringBuffer = new StringBuffer(0x100000);
            publish(stringBuffer);
            FileOutputStream fileOutputStream = new FileOutputStream("out.fso");
            fileOutputStream.write(CommonUtils.toBytes(stringBuffer.toString()));
            fileOutputStream.close();
        } catch (Exception exception) {
            MudgeSanity.logException("document -> toFSO failed: " + file, exception, false);
        }
    }

    public void toPDF(File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Fop fop = this.fopFactory.newFop("application/pdf", fileOutputStream);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StreamSource streamSource = new StreamSource(toStream());
            SAXResult sAXResult = new SAXResult(fop.getDefaultHandler());
            transformer.transform(streamSource, sAXResult);
            fileOutputStream.close();
        } catch (Exception exception) {
            MudgeSanity.logException("document -> toPDF failed [see out.fso]: "
                    + file, exception, false);
            toFSO(new File("out.fso"));
        }
    }

    protected InputStream toStream() {
        try {
            StringBuffer stringBuffer = new StringBuffer(0x100000);
            publish(stringBuffer);
            return new ByteArrayInputStream(stringBuffer.toString().getBytes("UTF-8"));
        } catch (Exception exception) {
            MudgeSanity.logException("output -> toStream failed", exception, false);
            return new ByteArrayInputStream(new byte[ORIENTATION_PORTRAIT]);
        }
    }

    public void publish(StringBuffer stringBuffer) {
        if (this.orientation == ORIENTATION_PORTRAIT) {
            stringBuffer.append(CommonUtils
                    .readResourceAsString("resources/fso/document_start_portrait.fso"));
        } else if (this.orientation == ORIENTATION_LANDSCAPE) {
            stringBuffer.append(CommonUtils
                    .readResourceAsString("resources/fso/document_start_landscape.fso"));
        }
        getBookmarks().publish(stringBuffer);
        ReportUtils.PublishAll(stringBuffer, this.pages);
        stringBuffer.append("</fo:root>");
    }
}
