package pe;

import common.AssertUtils;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

public class PEParser {
    protected DataInputStream content = null;
    protected byte[] bdata = new byte[8];
    protected ByteBuffer buffer = null;
    protected Map<String, Object> values = new HashMap();
    protected byte[] original;
    protected Stack frames = new Stack();
    protected Map<String, Object> locations = new HashMap();
    protected LinkedList strings = new LinkedList();
    protected boolean procassembly = false;

    public boolean isProcessAssembly() {
        return this.procassembly;
    }

    protected void parseDirectory(int n) throws IOException {
        long l1 = readLong();
        long l2 = readLong();
        put("DataDirectory." + n + ".VirtualAddress", l1);
        put("DataDirectory." + n + ".Size", l2);
    }

    protected void parseFunctionNameHint(int n1, int n2, int n3, List list) throws IOException {
        jump(n3 - n2 + n1);
        int i = readShort();
        String str = readString();
        list.add(str + "@" + i);
        complete();
    }

    protected List parseFunctionNameList(int n1, int n2, int n3) throws IOException {
        LinkedList<String> linkedList = new LinkedList<String>();
        jump(n1 - n2 + n3);
        while (true) {
            if (is64()) {
                long l = readQWord();
                if ((l & Long.MIN_VALUE) == Long.MIN_VALUE) {
                    l &= Long.MAX_VALUE;
                    // linkedList.add("<ordinal>@" + l);
                    linkedList.add("<ordinal>@" + (l &= Long.MAX_VALUE));
                    continue;
                }
                if (l <= 0L) {
                    break;
                }
                parseFunctionNameHint(n3, n2, (int) l, linkedList);
                continue;
            }
            int i = readInt();
            if ((i & Integer.MIN_VALUE) == Integer.MIN_VALUE) {
                i &= Integer.MAX_VALUE;
                linkedList.add("<ordinal>@" + i);
                continue;
            }
            if (i <= 0) {
                break;
            }
            parseFunctionNameHint(n3, n2, i, linkedList);
        }
        complete();
        return linkedList;
    }

    protected boolean parseImport(int n) throws IOException {
        int i = dirEntry(1);
        int j = get("DataDirectory.1.VirtualAddress");
        long l1 = readInt();
        consume(8);
        long l2 = readInt();
        long l3 = readInt();
        if (l1 == 0L && l2 == 0L && l3 == 0L) {
            return false;
        }
        put("Import." + n + ".RVAFunctionNameList", l1);
        put("Import." + n + ".RVAFunctionNameList.X", parseFunctionNameList((int) l1, j, i));
        put("Import." + n + ".RVAModuleName", l2);
        put("Import." + n + ".RVAModuleName.X", getStringFromPointer((int) l2, j, i));
        put("Import." + n + ".RVAFunctionAddressList", l3);
        return true;
    }

    public int getPointerForLocation(int n1, int n2) {
        int i = dirEntry(n1);
        int j = get("DataDirectory." + n1 + ".VirtualAddress");
        return n2 - i + j;
    }

    protected String getStringFromPointer(int n1, int n2, int n3) throws IOException {
        jump((n1 - n2 + n3));
        String str = readString();
        complete();
        return str;
    }

    public List getExportedFunctions() {
        return (List) this.values.get("Export.FunctionNames");
    }

    public byte[] carveExportedFunction(String string) {
        int i = getFunctionOffset(string);
        int j = getNextFunctionOffset(string);
        if (i == -1) {
            CommonUtils.print_error("Could not find '" + string + "' in DLL");
            return new byte[0];
        }
        return Arrays.copyOfRange(this.original, i, j);
    }

