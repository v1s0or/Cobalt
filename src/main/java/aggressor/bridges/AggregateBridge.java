package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.Keys;
import common.PivotHint;
import common.RegexParser;
import cortana.Cortana;
import data.DataAggregate;
import dialog.DialogUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScalarHash;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class AggregateBridge implements Function, Loadable {

    protected AggressorClient client;

    public AggregateBridge(AggressorClient aggressorClient) {
        this.client = aggressorClient;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&aggregate", this);
        Cortana.put(scriptInstance, "&agConvert", this);
        Cortana.put(scriptInstance, "&agArchives", this);
        Iterator iterator = Keys.getDataModelIterator();
        while (iterator.hasNext()) {
            String str1 = (String) iterator.next();
            str1 = str1.substring(0, 1).toUpperCase() + str1.substring(1);
            final String keyMajor = str1.toLowerCase();
            Cortana.put(scriptInstance, "&ag" + str1, new Function() {

                @Override
                public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
                    Map<String, List> map = (Map) BridgeUtilities.getObject(stack);
                    return CommonUtils.convertAll((List) map.get(keyMajor));
                }
            });
        }
        Cortana.put(scriptInstance, "&agServicesForHost", this);
        Cortana.put(scriptInstance, "&agSessionsForHost", this);
        Cortana.put(scriptInstance, "&agCredentialsForHost", this);
        Cortana.put(scriptInstance, "&agWebHitsForEmail", this);
        Cortana.put(scriptInstance, "&agWebHitsForToken", this);
        Cortana.put(scriptInstance, "&agCountWebHitsByToken", this);
        Cortana.put(scriptInstance, "&agSentEmailsForCampaign", this);
        Cortana.put(scriptInstance, "&agSentEmailsForEmailAddress", this);
        Cortana.put(scriptInstance, "&agApplicationsForEmailAddress", this);
        Cortana.put(scriptInstance, "&agFileIndicatorsForSession", this);
        Cortana.put(scriptInstance, "&agFileIndicators", this);
        Cortana.put(scriptInstance, "&agOtherIndicatorsForSession", this);
        Cortana.put(scriptInstance, "&agTasksAndCheckinsForSession", this);
        Cortana.put(scriptInstance, "&agServices", this);
        Cortana.put(scriptInstance, "&agArchivesByTactic", this);
        Cortana.put(scriptInstance, "&agTacticsUsed", this);
        Cortana.put(scriptInstance, "&agTokenToEmail", this);
        Cortana.put(scriptInstance, "&agEmailAddresses", this);
        Cortana.put(scriptInstance, "&agCampaigns", this);
        Cortana.put(scriptInstance, "&agSentEmails", this);
        Cortana.put(scriptInstance, "&agIndicators", this);
        Cortana.put(scriptInstance, "&agInputs", this);
        Cortana.put(scriptInstance, "&agInputsForSession", this);
        Cortana.put(scriptInstance, "&agTasks", this);
        Cortana.put(scriptInstance, "&agWebHits", this);
        Cortana.put(scriptInstance, "&agWebHitsWithTokens", this);
        Cortana.put(scriptInstance, "&agSessionsById", this);
        Cortana.put(scriptInstance, "&agC2Domains", this);
        Cortana.put(scriptInstance, "&agC2ForSample", this);
        Cortana.put(scriptInstance, "&agPEForSample", this);
        Cortana.put(scriptInstance, "&agPENotesForSample", this);
        Cortana.put(scriptInstance, "&agCommunicationPathForSession", this);
        Cortana.put(scriptInstance, "&agC2Samples", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar extractValue(Map map, String string) {
        List list = (List) map.get(string);
        return CommonUtils.convertAll(list);
    }

    public static List filterList(List<Map> list, String string1, String string2) {
        LinkedList<Map> linkedList = new LinkedList();
        for (Map map : list) {
            if (map.containsKey(string1)) {
                String str = map.get(string1).toString();
                if (string2.equals(str)) {
                    linkedList.add(map);
                }
            }
        }
        return linkedList;
    }

    public static List filterListBySetMember(List<Map> list, String string1, String string2) {
        LinkedList<Map> linkedList = new LinkedList();
        for (Map map : list) {
            if (map.containsKey(string1)) {
                Set set = CommonUtils.toSet(map.get(string1).toString());
                if (set.contains(string2)) {
                    linkedList.add(map);
                }
            }
        }
        return linkedList;
    }

    public static List filterList(List<Map> list, String string, Set paramSet) {
        LinkedList<Map> linkedList = new LinkedList();
        for (Map map : list) {
            if (map.containsKey(string)) {
                String str = map.get(string).toString();
                if (paramSet.contains(str)) {
                    linkedList.add(map);
                }
            }
        }
        return linkedList;
    }

    public static List filterListNot(List<Map> list, String string1, String string2) {
        LinkedList<Map> linkedList = new LinkedList();
        for (Map map : list) {
            if (map.containsKey(string1)) {
                String str = map.get(string1).toString();
                if (!string2.equals(str)) {
                    linkedList.add(map);
                }
            }
        }
        return linkedList;
    }

    public static List getValuesWithout(List list, String string) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            HashMap hashMap = new HashMap((Map) iterator.next());
            hashMap.remove(string);
            linkedHashSet.add(hashMap);
        }
        return new LinkedList(linkedHashSet);
    }

    public static List getValue(List<Map> list, String string) {
        LinkedList linkedList = new LinkedList();
        for (Map map : list) {
            linkedList.add(map.get(string));
        }
        return linkedList;
    }

    public static List join(List list1, List list2, String string) {
        return join(list1, list2, string, string);
    }

    public static List join(List<Map> list1, List<Map> list2, String string1, String string2) {
        LinkedList linkedList = new LinkedList();
        HashMap<Object, Object> hashMap = new HashMap();
        for (Map map : list1) {
            String str = map.get(string1) + "";
            hashMap.put(str, map);
        }
        for (Map map : list2) {
            String str = map.get(string2) + "";
            HashMap hashMap1 = new HashMap();
            hashMap1.putAll(map);
            if (hashMap.containsKey(str)) {
                hashMap1.putAll((Map) hashMap.get(str));
            }
            linkedList.add(hashMap1);
        }
        return linkedList;
    }

    public static Map toMap(List<Map> list, String string) {
        HashMap<String, Map> hashMap = new HashMap();
        for (Map map : list) {
            String str = (String) map.get(string);
            hashMap.put(str, map);
        }
        return hashMap;
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if ("&aggregate".equals(string))
            return SleepUtils.getScalar(DataAggregate.AllModels(this.client));
        if ("&agArchives".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            return CommonUtils.convertAll((List) map.get("archives"));
        }
        if ("&agArchivesByTactic".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list = filterListBySetMember((List) map.get("archives"), "tactic", str);
            return CommonUtils.convertAll(list);
        }
        if ("&agTacticsUsed".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            HashSet<String> hashSet = new HashSet();
            List<String> list = getValue((List) map.get("archives"), "tactic");
            for (String str : list) {
                if (str == null || "".equals(str)) {
                    continue;
                }
                Set<String> set = CommonUtils.toSet(str);
                for (String str1 : set) {
                    if (str1.startsWith("T")) {
                        hashSet.add(str1);
                    }
                }
            }
            return CommonUtils.convertAll(hashSet);
        }
        if ("&agConvert".equals(string)) {
            Object object = BridgeUtilities.getObject(stack);
            return CommonUtils.convertAll(object);
        }
        if ("&agTokenToEmail".equals(string)) {
            Map<String, List<Map<String, String>>> map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List<Map<String, String>> list = (List) map.get("tokens");
            for (Map map1 : list) {
                if (str.equals(map1.get("token"))) {
                    return SleepUtils.getScalar((String) map1.get("email"));
                }
            }
            return SleepUtils.getEmptyScalar();
        }
        if ("&agCampaigns".equals(string)) {
            Map<String, List<Map<String, String>>> map = (Map) BridgeUtilities.getObject(stack);
            List<Map<String, String>> list = (List) map.get("archives");
            HashMap<String, Object> hashMap1 = new HashMap();
            HashMap hashMap2 = new HashMap();
            for (Map map1 : list) {
                if ("sendmail_start".equals(map1.get("type"))) {
                    String str = map1.get("cid").toString();
                    hashMap1.put(str, map1);
                    continue;
                }
                if ("sendmail_post".equals(map1.get("type"))) {
                    String str1 = DialogUtils.string(map1, "cid");
                    String str2 = DialogUtils.string(map1, "status");
                    if ("SUCCESS".equals(str2)) {
                        CommonUtils.increment(hashMap2, str1);
                    }
                }
            }
            Iterator iterator = hashMap1.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String str = (String) entry.getKey();
                if (CommonUtils.count(hashMap2, str) == 0) {

                    iterator.remove();
                }
            }
            /*for (Map.Entry entry : hashMap1.entrySet()) {
                String str = (String) entry.getKey();
                if (CommonUtils.count(hashMap2, str) == 0)
                    null.remove();
            }*/
            return CommonUtils.convertAll(hashMap1);
        }
        if ("&agC2Samples".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = (List) map.get("c2samples");
            return CommonUtils.convertAll(list);
        }
        if ("&agC2Domains".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List<String> list = getValue((List) map.get("c2info"), "domains");
            HashSet hashSet = new HashSet();
            for (String str : list) {
                hashSet.addAll(CommonUtils.toSet(str));
            }
            LinkedList linkedList = new LinkedList(hashSet);
            Collections.sort(linkedList);
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agPEForSample".equals(string)) {
            Map map1 = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(stack));
            Map<String, Object> map2 = (Map) map1.get("pe");
            // <String, String>
            LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("Checksum", map2.get("Checksum") + "");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            Date date = (Date) map2.get("Compilation Timestamp");
            linkedHashMap.put("Compilation Timestamp", simpleDateFormat.format(date));
            linkedHashMap.put("Entry Point", map2.get("Entry Point") + "");
            if (map2.containsKey("Name")) {
                linkedHashMap.put("Name", map2.get("Name"));
            }
            long l = CommonUtils.toLongNumber(map2.get("Size") + "", 0L);
            String str = CommonUtils.formatSize(l) + " (" + l + " bytes)";
            linkedHashMap.put("Size", str);
            linkedHashMap.put("Target Machine", map2.get("Target Machine"));
            Scalar scalar = SleepUtils.getOrderedHashScalar();
            for (Map.Entry entry : linkedHashMap.entrySet()) {
                Scalar scalar1 = SleepUtils.getScalar((String) entry.getKey());
                Scalar scalar2 = scalar.getHash().getAt(scalar1);
                scalar2.setValue(SleepUtils.getScalar(entry.getValue() + ""));
            }
            return scalar;
        }
        if ("&agPENotesForSample".equals(string)) {
            Map map1 = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(stack));
            Map map2 = (Map) map1.get("pe");
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            return map2.containsKey("Notes") ? SleepUtils.getScalar((String) map2.get("Notes")) : SleepUtils.getEmptyScalar();
        }
        if ("&agC2ForSample".equals(string)) {
            Map map = SleepUtils.getMapFromHash((ScalarHash) BridgeUtilities.getObject(stack));
            List<Map> list = getValuesWithout((List) map.get("callbacks"), "bid");
            LinkedList linkedList = new LinkedList();
            for (Map map1 : list) {
                Set<String> set = CommonUtils.toSet((String) map1.get("domains"));
                String str = DialogUtils.string(map1, "proto");
                for (String str1 : set) {
                    HashMap hashMap = new HashMap();
                    hashMap.put("Host", str1);
                    hashMap.put("Port", map1.get("port"));
                    hashMap.put("Protocols", str);
                    linkedList.add(hashMap);
                }
            }
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agCommunicationPathForSession".equals(string)) {
            Map map1 = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            Map map2 = toMap((List) map1.get("sessions"), "id");
            Map map3 = toMap((List) map1.get("c2info"), "bid");
            LinkedList linkedList = new LinkedList();
            Map map4 = (Map) map2.get(str);
            while (map4 != null) {
                if (!"".equals(map4.get("pbid"))) {
                    HashMap hashMap = new HashMap();
                    if ("beacon".equals(map4.get("session"))) {
                        PivotHint pivotHint = new PivotHint(map4.get("phint") + "");
                        hashMap.put("protocol", pivotHint.getProtocol());
                        hashMap.put("port", Integer.valueOf(pivotHint.getPort()));
                    } else {
                        hashMap.put("protocol", "SSH");
                        hashMap.put("port", map4.get("port"));
                    }
                    str = (String) map4.get("pbid");
                    map4 = (Map) map2.get(str);
                    if (map4 != null) {
                        map4.remove("port");
                        map4.remove("protocol");
                        hashMap.put("hosts", map4.get("computer"));
                        hashMap.putAll(map4);
                        linkedList.add(hashMap);
                    }
                    continue;
                }
                map4 = null;
                Map map = (Map) map3.get(str);
                if (map != null) {
                    String str1 = DialogUtils.string(map, "domains");
                    int i = DialogUtils.number(map, "port");
                    HashMap hashMap = new HashMap();
                    hashMap.put("protocol", DialogUtils.string(map, "proto"));
                    hashMap.put("port", Integer.valueOf(i));
                    hashMap.put("hosts", str1);
                    linkedList.add(hashMap);
                }
            }
            CommonUtils.print_info("\t" + linkedList);
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agSessionsById".equals(string)) {
            Map map1 = (Map) BridgeUtilities.getObject(stack);
            List list = (List) map1.get("sessions");
            Map map2 = toMap((List) map1.get("sessions"), "id");
            return CommonUtils.convertAll(map2);
        }
        if ("&agEmailAddresses".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            LinkedList linkedList = new LinkedList(new HashSet(getValue((List) map.get("tokens"), "email")));
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agServicesForHost".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list = filterList((List) map.get("services"), "address", str);
            return CommonUtils.convertAll(list);
        }
        if ("&agServices".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            LinkedList<Map> linkedList = new LinkedList((List) map.get("services"));
            HashSet hashSet = new HashSet(getValue((List) map.get("targets"), "address"));
            Iterator iterator = linkedList.iterator();
            while (iterator.hasNext()) {
                Map map1 = (Map) iterator.next();
                String str = (String) map1.get("address");
                if (!hashSet.contains(str)) {

                    iterator.remove();
                }
            }
            /*for (Map map1 : linkedList) {
                String str = (String) map1.get("address");
                if (!hashSet.contains(str)) {
                    null.remove();
                }
            }*/
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agSessionsForHost".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list = filterList((List) map.get("sessions"), "internal", str);
            return CommonUtils.convertAll(list);
        }
        if ("&agCredentialsForHost".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list = filterList((List) map.get("credentials"), "host", str);
            return CommonUtils.convertAll(list);
        }
        if ("&agWebHitsForToken".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "webhit");
            List list2 = filterList(list1, "token", str);
            return CommonUtils.convertAll(list2);
        }
        if ("&agSentEmailsForCampaign".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "sendmail_post");
            List list2 = filterList(list1, "cid", str);
            List list3 = (List) map.get("tokens");
            return CommonUtils.convertAll(join(list3, list2, "token"));
        }
        if ("&agSentEmailsForEmailAddress".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "sendmail_post");
            List list2 = join((List) map.get("tokens"), list1, "token");
            List list3 = filterList(list2, "email", str);
            return CommonUtils.convertAll(list3);
        }
        if ("&agApplicationsForEmailAddress".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = (List) map.get("applications");
            List list2 = join((List) map.get("tokens"), list1, "token", "id");
            List list3 = filterList(list2, "email", str);
            return CommonUtils.convertAll(list3);
        }
        if ("&agCountWebHitsByToken".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            HashMap<String, Integer> hashMap = new HashMap();
            List list1 = filterList((List) map.get("archives"), "type", "webhit");
            List<String> list2 = getValue((List) map.get("tokens"), "token");
            for (String str : list2) {
                int i = filterList(list1, "token", str).size();
                if (i > 0) {
                    hashMap.put(str, Integer.valueOf(i));
                }
            }
            return CommonUtils.convertAll(hashMap);
        }
        if ("&agWebHitsForEmail".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            LinkedList linkedList = new LinkedList();
            List list1 = filterList((List) map.get("archives"), "type", "webhit");
            List<String> list2 = getValue(filterList((List) map.get("tokens"), "email", str), "token");
            for (String str1 : list2) {
                linkedList.addAll(filterList(list1, "token", str1));
            }
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agSentEmails".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "sendmail_post");
            return CommonUtils.convertAll(list);
        }
        if ("&agIndicators".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "indicator");
            return CommonUtils.convertAll(list);
        }
        if ("&agFileIndicators".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List<Map> list = filterList((List) map.get("archives"), "type", "indicator");
            HashSet<String> hashSet = new HashSet();

            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                Map map1 = (Map) iterator.next();
                String str1 = (String) map1.get("data");
                RegexParser regexParser = new RegexParser(str1);
                if (regexParser.matches("file: (.*?) (.*?) bytes (.*)")) {
                    String str2 = regexParser.group(1);
                    String str3 = regexParser.group(2);
                    String str4 = regexParser.group(3);
                    if (hashSet.contains(str2)) {
                        iterator.remove();
                        continue;
                    }
                    map1.put("hash", str2);
                    map1.put("name", str4);
                    map1.put("size", str3);
                    hashSet.add(str2);
                    continue;
                }
                iterator.remove();
            }
            /*for (Map map1 : list) {
                String str1 = (String) map1.get("data");
                RegexParser regexParser = new RegexParser(str1);
                if (regexParser.matches("file: (.*?) (.*?) bytes (.*)")) {
                    String str2 = regexParser.group(1);
                    String str3 = regexParser.group(2);
                    String str4 = regexParser.group(3);
                    if (hashSet.contains(str2)) {
                        null.remove();
                        continue;
                    }
                    map1.put("hash", str2);
                    map1.put("name", str4);
                    map1.put("size", str3);
                    hashSet.add(str2);
                    continue;
                }
                null.remove();
            }*/
            return CommonUtils.convertAll(list);
        }
        if ("&agFileIndicatorsForSession".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "indicator");
            List list2 = filterList(list1, "bid", str);
            LinkedList linkedList = new LinkedList();
            Iterator iterator = list2.iterator();
            while (iterator.hasNext()) {
                HashMap hashMap = new HashMap((Map) iterator.next());
                String str1 = (String) hashMap.get("data");
                RegexParser regexParser = new RegexParser(str1);
                if (regexParser.matches("file: (.*?) (.*?) bytes (.*)")) {
                    String str2 = regexParser.group(1);
                    String str3 = regexParser.group(2);
                    String str4 = regexParser.group(3);
                    hashMap.put("hash", str2);
                    hashMap.put("name", str4);
                    hashMap.put("size", str3);
                    linkedList.add(hashMap);
                }
            }
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agOtherIndicatorsForSession".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "indicator");
            List list2 = filterList(list1, "bid", str);
            LinkedList linkedList = new LinkedList();
            Iterator iterator = list2.iterator();
            while (iterator.hasNext()) {
                HashMap hashMap = new HashMap((Map) iterator.next());
                String str1 = (String) hashMap.get("data");
                RegexParser regexParser = new RegexParser(str1);
                if (regexParser.matches("service: (.*?) (.*)")) {
                    String str2 = regexParser.group(1);
                    String str3 = regexParser.group(2);
                    hashMap.put("target", str2);
                    hashMap.put("name", str3);
                    hashMap.put("type", "service");
                    linkedList.add(hashMap);
                }
            }
            return CommonUtils.convertAll(linkedList);
        }
        if ("&agTasksAndCheckinsForSession".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            Set set = CommonUtils.toSet("task, checkin, output");
            List list1 = filterList((List) map.get("archives"), "type", set);
            List list2 = filterList(list1, "bid", str);
            return CommonUtils.convertAll(list2);
        }
        if ("&agInputs".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "input");
            return CommonUtils.convertAll(list);
        }
        if ("&agInputsForSession".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            String str = BridgeUtilities.getString(stack, "");
            List list1 = filterList((List) map.get("archives"), "type", "input");
            List list2 = filterList(list1, "bid", str);
            return CommonUtils.convertAll(list2);
        }
        if ("&agTasks".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "task");
            return CommonUtils.convertAll(list);
        }
        if ("&agWebHits".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "webhit");
            return CommonUtils.convertAll(list);
        }
        if ("&agWebHitsWithTokens".equals(string)) {
            Map map = (Map) BridgeUtilities.getObject(stack);
            List list = filterList((List) map.get("archives"), "type", "webhit");
            list = filterListNot(list, "token", "");
            return CommonUtils.convertAll(list);
        }
        return SleepUtils.getEmptyScalar();
    }
}
