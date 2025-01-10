package report;

import common.CommonUtils;
import common.RegexParser;
import cortana.Cortana;
import dialog.DialogUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ReportBridge implements Function, Loadable, Environment {
    protected Map descriptions = new HashMap();

    protected LinkedHashMap reports = new LinkedHashMap();

    public List reportTitles() {
        return new LinkedList(this.reports.keySet());
    }

    public String describe(String string) {
        return (String) this.descriptions.get(string);
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.putenv(scriptInstance, "li", this);
        Cortana.putenv(scriptInstance, "nobreak", this);
        Cortana.putenv(scriptInstance, "output", this);
        Cortana.putenv(scriptInstance, "page", this);
        Cortana.putenv(scriptInstance, "report", this);
        Cortana.putenv(scriptInstance, "ul", this);
        Cortana.put(scriptInstance, "&block", this);
        Cortana.put(scriptInstance, "&bookmark", this);
        Cortana.put(scriptInstance, "&describe", this);
        Cortana.put(scriptInstance, "&formatTime", this);
        Cortana.put(scriptInstance, "&b", this);
        Cortana.put(scriptInstance, "&br", this);
        Cortana.put(scriptInstance, "&color", this);
        Cortana.put(scriptInstance, "&color2", this);
        Cortana.put(scriptInstance, "&h1", this);
        Cortana.put(scriptInstance, "&h2", this);
        Cortana.put(scriptInstance, "&h2_img", this);
        Cortana.put(scriptInstance, "&h3", this);
        Cortana.put(scriptInstance, "&h4", this);
        Cortana.put(scriptInstance, "&host_image", this);
        Cortana.put(scriptInstance, "&img", this);
        Cortana.put(scriptInstance, "&landscape", this);
        Cortana.put(scriptInstance, "&layout", this);
        Cortana.put(scriptInstance, "&li", this);
        Cortana.put(scriptInstance, "&link", this);
        Cortana.put(scriptInstance, "&kvtable", this);
        Cortana.put(scriptInstance, "&nobreak", this);
        Cortana.put(scriptInstance, "&output", this);
        Cortana.put(scriptInstance, "&p", this);
        Cortana.put(scriptInstance, "&p_formatted", this);
        Cortana.put(scriptInstance, "&text", this);
        Cortana.put(scriptInstance, "&table", this);
        Cortana.put(scriptInstance, "&ts", this);
        Cortana.put(scriptInstance, "&ul", this);
        Cortana.put(scriptInstance, "&list_unordered", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Document buildReport(String string1, String string2, Stack stack) {
        Document document = new Document(string2, 0);
        SleepClosure sleepClosure = (SleepClosure) this.reports.get(string1);
        sleepClosure.getOwner().getMetadata().put("document", document);
        SleepUtils.runCode(sleepClosure, string1, null, stack);
        sleepClosure.getOwner().getMetadata().put("document", null);
        sleepClosure.getOwner().getMetadata().put("document_stack", null);
        return document;
    }

    public Document getCurrentDocument(ScriptInstance scriptInstance) {
        Document document = (Document) scriptInstance.getMetadata().get("document");
        if (document == null) {
            throw new RuntimeException("this function must be run within the context of a report!");
        }
        return document;
    }

    public Stack getContentStack(ScriptInstance scriptInstance) {
        Stack stack = (Stack) scriptInstance.getMetadata().get("document_stack");
        if (stack == null) {
            stack = new Stack();
            scriptInstance.getMetadata().put("document_stack", stack);
        }
        return stack;
    }

    public Content getContent(ScriptInstance scriptInstance) {
        return (Content) getContentStack(scriptInstance).peek();
    }

    protected void eval(Content content, ScriptInstance scriptInstance, Block block) {
        getContentStack(scriptInstance).push(content);
        SleepUtils.runCode(scriptInstance, block);
        getContentStack(scriptInstance).pop();
    }

    protected void eval(Content content, SleepClosure sleepClosure) {
        getContentStack(sleepClosure.getOwner()).push(content);
        SleepUtils.runCode(sleepClosure, "", null, new Stack());
        getContentStack(sleepClosure.getOwner()).pop();
    }

    public void bindFunction(ScriptInstance scriptInstance, String string1, String string2, Block block) {
        if ("report".equals(string1)) {
            this.reports.put(string2, new SleepClosure(scriptInstance, block));
        } else if ("page".equals(string1)) {
            byte b = 0;
            if (string2.equals("rest")) {
                b = 1;
            } else if (string2.equals("first")) {
                b = 0;
            } else if (string2.equals("first-center")) {
                b = 2;
            } else if (string2.equals("single")) {
                b = 3;
            } else {
                throw new RuntimeException("invalid page type '" + string2 + "'");
            }
            eval(getCurrentDocument(scriptInstance).addPage(b), scriptInstance, block);
        }
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (!"sajkld".equals(string)) {
            if ("&bookmark".equals(string)) {
                if (stack.size() == 2) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    getCurrentDocument(scriptInstance).getBookmarks().bookmark(str1, str2);
                } else {
                    String str = BridgeUtilities.getString(stack, "");
                    getCurrentDocument(scriptInstance).getBookmarks().bookmark(str);
                }
            } else if ("&br".equals(string)) {
                getContent(scriptInstance).br();
            } else if ("&describe".equals(string)) {
                String str1 = BridgeUtilities.getString(stack, "");
                String str2 = BridgeUtilities.getString(stack, "");
                this.descriptions.put(str1, str2);
            } else {
                if ("&formatTime".equals(string)) {
                    long l = BridgeUtilities.getLong(stack);
                    return SleepUtils.getScalar(CommonUtils.formatTime(l));
                }
                if ("&h1".equals(string)) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, str1);
                    String str3 = BridgeUtilities.getString(stack, "left");
                    getContent(scriptInstance).h1(str1, str2, str3);
                } else if ("&h2".equals(string)) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, str1);
                    getContent(scriptInstance).h2(str1, str2);
                } else if ("&h2_img".equals(string)) {
                    BufferedImage bufferedImage =
                            (BufferedImage) BridgeUtilities.getObject(stack);
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, str1);
                    getContent(scriptInstance).h2_img(bufferedImage, str1, str2);
                } else if ("&h3".equals(string)) {
                    String str = BridgeUtilities.getString(stack, "");
                    getContent(scriptInstance).h3(str);
                } else if ("&h4".equals(string)) {
                    String str = BridgeUtilities.getString(stack, "");
                    getContent(scriptInstance).h4(str, "left");
                } else if ("&b".equals(string)) {
                    String str = BridgeUtilities.getString(stack, "");
                    getContent(scriptInstance).b(str);
                } else if ("&color".equals(string)) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    getContent(scriptInstance).color(str1, str2);
                } else if ("&color2".equals(string)) {
                    String str1 = BridgeUtilities.getString(stack, "");
                    String str2 = BridgeUtilities.getString(stack, "");
                    String str3 = BridgeUtilities.getString(stack, "");
                    getContent(scriptInstance).color2(str1, str2, str3);
                } else {
                    if ("&host_image".equals(string)) {
                        String str = BridgeUtilities.getString(stack, "");
                        double d = BridgeUtilities.getDouble(stack, 0.0D);
                        boolean bool = !SleepUtils.isEmptyScalar(
                                BridgeUtilities.getScalar(stack));
                        return SleepUtils.getScalar(
                                DialogUtils.TargetVisualizationMedium(str, d, bool, false));
                    }
                    if ("&img".equals(string)) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "");
                        getContent(scriptInstance).img(str1, str2);
                    } else if ("&kvtable".equals(string)) {
                        Scalar scalar = (Scalar) stack.pop();
                        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap();

                        Iterator i = scalar.getHash().getData().entrySet().iterator();
                        while (i.hasNext()) {
                            Map.Entry entry = (Map.Entry) i.next();
                            linkedHashMap.put(entry.getKey().toString(),
                                    entry.getValue() != null
                                            ? entry.getValue().toString() : "");
                        }

                        /*for (Map.Entry entry : scalar.getHash().getData().entrySet()) {
                            linkedHashMap.put(entry.getKey().toString(),
                                    entry.getValue() != null
                                            ? entry.getValue().toString() : "");
                        }*/
                        getContent(scriptInstance).kvtable(linkedHashMap);
                    } else if ("&landscape".equals(string)) {
                        getCurrentDocument(scriptInstance).setOrientation(1);
                    } else if ("&li".equals(string)) {
                        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack,
                                scriptInstance);
                        eval(getContent(scriptInstance).li(), sleepClosure);
                    } else if ("&nobreak".equals(string)) {
                        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack,
                                scriptInstance);
                        eval(getContent(scriptInstance).nobreak(), sleepClosure);
                    } else if ("&output".equals(string)) {
                        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack,
                                scriptInstance);
                        eval(getContent(scriptInstance).output("800"), sleepClosure);
                    } else if ("&block".equals(string)) {
                        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack,
                                scriptInstance);
                        String str = BridgeUtilities.getString(stack, "left");
                        eval(getContent(scriptInstance).block(str), sleepClosure);
                    } else if ("&p".equals(string)) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "left");
                        getContent(scriptInstance).p(str1, str2);
                    } else if ("&p_formatted".equals(string)) {
                        String str = BridgeUtilities.getString(stack, "");
                        str = CommonUtils.strrep(str, "\n\n*", "\n*");
                        List<String> list = CommonUtils.toList(str.split("\n"));
                        LinkedList<String> linkedList = new LinkedList();

                        Iterator iterator = list.iterator();
                        while (iterator.hasNext()) {
                            String str1 = (String) iterator.next();
                            str1 = str1.trim();
                            if (!str1.equals("") && str1.charAt(0) == '*' && str1.length() > 1) {
                                linkedList.add(str1.substring(1));
                                continue;
                            }
                            if (linkedList.size() > 0) {
                                getContent(scriptInstance).list_formatted(linkedList);
                                linkedList = new LinkedList();
                                if ("".equals(str1)) {
                                    continue;
                                }
                            }
                            RegexParser regexParser = new RegexParser(str1);
                            if (regexParser.matches("===(.*?)===")) {
                                getContent(scriptInstance).h4(regexParser.group(1), "left");
                                if (iterator.hasNext()) {
                                    str1 = (String) iterator.next();
                                    if (!"".equals(str1)) {
                                        getContent(scriptInstance).p(str1, "left");
                                    }
                                }
                                continue;
                            }
                            if ("".equals(str1)) {
                                getContent(scriptInstance).br();
                                continue;
                            }
                            RegexParser regexParser2 = new RegexParser(str1.trim());
                            if (regexParser2.matches("'''(.*?)'''(.*?)")) {
                                Content content = getContent(scriptInstance).block("left");
                                content.b(regexParser2.group(1));
                                content.text(regexParser2.group(2));
                                continue;
                            }
                            getContent(scriptInstance).p(str1, "left");
                        }

                        if (linkedList.size() > 0) {
                            getContent(scriptInstance).list(linkedList);
                        }
                    } else if ("&text".equals(string)) {
                        String str = BridgeUtilities.getString(stack, "");
                        getContent(scriptInstance).text(str);
                    } else if ("&table".equals(string) || "&layout".equals(string)) {
                        List list1 = SleepUtils.getListFromArray((Scalar) stack.pop());
                        List list2 = SleepUtils.getListFromArray((Scalar) stack.pop());
                        List<Map<String, Object>> list3 =
                                SleepUtils.getListFromArray((Scalar) stack.pop());
                        Iterator iterator = list3.iterator();
                        while (iterator.hasNext()) {
                            Map<String, Object> map = (Map) iterator.next();
                            for (Map.Entry entry : map.entrySet()) {
                                if (entry.getValue() instanceof SleepClosure) {
                                    SleepClosure sleepClosure =
                                            (SleepClosure) entry.getValue();
                                    Content content = getContent(scriptInstance).string();
                                    eval(content, sleepClosure);
                                    StringBuffer stringBuffer = new StringBuffer();
                                    content.publish(stringBuffer);
                                    entry.setValue(stringBuffer.toString());
                                    continue;
                                }
                                entry.setValue(Content.fixText(
                                        entry.getValue() != null
                                                ? entry.getValue().toString() : ""));
                            }
                        }
                        if ("&table".equals(string)) {
                            getContent(scriptInstance).table(list1, list2, list3);
                        } else {
                            getContent(scriptInstance).layout(list1, list2, list3);
                        }
                    } else if ("&ts".equals(string)) {
                        String str = BridgeUtilities.getString(stack, "");
                        getContent(scriptInstance).ts();
                    } else if ("&ul".equals(string)) {
                        SleepClosure sleepClosure = BridgeUtilities.getFunction(stack,
                                scriptInstance);
                        eval(getContent(scriptInstance).ul(), sleepClosure);
                    } else if ("&list_unordered".equals(string)) {
                        List list = SleepUtils.getListFromArray((Scalar) stack.pop());
                        getContent(scriptInstance).list(list);
                    } else if ("&link".equals(string)) {
                        String str1 = BridgeUtilities.getString(stack, "");
                        String str2 = BridgeUtilities.getString(stack, "");
                        getContent(scriptInstance).link_bullet(str1, str2);
                    }
                }
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