    public int getFunctionOffset(String string) {
        List list1 = getExportedFunctions();
        List list2 = (List) this.values.get("Export.FunctionAddressesFixed");
        Iterator iterator1 = list1.iterator();
        Iterator iterator2 = list2.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            String str = (String) iterator1.next();
            Long l = (Long) iterator2.next();
            if (string.equals(str)) {
                return (int) l.longValue();
            }
        }
        return -1;
    }

    public int getNextFunctionOffset(String string) {
        long l = getFunctionOffset(string);
        List list1 = getExportedFunctions();
        List list2 = (List) this.values.get("Export.FunctionAddressesFixed");
        Iterator iterator1 = list1.iterator();
        Iterator iterator2 = list2.iterator();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            Long l2 = (Long) iterator2.next();
            if (l2 > l) {
                return (int) l2.longValue();
            }
        }
        int i = get(".text.PointerToRawData");
        int j = get(".text.SizeOfRawData");
        return i + j;
    }

    protected void parseExport() throws IOException {
        int n = dirEntry(0);
        int k = get("DataDirectory.0.VirtualAddress");
        consume(12);
        report("Export.Name");
        put("Export.Name", getStringFromPointer(readInt(), k, n));
        put("Export.Base", readInt());
        put("Export.NumberOfFunctions", readInt());
        put("Export.NumberOfNames", readInt());
        put("Export.AddressOfFunctions", readInt());
        put("Export.AddressOfNames", readInt());
        put("Export.AddressOfNameOridinals", readInt());
        jump(get("Export.AddressOfNames") - k + n);
        jump(readInt() - k + n);
        LinkedList linkedList = new LinkedList();
        for (int i = 0; i < get("Export.NumberOfNames"); i++) {
            report("Export.AddressOfName." + i);
            linkedList.add(readString());
        }
        put("Export.FunctionNames", linkedList);
        complete();
        complete();
        jump(get("Export.AddressOfFunctions") - k + n);
        linkedList = new LinkedList();
        for (int i = 0; i < get("Export.NumberOfNames"); i++) {
            linkedList.add(readLong());
        }
        put("Export.FunctionAddresses", linkedList);
        complete();
        jump((get("Export.AddressOfFunctions") - k + n));
        linkedList = new LinkedList();
        for (int i = 0; i < get("Export.NumberOfNames"); i++) {
            linkedList.add(new Long(fixAddress(readLong())));
        }
        put("Export.FunctionAddressesFixed", linkedList);
        complete();
    }

    public long fixAddress(long l) {
        Iterator iterator = SectionsTable().iterator();
        while (iterator.hasNext()) {
            String str = iterator.next() + "";
            if (inSection(str, l)) {
                return l - sectionAddress(str) + sectionStart(str);
            }
        }
        return -1L;
    }

    public static PEParser load(InputStream inputStream) {
        return new PEParser(inputStream);
    }

    public static PEParser load(byte[] arrby) {
        return new PEParser(arrby);
    }

    protected PEParser(InputStream inputStream) {
        this(CommonUtils.readAll(inputStream));
    }

    protected void jump(long l) throws IOException {
        this.frames.push(this.content);
        this.content = new DataInputStream(new ByteArrayInputStream(this.original));
        if (l > 0L) {
            consume((int) l);
        }
    }

    protected void complete() throws IOException {
        this.content.close();
        this.content = (DataInputStream) this.frames.pop();
    }

    public long checksum() {
        PEImageChecksum pEImageChecksum = new PEImageChecksum(getLocation("CheckSum"));
        pEImageChecksum.update(this.original, 0, this.original.length);
        return pEImageChecksum.getValue();
    }

    protected PEParser(byte[] arrby) {
        this.original = arrby;
        this.buffer = ByteBuffer.wrap(this.bdata);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.content = new DataInputStream(new ByteArrayInputStream(arrby));
        try {
            parse();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    protected void consume(int n) throws IOException {
        this.content.skipBytes(n);
    }

    protected int readInt() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return (int) this.buffer.getLong(0);
    }

    protected long readLong() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 4);
        return this.buffer.getLong(0);
    }

    protected long readQWord() throws IOException {
        this.buffer.clear();
        this.content.read(this.bdata, 0, 8);
        return this.buffer.getLong(0);
    }

    protected char readChar() throws IOException {
        return (char) this.content.readByte();
    }

    protected char readChar(DataInputStream dataInputStream) throws IOException {
        return (char) dataInputStream.readByte();
    }

    protected int readShort() throws IOException {
        this.content.read(this.bdata, 0, 2);
        return this.buffer.getShort(0) & 0xFFFF;
    }

    protected String readString() throws IOException {
        string();
        StringBuffer stringBuffer = new StringBuffer();
        while (true) {
            char c = readChar();
            if (c > Character.MIN_VALUE) {
                stringBuffer.append(c);
                continue;
            }
            break;
        }
        if (stringBuffer.toString().startsWith("_ReflectiveLoader") || stringBuffer.toString().startsWith("ReflectiveLoader")) {
            this.strings.removeLast();
        }
        if (stringBuffer.toString().contains("CorExeMain")) {
            this.procassembly = true;
        }
        return stringBuffer.toString();
    }

    protected String readString(int n) throws IOException {
        string();
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b = 0; b < n; b++) {
            char c = readChar();
            if (c > '\000') {
                stringBuffer.append(c);
            }
        }
        if (stringBuffer.toString().startsWith("_ReflectiveLoader") || stringBuffer.toString().startsWith("ReflectiveLoader")) {
            this.strings.removeLast();
        }
        if (stringBuffer.toString().contains("CorExeMain")) {
            this.procassembly = true;
        }
        return stringBuffer.toString();
    }

    protected void put(String string, long l) {
        this.values.put(string, new Long(l));
    }

    protected void put(String string, List list) {
        this.values.put(string, list);
    }

    protected void put(String string1, String string2) {
        this.values.put(string1, string2);
    }

    protected void put(String string, Date paramDate) {
        this.values.put(string, paramDate);
    }

    protected void error(String string) {
        throw new RuntimeException(string);
    }

    protected void header(String string, int n) throws Exception {
        report("header." + string);
        int i = readShort();
        if (i != n) {
            error("Header " + string + " Magic Failed: " + i + " expected (" + n + ")");
        }
    }

    public int get(String string) {
        Long l = (Long) this.values.get(string);
        if (l == null) {
            return 0;
        }
        return (int) l.longValue();
    }

    public Date getDate(String string) {
        return (Date) this.values.get(string);
    }

    public String getString(String string) {
        return (String) this.values.get(string);
    }

    public String getDLLName() throws IOException {
        return getString("Export.Name");
    }

    protected void put(String string1, String string2, long l) {
        this.values.put(string1 + "." + string2, new Long(l));
    }

    protected void put(String string1, String string2, String string3) {
        this.values.put(string1 + "." + string2, new Long(string3));
    }

    protected void readCharacteristics(String string) throws IOException {
        long l = readLong();
        LinkedList<String> linkedList = new LinkedList<String>();
        if ((l & 0x20L) == 32L) {
            linkedList.add("Code");
        }
        if ((l & 0x40L) == 64L) {
            linkedList.add("Initialized Data");
        }
        if ((l & 0x80L) == 128L) {
            linkedList.add("Uninitialized Data");
        }
        if ((l & 0x4000000L) == 0x4000000L) {
            linkedList.add("Section cannot be cached");
        }
        if ((l & 0x8000000L) == 0x8000000L) {
            linkedList.add("Section is not pageable");
        }
        if ((l & 0x10000000L) == 0x10000000L) {
            linkedList.add("Section is shared");
        }
        if ((l & 0x20000000L) == 0x20000000L) {
            linkedList.add("Executable");
        }
        if ((l & 0x40000000L) == 0x40000000L) {
            linkedList.add("Readable");
        }
        if ((l & 0x80000000L) == 0x80000000L) {
            linkedList.add("Writable");
        }
        linkedList.add("0x" + Long.toString(l, 16));
        this.values.put(string + ".Characteristics", linkedList);
    }

    protected Date readDate() throws IOException {
        return new Date(readLong() * 1000L);
    }

    public boolean hasSection(String string) {
        HashSet hashSet = new HashSet(SectionsTable());
        return hashSet.contains(string);
    }

    protected void parseSection(int n) throws Exception {
        report("Sections.AddressOfName." + n);
        String str = readString(8);
        append("SectionsTable", str);
        put(str, "VirtualSize", readInt());
        put(str, "VirtualAddress", readInt());
        put(str, "SizeOfRawData", readInt());
        put(str, "PointerToRawData", readInt());
        put(str, "PointerToRelocations", readInt());
        put(str, "PointerToLinenumbers", readInt());
        report(str + ".NumberOfRelocations");
        put(str, "NumberOfRelocations", readShort());
        put(str, "NumberOfLinenumbers", readShort());
        readCharacteristics(str);
    }

    protected void append(String string1, String string2) {
        if (values.get(string1) == null) {
            values.put(string1, new LinkedList());
        }
        LinkedList linkedList = (LinkedList) values.get(string1);
        linkedList.add(string2);
    }

    public int sectionStart(String string) {
        return get(string + ".PointerToRawData");
    }

    public int sectionSize(String string) {
        return get(string + ".SizeOfRawData");
    }

    public int sectionAddress(String string) {
        return get(string + ".VirtualAddress");
    }

    public int sectionEnd(String string) {
        return get(string + ".VirtualAddress") + get(string + ".VirtualSize");
    }

    protected boolean inSection(String string, long l) {
        long l1 = sectionAddress(string);
        long l2 = get(string + ".VirtualSize");
        return l >= l1 && l < l1 + l2;
    }

    public List<String> SectionsTable() {
        return (List) this.values.get("SectionsTable");
    }

    protected int dirEntry(int n) {
        int i = get("DataDirectory." + n + ".VirtualAddress");
        for (String str : SectionsTable()) {
            int j = sectionAddress(str);
            int k = sectionSize(str);
            if (i >= j && i < j + k) {
                return sectionStart(str) + i - j;
            }
        }
        throw new RuntimeException("Directory entry: " + n + "@" + i + " not found");
    }

    public boolean is64() {
        return (get("Machine") == 34404);
    }

    protected void parse64() throws Exception {
        header("Optional", 523);
        consume(14);
        report("AddressOfEntryPoint");
        put("AddressOfEntryPoint", readInt());
        consume(4);
        put("ImageBase", readQWord());
        report("SectionAlignment");
        put("SectionAlignment", readInt());
        put("FileAlignment", readInt());
        consume(8);
        put("MajorSubSystemVersion", readShort());
        consume(6);
        report("SizeOfImage");
        put("SizeOfImage", readInt());
        put("SizeOfHeaders", readInt());
        report("CheckSum");
        put("CheckSum", readInt());
        put("Subsystem", readShort());
        put("DllCharacteristics", readShort());
        consume(32);
        report("LoaderFlags");
        put("LoaderFlags", readInt());
        put("NumberOfRvaAndSizes", readInt());
    }

    protected void parse32() throws Exception {
        header("Optional", 267);
        consume(14);
        report("AddressOfEntryPoint");
        put("AddressOfEntryPoint", readInt());
        consume(8);
        put("ImageBase", readInt());
        report("SectionAlignment");
        put("SectionAlignment", readInt());
        put("FileAlignment", readInt());
        consume(8);
        put("MajorSubSystemVersion", readShort());
        consume(6);
        report("SizeOfImage");
        put("SizeOfImage", readInt());
        put("SizeOfHeaders", readInt());
        report("CheckSum");
        put("CheckSum", readInt());
        put("Subsystem", readShort());
        put("DllCharacteristics", readShort());
        consume(16);
        report("LoaderFlags");
        put("LoaderFlags", readInt());
        put("NumberOfRvaAndSizes", readInt());
    }

    public int here() throws IOException {
        return this.original.length - this.content.available();
    }

    public void string() throws IOException {
        try {
            int n = here();
            this.strings.add(new Integer(n));
        } catch (Exception exception) {
            MudgeSanity.logException("string", exception, false);
        }
    }

    public void report(String string) {
        try {
            int n = here();
            this.locations.put(string, new Integer(n));
        } catch (Exception exception) {
            MudgeSanity.logException("report: " + string, exception, false);
        }
    }

    public Iterator stringIterator() {
        return strings.iterator();
    }

    public int getLocation(String string) {
        if (!locations.containsKey(string)) {
            throw new IllegalArgumentException("No location for '" + string + "'");
        }
        int n = (Integer) locations.get(string);
        AssertUtils.Test(n >= 60, string + " (offset: " + n + ") Reflective Loader bootstrap region");
        return n;
    }

    public int getRichHeaderSize() {
        return get("e_lfanew") - 128;
    }

    public byte[] getRichHeader() {
        if (getRichHeaderSize() <= 0) {
            return new byte[0];
        }
        return Arrays.copyOfRange(original, 128, get("e_lfanew"));
    }

    protected void parse() throws Exception {
        header("e_magic", 23117);
        consume(58);
        report("e_lfanew");
        put("e_lfanew", readInt());
        jump(get("e_lfanew"));
        header("PE", 17744);
        consume(2);
        put("Machine", readShort());
        put("Sections", readShort());
        report("TimeDateStamp");
        put("TimeDateStamp", readDate());
        put("PointerToSymbolTable", readInt());
        report("NumberOfSymbols");
        put("NumberOfSymbols", readInt());
        put("SizeOfOptionalHeader", readShort());
        report("Characteristics");
        put("Characteristics", readShort());
        if (is64()) {
            parse64();
        } else {
            parse32();
        }
        for (int i = 0; i < get("NumberOfRvaAndSizes"); i++) {
            parseDirectory(i);
        }
        for (int i = 0; i < get("Sections"); i++) {
            parseSection(i);
        }
        report("HeaderSlack");
        complete();
        if (get("DataDirectory.1.VirtualAddress") != 0) {
            jump(dirEntry(1));
            int i = 0;
            while (parseImport(i)) {
                i++;
            }
            complete();
        }
        if (get("DataDirectory.0.VirtualAddress") != 0) {
            jump(dirEntry(0));
            parseExport();
            complete();
        }
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Key                                Value\n");
        stringBuffer.append("---                                -----\n");
        for (Map.Entry entry : new TreeMap<String, Object>(this.values).entrySet()) {
            String str = (String) entry.getKey();
            while (str.length() < 35) {
                str = str + " ";
            }
            stringBuffer.append(str);
            if (entry.getValue() instanceof Long) {
                Long l1 = (Long) entry.getValue();
                long l2 = l1;
                String str1 = "0x" + Long.toString(l2, 16);
                while (str1.length() < 12) {
                    str1 = str1 + " ";
                }
                str1 = str1 + l2;
                stringBuffer.append(str1);
                stringBuffer.append("\n");
                continue;
            }
            if (entry.getValue() instanceof String) {
                stringBuffer.append(entry.getValue() + "\n");
                continue;
            }
            if (entry.getValue() instanceof List) {
                stringBuffer.append(entry.getValue() + "\n");
                continue;
            }
            if (entry.getValue() instanceof Date) {
                long l = ((Date) entry.getValue()).getTime() / 1000L;
                String str1;
                str1 = "0x" + Long.toString(l, 16);
                while (str1.length() < 12) {
                    str1 = str1 + " ";
                }
                str1 = str1 + l;
                while (str1.length() < 32) {
                    str1 = str1 + " ";
                }
                str1 = str1 + CommonUtils.formatDateAny("dd MMM yyyy HH:mm:ss", l * 1000L);
                stringBuffer.append(str1);
                stringBuffer.append("\n");
            }
        }
        return stringBuffer.toString();
    }

    public String getStringAt(int n) {
        StringBuffer stringBuffer = new StringBuffer();
        while (this.original[n] != 0) {
            stringBuffer.append((char) this.original[n]);
            n++;
        }
        return stringBuffer.toString();
    }

    public void stringWalk() {
        Iterator iterator = stringIterator();
        while (iterator.hasNext()) {
            int n = (Integer) iterator.next();
            CommonUtils.print_stat("[" + n + "] " + getStringAt(n));
        }
    }

    public static void dump(String[] arrstring) throws Exception {
        File file = new File(arrstring[1]);
        PEParser pEParser = load(new FileInputStream(file));
        System.out.println(pEParser.toString());
        System.out.println("Checksum: " + pEParser.checksum());
        System.out.println("\n\nLocations:\n----------");
        for (Map.Entry entry : pEParser.locations.entrySet()) {
            String str;
            str = (String) entry.getKey();
            while (str.length() < 31) {
                str = str + " ";
            }
            System.out.println(str + " " + entry.getValue());
        }
    }

    public static void stage(String[] arrstring) throws Exception {
        PEClone pEClone = new PEClone();
        pEClone.start(arrstring[0]);
    }

    public static void main(String[] arrstring) throws Exception {
        if (arrstring.length == 0) {
            CommonUtils.print_info("Cobalt Strike PE Parser. Options:\n\t./peclone [file]\n\t\tDump PE headers as a Malleable PE stage block\n\t./peclone dump [file]\n\t\tRun Cobalt Strike's PE parser against the file");
        } else if (arrstring[0].equals("dump") && arrstring.length == 2) {
            dump(arrstring);
        } else {
            stage(arrstring);
        }
    }
}
