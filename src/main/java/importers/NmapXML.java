package importers;

import common.CommonUtils;
import common.MudgeSanity;
import common.OperatingSystem;

import java.io.File;
import java.io.FileInputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NmapXML extends Importer {
    public NmapXML(ImportHandler importHandler) {
        super(importHandler);
    }

    public boolean isNmapXML(File file) {
        String str = CommonUtils.peekFile(file, 1024);
        return (str.startsWith("<?xml") && str.indexOf("<nmaprun") > 0);
    }

    public boolean parse(File file) {
        if (!isNmapXML(file))
            return false;
        try {
            SAXParserFactory sAXParserFactory = SAXParserFactory.newInstance();
            SAXParser sAXParser = sAXParserFactory.newSAXParser();
            sAXParser.parse(new FileInputStream(file), new NmapHandler());
            return true;
        } catch (Exception exception) {
            MudgeSanity.logException("Nmap XML is partially corrupt: " + file, exception, false);
            return true;
        }
    }

    class NmapHandler extends DefaultHandler {
        protected String host;

        protected boolean up = false;

        protected String port;

        protected String product = null;

        protected String version = null;

        protected boolean hasport = false;

        protected OperatingSystem os = null;

        protected int osscore = 0;

        public void startElement(String string1, String string2, String string3, Attributes param1Attributes) throws SAXException {
            if ("host".equals(string3)) {
                this.os = null;
                this.osscore = 0;
                this.port = null;
                this.up = false;
                this.host = null;
                this.product = null;
                this.version = null;
                this.hasport = false;
            } else if ("status".equals(string3)) {
                this.up = "up".equals(param1Attributes.getValue("state"));
            } else if ("address".equals(string3) && "ipv4".equals(param1Attributes.getValue("addrtype"))) {
                this.host = param1Attributes.getValue("addr");
            } else if ("address".equals(string3) && "ipv6".equals(param1Attributes.getValue("addrtype"))) {
                this.host = param1Attributes.getValue("addr");
            } else if ("port".equals(string3)) {
                this.port = param1Attributes.getValue("portid");
                this.product = null;
                this.version = null;
            } else if ("service".equals(string3)) {
                this.product = param1Attributes.getValue("product");
                this.version = param1Attributes.getValue("version");
            } else if ("state".equals(string3)) {
                this.hasport = true;
            } else if ("os".equals(string3)) {
                this.os = null;
                this.osscore = 0;
            } else if ("osclass".equals(string3)) {
                String str1 = param1Attributes.getValue("osfamily");
                String str2 = param1Attributes.getValue("osgen");
                int i = CommonUtils.toNumber(param1Attributes.getValue("accuracy"), 0);
                OperatingSystem operatingSystem = new OperatingSystem(str1 + " " + str2);
                if (i > this.osscore && !operatingSystem.isUnknown()) {
                    this.os = operatingSystem;
                    this.osscore = i;
                }
            }
        }

        public void endElement(String string1, String string2, String string3) throws SAXException {
            if (this.hasport && "host".equals(string3)) {
                if (this.os != null) {
                    NmapXML.this.host(this.host, null, this.os.getName(), this.os.getVersion());
                } else {
                    NmapXML.this.host(this.host, null, null, 0.0D);
                }
            } else if (this.up && "service".equals(string3)) {
                if (this.product != null && this.version != null) {
                    NmapXML.this.service(this.host, this.port, this.product + " " + this.version);
                } else if (this.product != null) {
                    NmapXML.this.service(this.host, this.port, this.product);
                } else {
                    NmapXML.this.service(this.host, this.port, null);
                }
            }
        }
    }
}
