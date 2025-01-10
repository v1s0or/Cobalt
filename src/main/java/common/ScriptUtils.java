package common;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.runtime.Scalar;
import sleep.runtime.ScalarArray;
import sleep.runtime.SleepUtils;

public class ScriptUtils {

    public static Scalar toSleepArray(Object[] arrobject) {
        return SleepUtils.getArrayWrapper(CommonUtils.toList(arrobject));
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

    public static Scalar convertAll(Object object) {
        if (object instanceof Collection) {
            Scalar scalar = SleepUtils.getArrayScalar();
            Iterator iterator = ((Collection) object).iterator();
            while (iterator.hasNext()) {
                scalar.getArray().push(convertAll(iterator.next()));
            }
            return scalar;
        }
        if (object instanceof Map) {
            Scalar scalar = SleepUtils.getHashScalar();
            Map<String, Object> m = (Map) object;
            for (Map.Entry entry : m.entrySet()) {
                Scalar scalar1 = SleepUtils.getScalar(entry.getKey() + "");
                Scalar scalar2 = scalar.getHash().getAt(scalar1);
                scalar2.setValue(convertAll(entry.getValue()));
            }
            return scalar;
        }
        if (object instanceof BeaconEntry) {
            return convertAll(((BeaconEntry) object).toMap());
        }
        if (object instanceof Scriptable) {
            Scriptable scriptable = (Scriptable) object;
            Scalar scalar = SleepUtils.getArrayScalar();
            scalar.getArray().push(SleepUtils.getScalar(scriptable.eventName()));
            Stack stack = scriptable.eventArguments();
            while (!stack.isEmpty()) {
                scalar.getArray().push((Scalar) stack.pop());
            }
            return scalar;
        }
        if (object instanceof ToScalar) {
            return ((ToScalar) object).toScalar();
        }
        if (object instanceof Object[]) {
            Object[] arrobject = (Object[]) object;
            LinkedList linkedList = new LinkedList();
            for (int b = 0; b < arrobject.length; b++) {
                linkedList.add(arrobject[b]);
            }
            return convertAll(linkedList);
        }
        return ObjectUtilities.BuildScalar(true, object);
    }

    public static String[] ArrayOrString(Stack stack) {
        if (stack.isEmpty()) {
            return new String[0];
        }
        Scalar scalar = (Scalar) stack.peek();
        if (scalar.getArray() != null) {
            return CommonUtils.toStringArray(BridgeUtilities.getArray(stack));
        }
        return new String[]{((Scalar) stack.pop()).stringValue()};
    }

    public static Scalar IndexOrMap(Map map, Stack stack) {
        if (stack.isEmpty()) {
            return SleepUtils.getHashWrapper(map);
        }
        String str = BridgeUtilities.getString(stack, "");
        return CommonUtils.convertAll(map.get(str));
    }

    public static Stack StringToArguments(String string) {
        Stack<Scalar> stack = new Stack();
        StringBuffer stringBuffer = new StringBuffer();
        for (int b = 0; b < string.length(); b++) {
            char c = string.charAt(b);
            if (c == ' ') {
                if (stringBuffer.length() > 0) {
                    stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
                }
                stringBuffer = new StringBuffer();
            } else if (c == '"' && stringBuffer.length() == 0) {
                while (++b < string.length() && string.charAt(b) != '"') {
                    stringBuffer.append(string.charAt(b));
                    b++;
                }
                stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
                stringBuffer = new StringBuffer();
            } else {
                stringBuffer.append(c);
            }
        }
        if (stringBuffer.length() > 0) {
            stack.add(0, SleepUtils.getScalar(stringBuffer.toString()));
        }
        stack.pop();
        return stack;
    }
}
