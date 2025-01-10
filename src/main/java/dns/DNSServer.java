package dns;

import common.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DNSServer implements Runnable {

    protected Thread fred;

    protected DatagramSocket server;

    protected DatagramPacket in = new DatagramPacket(new byte[512], 512);

    protected DatagramPacket out = new DatagramPacket(new byte[512], 512);

    protected Handler listener = null;

    protected int ttl = 1;

    public static final int DNS_TYPE_A = 1;

    public static final int DNS_TYPE_AAAA = 28;

    public static final int DNS_TYPE_CNAME = 5;

    public static final int DNS_TYPE_TXT = 16;

    protected boolean isup = true;

    public void setDefaultTTL(int n) {
        this.ttl = n;
    }

    public static Response A(long l) {
        Response response = new Response();
        response.type = DNS_TYPE_A;
        response.addr4 = l;
        return response;
    }

    public static Response TXT(byte[] arrby) {
        Response response = new Response();
        response.type = DNS_TYPE_TXT;
        response.data = arrby;
        return response;
    }

    public static Response AAAA(byte[] arrby) {
        Response response = new Response();
        response.type = DNS_TYPE_AAAA;
        response.data = arrby;
        return response;
    }

    public void installHandler(Handler handler) {
        this.listener = handler;
    }

    public byte[] respond(byte[] arrby) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        DNSHeader dNSHeader = new DNSHeader(arrby);
        dNSHeader.flags = (short) dNSHeader.flags | 0x8000;
        dNSHeader.ancount++;
        dNSHeader.nscount = 0;
        dNSHeader.arcount = 0;
        for (DNSQuestion dNSQuestion : dNSHeader.getQuestions()) {
            DNSAnswer dNSAnswer = new DNSAnswer(dNSQuestion);
            dNSQuestion.setAnswer(dNSAnswer);
        }
        for (DNSQuestion dNSQuestion : dNSHeader.getQuestions()) {
            DNSAnswer dNSAnswer = dNSQuestion.getAnswer();
            if (dNSQuestion.getType() == DNS_TYPE_AAAA && dNSAnswer.getType() == DNS_TYPE_A) {
                dNSHeader.ancount--;
            }
        }
        dataOutputStream.writeShort(dNSHeader.id);
        dataOutputStream.writeShort(dNSHeader.flags);
        dataOutputStream.writeShort(dNSHeader.qdcount);
        dataOutputStream.writeShort(dNSHeader.ancount);
        dataOutputStream.writeShort(dNSHeader.nscount);
        dataOutputStream.writeShort(dNSHeader.arcount);
        /*null = dNSHeader.getQuestions().iterator();
        int i;
        for (i = 12; null.hasNext(); i += dNSQuestion.getSize()) {
            DNSQuestion dNSQuestion = (DNSQuestion) null.next();
            dataOutputStream.write(arrby, i, dNSQuestion.getSize());
        }*/

        Iterator iterator = dNSHeader.getQuestions().iterator();
        int n = 12;
        while (iterator.hasNext()) {
            DNSQuestion dNSQuestion = (DNSQuestion) iterator.next();
            dataOutputStream.write(arrby, n, dNSQuestion.getSize());
            n += dNSQuestion.getSize();
        }

        for (DNSQuestion dNSQuestion : dNSHeader.getQuestions()) {
            DNSAnswer dNSAnswer = dNSQuestion.getAnswer();
            if (dNSQuestion.getType() == DNS_TYPE_AAAA && dNSAnswer.getType() == DNS_TYPE_A) {
                CommonUtils.print_warn("Dropped AAAA request for: " + dNSQuestion.qname + " (A request expected)");
                continue;
            }
            dataOutputStream.write(dNSAnswer.getAnswer());
        }
        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public DNSServer(int n) throws SocketException {
        this.server = new DatagramSocket(n);
    }

    public void stop() {
        this.isup = false;
        this.fred.interrupt();
        try {
            this.server.close();
        } catch (Exception exception) {
        }
    }

    public void run() {
        while (this.isup) {
            try {
                this.server.receive(this.in);
                DNSHeader dNSHeader = new DNSHeader(this.in.getData());
                this.out.setAddress(this.in.getAddress());
                this.out.setPort(this.in.getPort());
                this.out.setData(respond(this.in.getData()));
                this.server.send(this.out);
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
        try {
            this.server.close();
        } catch (Exception exception) {
        }
        CommonUtils.print_info("DNS server stopped");
    }

    public void go() {
        this.fred = new Thread(this);
        this.fred.start();
    }

    private static class DNSHeader {
        public int id;

        public int flags;

        public int qdcount;

        public int ancount;

        public int nscount;

        public int arcount;

        protected List<DNSQuestion> questions = new LinkedList();

        public DNSHeader(byte[] arrby) throws IOException {
            
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(arrby));
            this.id = dataInputStream.readUnsignedShort();
            this.flags = dataInputStream.readUnsignedShort();
            this.qdcount = dataInputStream.readUnsignedShort();
            this.ancount = dataInputStream.readUnsignedShort();
            this.nscount = dataInputStream.readUnsignedShort();
            this.arcount = dataInputStream.readUnsignedShort();
            for (int i = 0; i < this.qdcount; i++) {
                DNSQuestion dNSQuestion = new DNSQuestion(dataInputStream);
                this.questions.add(dNSQuestion);
            }
        }

        public List<DNSQuestion> getQuestions() {
            return this.questions;
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("DNS Header\n");
            stringBuffer.append("ID:      " + Integer.toHexString(this.id) + "\n");
            stringBuffer.append("Flags:   " + Integer.toBinaryString(this.flags) + "\n");
            stringBuffer.append("QdCount: " + this.qdcount + "\n");
            stringBuffer.append("AnCount: " + this.ancount + "\n");
            stringBuffer.append("NsCount: " + this.nscount + "\n");
            stringBuffer.append("ArCount: " + this.arcount + "\n");
            for (DNSQuestion dNSQuestion : this.questions) {
                stringBuffer.append(dNSQuestion);
            }
            return stringBuffer.toString();
        }
    }

    private static class DNSQuestion {
        public String qname;

        public int qtype;

        public int qclass;

        public int size = 0;

        public DNSAnswer answer = null;

        public DNSQuestion(DataInputStream dataInputStream) throws IOException {
            StringBuffer stringBuffer = new StringBuffer();
            int bytes = dataInputStream.readUnsignedByte();
            this.size++;
            while (bytes > 0) {
                for (int i = 0; i < bytes; i++) {
                    stringBuffer.append((char) dataInputStream.readUnsignedByte());
                    this.size++;
                }
                stringBuffer.append(".");
                bytes = dataInputStream.readUnsignedByte();
                this.size++;
            }
            this.qname = stringBuffer.toString();
            this.qtype = dataInputStream.readUnsignedShort();
            this.qclass = dataInputStream.readUnsignedShort();
            this.size += 4;
        }

        public DNSAnswer getAnswer() {
            return this.answer;
        }

        public void setAnswer(DNSAnswer dNSAnswer) {
            this.answer = dNSAnswer;
        }

        public int getType() {
            return this.qtype;
        }

        public int getSize() {
            return this.size;
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("\tQuestion: '" + this.qname + "' size: " + this.size + " bytes\n");
            stringBuffer.append("\tQType:    " + Integer.toHexString(this.qtype) + "\n");
            stringBuffer.append("\tQClass:   " + Integer.toHexString(this.qclass) + "\n\n");
            return stringBuffer.toString();
        }
    }

    private class DNSAnswer {
        protected ByteArrayOutputStream raw = new ByteArrayOutputStream(512);

        protected int type;

        public byte[] getAnswer() {
            return this.raw.toByteArray();
        }

        public int getType() {
            return this.type;
        }

        public DNSAnswer(DNSQuestion dNSQuestion) throws IOException {
            DataOutputStream dataOutputStream = new DataOutputStream(this.raw);
            String[] arrstring = dNSQuestion.qname.split("\\.");
            for (int i = 0; i < arrstring.length; i++) {
                dataOutputStream.writeByte(arrstring[i].length());
                for (int j = 0; j < arrstring[i].length(); j++) {
                    dataOutputStream.writeByte(arrstring[i].charAt(j));
                }
            }
            dataOutputStream.writeByte(0);
            if (listener != null) {
                int n;
                Response response = listener.respond(dNSQuestion.qname, dNSQuestion.getType());
                if (response == null) {
                    System.err.println("Response for question is null\n" + dNSQuestion);
                    response = A(0L);
                }
                dataOutputStream.writeShort(response.type);
                dataOutputStream.writeShort(DNS_TYPE_A);
                dataOutputStream.writeInt(ttl);
                this.type = response.type;
                switch (response.type) {
                    case DNS_TYPE_A:
                        dataOutputStream.writeShort(4);
                        dataOutputStream.writeInt((int) response.addr4);
                        break;
                    case DNS_TYPE_AAAA:
                        dataOutputStream.writeShort(DNS_TYPE_TXT);
                        for (n = 0; n < DNS_TYPE_TXT; n++) {
                            if (n < response.data.length) {
                                dataOutputStream.writeByte(response.data[n]);
                            } else {
                                dataOutputStream.writeByte(0);
                            }
                        }
                        break;
                    case DNS_TYPE_TXT:
                        dataOutputStream.writeShort(response.data.length + DNS_TYPE_A);
                        dataOutputStream.writeByte(response.data.length);
                        for (n = 0; n < response.data.length; n++) {
                            dataOutputStream.writeByte(response.data[n]);
                        }
                        break;
                    default:
                }
            }
            dataOutputStream.close();
        }
    }

    public interface Handler {
        Response respond(String string, int n);
    }

    public static final class Response {
        public int type = 0;

        public long addr4;

        public long[] addr6;

        public byte[] data;
    }
}
