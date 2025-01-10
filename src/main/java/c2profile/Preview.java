package c2profile;

import beacon.BeaconPayload;
import cloudstrike.Response;
import common.CommonUtils;
import common.License;
import common.MudgeSanity;
import common.SleevedResource;
import common.WebTransforms;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pe.MalleablePE;
import pe.PEParser;

public class Preview implements Serializable {
    protected Profile c2profile;

    protected Map characteristics = null;

    protected List notes = new LinkedList();

    public Preview(Profile profile) {
        this.c2profile = profile;
    }

    public String getSampleName() {
        return this.c2profile.getString(".sample_name");
    }

    public void summarize(Map map) {
        map.put("c2sample.client", getClientSample());
        map.put("c2sample.server", getServerSample());
        map.put("c2sample.name", getSampleName());
        map.put("c2sample.strings", getStrings());
        map.put("c2sample.pe", getPE());
    }

    public void note(String string) {
        this.notes.add(string);
    }

    public Map getPE() {
        if (this.characteristics != null) {
            return this.characteristics;
        }
        byte[] arrby1 = SleevedResource.readResource("resources/beacon.dll");
        MalleablePE malleablePE = new MalleablePE(this.c2profile);
        byte[] arrby2 = malleablePE.pre_process(arrby1, "x86");
        PEParser pEParser = PEParser.load(arrby2);
        this.characteristics = new LinkedHashMap();
        this.characteristics.put("Checksum",
                Integer.valueOf(pEParser.get("CheckSum")));
        this.characteristics.put("Compilation Timestamp",
                pEParser.getDate("TimeDateStamp"));
        this.characteristics.put("Entry Point",
                Integer.valueOf(pEParser.get("AddressOfEntryPoint")));
        this.characteristics.put("Name",
                pEParser.getString("Export.Name").replaceAll("\\P{Print}", "."));
        this.characteristics.put("Size",
                Integer.valueOf(pEParser.get("SizeOfImage")));
        this.characteristics.put("Target Machine", "x86");
        if (License.isTrial()) {
            note("EICAR strings were observed within this payload and its traffic. This is a clever technique to detect and evade anti-virus products.");
        }

        if (this.c2profile.option(".stage.obfuscate")
                || this.c2profile.option(".stage.sleep_mask")) {
            this.characteristics.remove("Name");
            if (!this.c2profile.option(".stage.cleanup")) {
                note("The final payload DLL is obfuscated in memory.");
                note("The package that loads the payload DLL is less obfuscated.");
            } else {
                note("The payload DLL obfuscates itself in memory.");
            }
        } else if (this.c2profile.option(".stage.stomppe")) {
            note("The payload DLL clears its in-memory MZ, PE, and e_lfanew header values. This is a common obfuscation for memory injected DLLs.");
        }
        if (this.c2profile.option(".stage.userwx")) {
            if ("".equals(this.c2profile.getString(".stage.module_x86"))) {
                note("This payload resides in memory pages with RWX permissions. These memory pages are not backed by a file on disk.");
            } else {
                note("This payload resides in memory pages with RWX permissions.");
            }
        }
        if (!"".equals(this.c2profile.getString(".stage.module_x86"))) {
            note("This payload loads "
                    + this.c2profile.getString(".stage.module_x86")
                    + " and overwrites its location in memory. This hides the payload within memory backed by this legitimate file.");
        }
        if (this.notes.size() > 0) {
            this.characteristics.put("Notes", CommonUtils.join(this.notes, " "));
        }
        return this.characteristics;
    }

    public String getClientSample() {
        return getClientSample(".http-get");
    }

    public String getServerSample() {
        return getServerSample(".http-get");
    }

    public String getStrings() {
        return this.c2profile.getToStringLog(".stage");
    }

    public String getClientSample(String string) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] arrby = CommonUtils.randomData(16);
        String str1 = "";
        String str2 = "";
        if (string.equals(".http-stager")) {
            str1 = this.c2profile.getString(string + ".uri_x86");
            if ("".equals(str1)) {
                str1 = CommonUtils.MSFURI();
            }
            str2 = "GET";
        } else {
            str1 = CommonUtils.pick(this.c2profile.getString(string + ".uri").split(" "));
            str2 = this.c2profile.getString(string + ".verb");
        }
        if (string.equals(".http-post")) {
            arrby = CommonUtils.toBytes(CommonUtils.rand(99999) + "");
        }
        this.c2profile.apply(string + ".client", response, arrby);
        StringBuffer stringBuffer1 = new StringBuffer();

        Iterator iterator = response.params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = entry.getKey() + "";
            String value = entry.getValue() + "";
            try {
                entry.setValue(URLEncoder.encode(entry.getValue() + "", "UTF-8"));
            }
            catch (Exception exception) {
                MudgeSanity.logException("url encoding: " + entry, exception, false);
            }
            stringBuffer1.append(key + "=" + value);
            if (iterator.hasNext()) {
                stringBuffer1.append("&");
            }
        }
        if (stringBuffer1.length() > 0) {
            str1 = str1 + response.uri + "?" + stringBuffer1;
        } else {
            str1 = str1 + response.uri;
        }
        StringBuffer stringBuffer2 = new StringBuffer();
        stringBuffer2.append(str2 + " " + str1 + " HTTP/1.1\n");
        if (!response.header.containsKey("User-Agent")) {
            response.header.put("User-Agent", BeaconPayload.randua(this.c2profile));
        }
        for (Map.Entry entry : response.header.entrySet()) {
            String str3 = entry.getKey() + "";
            String str4 = entry.getValue() + "";
            entry.setValue(str4.replaceAll("\\P{Graph}", ""));
            stringBuffer2.append(str3 + ": " + str4 + "\n");
        }
        if (response.data != null) {
            try {
                byte[] arrby1 = new byte[0];
                arrby1 = new byte[response.data.available()];
                response.data.read(arrby1, 0, arrby1.length);
                stringBuffer2.append("\n" + CommonUtils.bString(arrby1).replaceAll("\\P{Print}", "."));
                stringBuffer2.append("\n");
            } catch (Exception exception) {
                MudgeSanity.logException("sample generate", exception, false);
            }
        }
        stringBuffer2.append("\n");
        return stringBuffer2.toString();
    }

    public String getServerSample(String string) {
        try {
            Response response = new Response("200 OK", null, (InputStream) null);
            byte[] arrby1 = CommonUtils.randomData(64);
            if (".http-post".equals(string)) {
                arrby1 = new byte[0];
            }
            this.c2profile.apply(string + ".server", response, arrby1);
            (new WebTransforms(this.c2profile)).filterResponse(response);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("HTTP/1.1 " + response.status + "\n");
            for (Map.Entry entry : response.header.entrySet()) {
                String str1 = entry.getKey() + "";
                String str2 = entry.getValue() + "";
                entry.setValue(str2.replaceAll("\\P{Graph}", ""));
                stringBuffer.append(str1 + ": " + str2 + "\n");
            }
            byte[] arrby2 = new byte[0];
            if (response.data != null) {
                arrby2 = new byte[response.data.available()];
                response.data.read(arrby2, 0, arrby2.length);
            }
            stringBuffer.append("\n" + CommonUtils.bString(arrby2).replaceAll("\\P{Print}", "."));
            return stringBuffer.toString();
        } catch (IOException iOException) {
            MudgeSanity.logException("getServerSample: " + string, iOException, false);
            return "";
        }
    }
}
