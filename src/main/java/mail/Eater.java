package mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Header;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.field.ParseException;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.message.AbstractEntity;
import org.apache.james.mime4j.message.AbstractMessage;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.message.DefaultMessageWriter;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.TempFileStorageProvider;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;

public class Eater {

    static {
        TempFileStorageProvider tempFileStorageProvider = new TempFileStorageProvider();
        DefaultStorageProvider.setInstance(tempFileStorageProvider);
    }

    protected Message message;

    public Eater(String name) throws IOException {
        this(new FileInputStream(name));
    }

    public Eater(InputStream fis) {
        DefaultMessageBuilder defaultMessageBuilder = new DefaultMessageBuilder();

        MimeConfig config = new MimeConfig();
        config.setMaxLineLen(-1);
        config.setMaxHeaderLen(-1);
        config.setMaxHeaderCount(-1);
        defaultMessageBuilder.setMimeEntityConfig(config);

        try {
            this.message = defaultMessageBuilder.parseMessage(fis);
            stripHeaders(this.message);
            stripAttachments(this.message);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void done() {
        this.message.dispose();
    }

    public static void main(String[] args) throws Exception {
        Eater temp = new Eater(args[0]);
        try {
            if (args.length == 2) {
                temp.attachFile("build.xml");
            }
            System.out.println(new String(temp.getMessage(
                    "Raphael Mudge <rsmudge@gmail.com>", "Test User <test@aol.com>"),
                    StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void stripHeaders(Message message) {
        Header h = message.getHeader();
        h.removeFields("Authentication-Results");
        h.removeFields("Delivered-To");
        h.removeFields("DKIM-Signature");
        h.removeFields("DomainKey-Signature");
        h.removeFields("DomainKey-Status");
        h.removeFields("In-Reply-To");
        h.removeFields("Message-ID");
        h.removeFields("Received");
        h.removeFields("Received-SPF");
        h.removeFields("References");
        h.removeFields("Reply-To");
        h.removeFields("Return-Path");
        h.removeFields("Sender");
        h.removeFields("X-AUTH-Result");
        h.removeFields("X-Message-Delivery");
        h.removeFields("X-Message-Info");
        h.removeFields("X-Message-Status");
        h.removeFields("X-Original-Authentication-Results");
        h.removeFields("X-OriginalArrivalTime");
        h.removeFields("X-Original-Sender");
        h.removeFields("X-SID-PRA");
        h.removeFields("CC");
        h.removeFields("Return-Path");
        h.removeFields("Envelope-to");
        h.removeFields("Delivery-date");
        h.removeFields("X-Sender");
        h.removeFields("X-AntiAbuse");
        h.removeFields("X-Filter-ID");
        h.removeFields("X-Originating-IP");
        h.removeFields("X-SpamExperts-Domain");
        h.removeFields("X-SpamExperts-Username");
        h.removeFields("X-SpamExperts-Outgoing-Class");
        h.removeFields("X-SpamExperts-Outgoing-Evidence");
        h.removeFields("X-Recommended-Action");
        h.removeFields("X-DKIM");
        h.removeFields("X-DomainKeys");
        h.removeFields("X-Spam-Checker-Version");
        h.removeFields("X-Spam-Checker");
        h.removeFields("X-Spam-Level");
        h.removeFields("X-Spam-Status");
        h.removeFields("X-MS-Has-Attach");
        h.removeFields("X-MS-TNEF-Correlator");
        h.removeFields("x-ms-exchange-transport-fromentityheader");
        h.removeFields("x-microsoft-antispam");
        h.removeFields("x-forefront-prvs");
        h.removeFields("x-forefront-antispam-report");
        h.removeFields("X-WSS-ID");
        h.removeFields("X-M-MSG");
    }

    protected String extractContent(Entity ent) {
        try {
            SingleBody body = (SingleBody) ent.getBody();
            InputStream stream = body.getInputStream();
            InputStreamReader r = new InputStreamReader(stream, ent.getCharset());
            char[] buffer = new char[0x200000];
            int len = r.read(buffer);
            return new String(buffer, 0, len);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }


    public String getMessageEntity(String mimetype) {
        return getMessageEntity(this.message, mimetype);
    }


    protected String getMessageEntity(Entity ent, String mimetype) {
        String result = null;

        if (ent.getBody() instanceof SingleBody && mimetype.equals(ent.getMimeType())) {
            return extractContent(ent);
        }
        if (ent.getBody() instanceof Multipart) {
            Multipart body = (Multipart) ent.getBody();
            Iterator i = body.getBodyParts().iterator();
            while (i.hasNext()) {
                Entity e = (Entity) i.next();
                result = getMessageEntity(e, mimetype);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected void fixMessageType(Entity ent) {
        if (!(ent.getBody() instanceof SingleBody)) {
            return;
        }
        if ("text/plain".equals(ent.getMimeType())) {
            try {
                SingleBody body = (SingleBody) ent.getBody();
                InputStream stream = body.getInputStream();

                InputStreamReader r = new InputStreamReader(stream, ent.getCharset());
                char[] buffer = new char[0x200000];
                int len = r.read(buffer);

                String changeme = new String(buffer, 0, len);
                changeme = changeme.replaceAll("(?i:(http[s]{0,1}://[^\\n\\s]*))",
                        "<a href=\"$1\">$1</a>");
                changeme = changeme.replaceAll("\n", "\n<br />");

                TextBody hotty = new BasicBodyFactory().textBody(changeme, ent.getCharset());

                AbstractEntity entity = (AbstractEntity) ent;
                entity.setContentTransferEncoding("7bit");

                ent.removeBody();
                ((AbstractEntity) ent).setText(hotty, "html");
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    protected void fixMessageEncoding(Entity ent, String transferEncoding) {
        String type = ent.getDispositionType();

        if (ent.isMultipart()) {
            Multipart mbody = (Multipart) ent.getBody();
            Iterator i = mbody.getBodyParts().iterator();
            while (i.hasNext()) {
                Entity next = (Entity) i.next();
                fixMessageEncoding(next, next.getContentTransferEncoding());
            }
        } else {
            if (!(ent.getBody() instanceof SingleBody)) {
                return;
            }
            if (!"inline".equals(type) || !"base64".equals(transferEncoding)) {

                if (!"attachment".equals(type) || getContentId(ent) == null
                        || !"base64".equals(transferEncoding)) {

                    if (!"base64".equals(transferEncoding) || getContentId(ent) == null) {

                        if ("quoted-printable".equals(transferEncoding)
                                || "base64".equals(transferEncoding)) {
                            try {
                                SingleBody body = (SingleBody) ent.getBody();
                                InputStream stream = body.getInputStream();
                                TextBody hotty = new BasicBodyFactory()
                                        .textBody(stream, ent.getCharset());

                                AbstractEntity entity = (AbstractEntity) ent;
                                entity.setContentTransferEncoding("7bit");

                                ent.removeBody();
                                ent.setBody(hotty);
                            } catch (IOException ioex) {
                                ioex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public String getContentId(Entity e) {
        if (e == null) {
            return null;
        }
        Header header = e.getHeader();
        if (header == null) {
            return null;
        }
        Field field = header.getField("Content-ID");
        if (field == null) {
            return null;
        }
        return field.getBody();
    }

    protected void stripAttachments(Message message) {
        if (!message.isMultipart()) {
            fixMessageEncoding(message, message.getContentTransferEncoding());
            fixMessageType(message);

            return;
        }
        while (true) {
            Multipart multipart = (Multipart) message.getBody();
            List<Entity> tempz = multipart.getBodyParts();
            Iterator<Entity> i = tempz.iterator();
            int x = 0;
            int y = -1;
            while (i.hasNext()) {
                Entity ent = (Entity) i.next();

                if ("attachment".equals(ent.getDispositionType()) && getContentId(ent) == null) {
                    y = x;
                } else {

                    fixMessageEncoding(ent, ent.getContentTransferEncoding());
                }

                x++;
            }

            if (y != -1) {
                multipart.removeBodyPart(y);
                continue;
            }
            break;
        }
    }

    public void attachFile(String file) throws IOException {
        if (!this.message.isMultipart()) {
            attachFileSingle(file);
        } else {

            attachFileMultipart(file);
        }
    }

    public void attachFileSingle(String file) throws IOException {
        try {
            MultipartImpl multipartImpl = new MultipartImpl("mixed");

            Body oldmessage = this.message.removeBody();
            BodyPart content = new BodyPart();
            content.setBody(oldmessage, this.message.getMimeType());

            BodyPart attachment = createAttachment(file);

            multipartImpl.addBodyPart(content);
            multipartImpl.addBodyPart(attachment);

            ((AbstractEntity) this.message).setMultipart(multipartImpl);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    public void attachFileMultipart(String file) throws IOException {
        try {
            Multipart multipart = (Multipart) this.message.getBody();
            BodyPart attachment = createAttachment(file);

            if ("mixed".equals(multipart.getSubType())) {
                multipart.addBodyPart(attachment);
            } else {

                MultipartImpl multipartImpl = new MultipartImpl("mixed");


                Entity favored = null;
                Iterator i = multipart.getBodyParts().iterator();
                while (i.hasNext()) {
                    Entity ent = (Entity) i.next();
                    if ("text/html".equals(ent.getMimeType())) {
                        favored = ent;
                        continue;
                    }
                    if (favored == null) {
                        favored = ent;
                        fixMessageType(favored);
                    }
                }

                if (favored != null) {
                    multipartImpl.addBodyPart(favored);
                }
                multipartImpl.addBodyPart(attachment);

                this.message.removeBody();
                ((AbstractEntity) this.message).setMultipart(multipartImpl);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getSubject() {
        return this.message.getSubject();
    }

    public byte[] getMessage(String from, String to) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(0x400000);
        try {
            this.message.setDate(new Date());

            if (from != null) {
                this.message.setFrom(AddressBuilder.DEFAULT.parseMailbox(from));
            }
            if (to != null) {
                this.message.setTo(AddressBuilder.DEFAULT.parseMailbox(to));
            }
            DefaultMessageWriter writer = new DefaultMessageWriter();
            writer.writeMessage(this.message, output);
        } catch (Exception ioex) {
            throw new RuntimeException(ioex);
        }

        return output.toByteArray();
    }

    private BodyPart createAttachment(String name) throws IOException {
        File file = new File(name);
        String namez = file.getName();

        BinaryBody body = new StorageBodyFactory().binaryBody(new FileInputStream(name));

        Map<String, String> temp = new HashMap();
        temp.put("name", namez);

        BodyPart bodyPart = new BodyPart();
        bodyPart.setBody(body, "application/octet-stream", temp);
        bodyPart.setContentTransferEncoding("base64");
        bodyPart.setContentDisposition("attachment");
        bodyPart.setFilename(namez);

        return bodyPart;
    }
}
