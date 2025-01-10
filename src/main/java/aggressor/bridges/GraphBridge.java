package aggressor.bridges;

import aggressor.TabManager;
import cortana.Cortana;
import graph.NetworkGraph;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.imageio.ImageIO;

import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class GraphBridge implements Function, Loadable {

    protected TabManager manager;

    protected Cortana engine;

    protected static Map imageCache = new HashMap();

    public GraphBridge(Cortana cortana, TabManager paramTabManager) {
        this.engine = cortana;
        this.manager = paramTabManager;
    }

    public void scriptLoaded(ScriptInstance scriptInstance) {
        Cortana.put(scriptInstance, "&graph", this);
        Cortana.put(scriptInstance, "&graph_start", this);
        Cortana.put(scriptInstance, "&graph_end", this);
        Cortana.put(scriptInstance, "&graph_add", this);
        Cortana.put(scriptInstance, "&graph_connect", this);
        Cortana.put(scriptInstance, "&image_overlay", this);
        Cortana.put(scriptInstance, "&graph_zoom", this);
        Cortana.put(scriptInstance, "&graph_zoom_reset", this);
        Cortana.put(scriptInstance, "&graph_layout", this);
    }

    public void scriptUnloaded(ScriptInstance scriptInstance) {
    }

    public Scalar evaluate(String string, ScriptInstance scriptInstance, Stack stack) {
        if (string.equals("&graph"))
            return SleepUtils.getScalar(new NetworkGraph());
        if (string.equals("&graph_start")) {
            NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
            networkGraph.start();
        } else if (string.equals("&graph_end")) {
            NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
            networkGraph.deleteNodes();
            networkGraph.end();
        } else if (string.equals("&graph_add")) {
            NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            Image image = (Image) BridgeUtilities.getObject(stack);
            String str4 = BridgeUtilities.getString(stack, "");
            networkGraph.addNode(str1, str2, str3, image, str4, "");
        } else if (string.equals("&graph_connect")) {
            NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
            String str1 = BridgeUtilities.getString(stack, "");
            String str2 = BridgeUtilities.getString(stack, "");
            String str3 = BridgeUtilities.getString(stack, "");
            String str4 = BridgeUtilities.getString(stack, "");
            String str5 = BridgeUtilities.getString(stack, "");
            String str6 = BridgeUtilities.getString(stack, "");
            networkGraph.addEdge(str1, str2, str3, str4, str5, str6, 1);
        } else {
            if (string.equals("&image_overlay")) {
                String str = BridgeUtilities.getString(stack, "");
                synchronized (imageCache) {
                    if (imageCache.containsKey(str))
                        return SleepUtils.getScalar(imageCache.get(str));
                }
                BufferedImage bufferedImage = new BufferedImage(1000, 776, 2);
                Graphics2D graphics2D = ((BufferedImage) bufferedImage).createGraphics();
                while (!stack.isEmpty()) {
                    try {
                        String str1 = BridgeUtilities.getString(stack, "");
                        FileInputStream fileInputStream = new FileInputStream(str1);
                        BufferedImage bufferedImage1 = ImageIO.read(new FileInputStream(str1));
                        fileInputStream.close();
                        graphics2D.drawImage(bufferedImage1, 0, 0, 1000, 776, null);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
                graphics2D.dispose();
                synchronized (imageCache) {
                    imageCache.put(str, bufferedImage);
                }
                return SleepUtils.getScalar(bufferedImage);
            }
            if (string.equals("&graph_zoom_reset")) {
                NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
                networkGraph.resetZoom();
            } else if (string.equals("&graph_zoom")) {
                NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
                double d = BridgeUtilities.getDouble(stack);
                networkGraph.zoom(d);
            } else if (string.equals("&graph_layout")) {
                NetworkGraph networkGraph = (NetworkGraph) BridgeUtilities.getObject(stack);
                String str = BridgeUtilities.getString(stack, "");
                networkGraph.setAutoLayout(str);
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}
