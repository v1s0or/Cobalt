package common;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

public class Requirements {

    public static void recommended() {
    }

    public static Set arguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List list = runtimeMXBean.getInputArguments();
        return new HashSet(list);
    }

    public static String requirements(boolean bl) {
        if ("1.6".equals(System.getProperty("java.specification.version"))) {
            return "Java 1.6 is not supported. Please upgrade to Java 1.7 or later.";
        }
        if (bl && "1.8".equals(System.getProperty("java.specification.version")) && CommonUtils.isin("OpenJDK", System.getProperty("java.runtime.name"))) {
            return "OpenJDK 1.8 is not supported. Use Oracle Java 8 or OpenJDK 11 (or later.)";
        }
        Set set = arguments();
        if (!set.contains("-XX:+AggressiveHeap")) {
            return "Java -XX:+AggressiveHeap option not set. Use the Cobalt Strike launcher. Don't click the .jar file!";
        }
        if (!set.contains("-XX:+UseParallelGC")) {
            return "Java -XX:+UseParallelGC option not set. Use the Cobalt Strike launcher. Don't click the .jar file!";
        }
        return null;
    }

    public static void checkGUI() {
        recommended();
        String str1 = requirements(true);
        if (str1 != null) {
            JOptionPane.showMessageDialog(null, str1, null, 0);
            CommonUtils.print_error(str1);
            System.exit(0);
        }
        String str2 = System.getenv("XDG_SESSION_TYPE");
        if ("wayland".equals(str2)) {
            CommonUtils.print_warn("You are using a Wayland desktop and not X11. Graphical Java applications run on Wayland are known to crash. You should use X11. See: https://www.cobaltstrike.com/help-wayland");
            JOptionPane.showInputDialog(null, "The Wayland desktop is not supported with Cobalt Strike.\nMore information:", null, 2, null, null, "https://www.cobaltstrike.com/help-wayland");
        }
    }

    public static void checkConsole() {
        recommended();
        String str = requirements(false);
        if (str != null) {
            CommonUtils.print_error(str);
            System.exit(0);
        }
    }
}
