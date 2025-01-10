package graph;

import aggressor.Prefs;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxGraphLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.mxGraphComponent.mxGraphControl;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import common.CommonUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

public class NetworkGraph extends JComponent implements ActionListener {

    protected mxGraph graph;
    protected mxGraphComponent component;
    protected Object parent;
    protected boolean isAlive = true;
    protected String layout = null;
    protected Map<String, Image> nodeImages = new HashMap();
    protected GraphPopup popup = null;
    protected double zoom = 1.0D;
    protected TouchMap nodes = new TouchMap();
    protected LinkedList<mxCell> edges = new LinkedList();
    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_INBOUND = 1;
    public static final int DIRECTION_OUTBOUND = 2;
    protected Map tooltips = new HashMap();

    public void actionPerformed(ActionEvent actionEvent) {
        this.isAlive = false;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public GraphPopup getGraphPopup() {
        return this.popup;
    }

    public void setGraphPopup(GraphPopup graphPopup) {
        this.popup = graphPopup;
    }

    public Image getScreenshot() {
        LinkedList<Object> linkedList = new LinkedList<Object>();
        for (Object v : this.nodes.values()) {
            linkedList.addAll(Arrays.asList(this.graph.getEdges(v)));
        }
        linkedList.addAll(this.nodes.values());
        return mxCellRenderer.createBufferedImage(this.graph, linkedList.toArray(), this.zoom, null, true, null, new NetworkGraphCanvas());
    }

    public void setTransferHandler(TransferHandler transferHandler) {
        this.component.setTransferHandler(transferHandler);
    }

    public void clearSelection() {
        this.graph.clearSelection();
    }

    public void selectAll() {
        this.graph.selectAll();
    }

    public NetworkGraph() {
        mxConstants.VERTEX_SELECTION_COLOR = Prefs.getPreferences().getColor("graph.selection.color", "#00ff00");
        mxConstants.EDGE_SELECTION_COLOR = Prefs.getPreferences().getColor("graph.edge.color", "#3c6318");
        this.graph = new mxGraph() {
            @Override
            public String getToolTipForCell(Object object) {
                if (NetworkGraph.this.tooltips.get(object) == null) {
                    return "";
                }
                return NetworkGraph.this.tooltips.get(object) + "";
            }
        };
        this.graph.setAutoOrigin(true);
        this.graph.setCellsEditable(false);
        this.graph.setCellsResizable(false);
        this.graph.setCellsBendable(false);
        this.graph.setAllowDanglingEdges(false);
        this.graph.setSplitEnabled(false);
        this.graph.setKeepEdgesInForeground(false);
        this.graph.setKeepEdgesInBackground(true);
        this.parent = this.graph.getDefaultParent();
        this.component = new NetworkGraphComponent(this.graph);
        this.component.setFoldingEnabled(true);
        this.component.setConnectable(false);
        this.component.setCenterPage(true);
        this.component.setToolTips(true);
        this.graph.setDropEnabled(true);
        new mxRubberband(this.component);
        addPopupListener();
        this.layout = Prefs.getPreferences().getString("graph.default_layout.layout", "none");
        this.component.getViewport().setOpaque(false);
        this.component.setOpaque(true);
        this.component.setBackground(Prefs.getPreferences().getColor("graph.background.color", "#111111"));
        setLayout(new BorderLayout());
        add(this.component, "Center");
        setupShortcuts();
    }

    public void addActionForKeyStroke(KeyStroke keyStroke, Action action) {
        this.component.getActionMap().put(keyStroke.toString(), action);
        this.component.getInputMap().put(keyStroke, keyStroke.toString());
    }

    public void addActionForKey(String string, Action action) {
        addActionForKeyStroke(KeyStroke.getKeyStroke(string), action);
    }

    public void addActionForKeySetting(String string1, String string2, Action action) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(string2);
        if (keyStroke != null) {
            addActionForKeyStroke(keyStroke, action);
        }
    }

    public void doStackLayout() {
        if (this.layout != null) {
            this.layout = "stack";
        }
        mxStackLayout mxStackLayout2 = new mxStackLayout(this.graph, true, 25);
        mxStackLayout2.execute(this.parent);
    }

    public void doTreeLeftLayout() {
        if (this.layout != null) {
            this.layout = "tree-left";
        }
        mxHierarchicalLayout mxHierarchicalLayout2 = new mxHierarchicalLayout(this.graph, 7);
        mxHierarchicalLayout2.execute(this.parent);
    }

    public void doTreeRightLayout() {
        if (this.layout != null) {
            this.layout = "tree-right";
        }
        mxHierarchicalLayout mxHierarchicalLayout2 = new mxHierarchicalLayout(this.graph, 3);
        mxHierarchicalLayout2.execute(this.parent);
    }

    public void doTreeTopLayout() {
        if (this.layout != null) {
            this.layout = "tree-top";
        }
        mxHierarchicalLayout mxHierarchicalLayout2 = new mxHierarchicalLayout(this.graph, 1);
        mxHierarchicalLayout2.execute(this.parent);
    }

    public void doTreeBottomLayout() {
        if (this.layout != null) {
            this.layout = "tree-bottom";
        }
        mxHierarchicalLayout mxHierarchicalLayout2 = new mxHierarchicalLayout(this.graph, 5);
        mxHierarchicalLayout2.execute(this.parent);
    }

    public void doCircleLayout() {
        if (this.layout != null) {
            this.layout = "circle";
        }
        CircleLayout circleLayout = new CircleLayout(this.graph, 1.0D);
        circleLayout.execute(this.parent);
    }

    public void doTreeLayout() {
        mxFastOrganicLayout mxFastOrganicLayout2 = new mxFastOrganicLayout(this.graph);
        mxFastOrganicLayout2.execute(this.parent);
    }

    private void setupShortcuts() {
        addActionForKeySetting("graph.clear_selection.shortcut", "pressed ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.clearSelection();
            }
        });
        addActionForKeySetting("graph.select_all.shortcut", "ctrl pressed A", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.selectAll();
            }
        });
        addActionForKeySetting("graph.zoom_in.shortcut", "ctrl pressed EQUALS", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.zoom(0.1D);
            }
        });
        addActionForKeySetting("graph.zoom_out.shortcut", "ctrl pressed MINUS", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.zoom(-0.1D);
            }
        });
        addActionForKeySetting("graph.zoom_reset.shortcut", "ctrl pressed 0", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.resetZoom();
            }
        });
        addActionForKeySetting("graph.arrange_icons_stack.shortcut", "ctrl pressed S", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.doStackLayout();
            }
        });
        addActionForKeySetting("graph.arrange_icons_circle.shortcut", "ctrl pressed C", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.doCircleLayout();
            }
        });
        addActionForKeySetting("graph.arrange_icons_hierarchical.shortcut", "ctrl pressed H", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NetworkGraph.this.doTreeLeftLayout();
            }
        });
    }

    public String getCellAt(Point point) {
        Point point1 = this.component.getViewport().getViewPosition();
        Point point2 = new Point((int) (point.getX() + point1.getX()), (int) (point.getY() + point1.getY()));
        mxCell mxCell2 = (mxCell) this.component.getCellAt((int) point2.getX(), (int) point2.getY());
        if (mxCell2 != null) {
            return mxCell2.getId();
        }
        return null;
    }

    public String[] getSelectedHosts() {
        LinkedList<String> linkedList = new LinkedList<String>();
        Object[] arrobject = this.graph.getSelectionCells();
        for (int i = 0; i < arrobject.length; i++) {
            mxCell mxCell2 = (mxCell) arrobject[i];
            if (nodes.containsKey(mxCell2.getId()) && !"".equals(mxCell2.getId())) {
                linkedList.add(mxCell2.getId());
            }
        }
        String[] arrstring = new String[linkedList.size()];
        Iterator iterator = linkedList.iterator();
        for (int j = 0; iterator.hasNext(); j++) {
            arrstring[j] = (iterator.next() + "");
        }
        return arrstring;
    }

    private void addPopupListener() {
        this.component.getGraphControl().addMouseListener(new MouseAdapter() {
            public void handleEvent(MouseEvent mouseEvent) {
                if (mouseEvent.isPopupTrigger() && getGraphPopup() != null) {
                    getGraphPopup().showGraphPopup(getSelectedHosts(), mouseEvent);
                    mouseEvent.consume();
                } else if (mouseEvent.getClickCount() < 2 || mouseEvent.isConsumed()) {
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                handleEvent(mouseEvent);
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                handleEvent(mouseEvent);
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                handleEvent(mouseEvent);
            }
        });
    }

    public void resetZoom() {
        this.zoom = 1.0D;
        zoom(0.0D);
    }

    public void zoom(double d) {
        this.zoom += d;
        this.component.zoomTo(this.zoom, true);
    }

    public void start() {
        this.graph.getModel().beginUpdate();
        this.nodes.startUpdates();
        for (mxCell edge : this.edges) {
            graph.getModel().remove(edge);
        }
        edges = new LinkedList();
    }

    public void setAutoLayout(String string) {
        this.layout = string;
        autoLayout();
    }

    public void autoLayout() {
        if (this.layout == null) {
            return;
        }
        if (this.layout.equals("circle")) {
            doCircleLayout();
        }
        if (this.layout.equals("stack")) {
            doStackLayout();
        }
        if (this.layout.equals("tree-left")) {
            doTreeLeftLayout();
        }
        if (this.layout.equals("tree-top")) {
            doTreeTopLayout();
        }
        if (this.layout.equals("tree-right")) {
            doTreeRightLayout();
        }
        if (this.layout.equals("tree-bottom")) {
            doTreeBottomLayout();
        }
    }

    public void end() {
        this.graph.getModel().endUpdate();
        CommonUtils.runSafe(new Runnable() {
            @Override
            public void run() {
                NetworkGraph.this.autoLayout();
                NetworkGraph.this.graph.refresh();
            }
        });
    }

    public void deleteNodes(String[] arrstring) {
        Object[] arrobject = new Object[arrstring.length];
        for (int i = 0; i < arrstring.length; i++) {
            arrobject[i] = this.nodes.remove(arrstring[i]);
        }
        this.graph.removeCells(arrobject, true);
    }

    public void deleteNodes() {
        List localList = this.nodes.clearUntouched();
        Object[] arrobject = new Object[localList.size()];
        Iterator iterator = localList.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Map.Entry localEntry = (Map.Entry) iterator.next();
            arrobject[i] = localEntry.getValue();
        }
        this.graph.removeCells(arrobject, true);
    }

    public void addEdge(String string1, String string2, String string3, String string4, String string5, String string6, int n) {
        mxCell mxCell1 = (mxCell) this.nodes.get(string1);
        mxCell mxCell2 = (mxCell) this.nodes.get(string2);
        mxCell mxCell3 = (mxCell) this.graph.insertEdge(this.parent, null, string6, mxCell1, mxCell2);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("fontColor=" + Prefs.getPreferences().getString("graph.foreground.color", "#cccccc") + ";");
        Font font = Prefs.getPreferences().getFont("graph.font.font", "Monospaced BOLD 14");
        stringBuffer.append("fontSize=" + font.getSize() + ";");
        stringBuffer.append("fontFamily=" + font.getFamily() + ";");
        stringBuffer.append("fontStyle=" + font.getStyle() + ";");
        stringBuffer.append("strokeColor=" + string3 + ";strokeWidth=" + string4 + ";dashed=" + string5);
        if (n == 2) {
            stringBuffer.append(";startArrow=classic;endArrow=none");
        } else if (n == 1) {
            stringBuffer.append(";startArrow=none;endArrow=classic");
        } else if (n == 0) {
            stringBuffer.append(";startArrow=none;endArrow=none");
        }
        mxCell3.setStyle(stringBuffer.toString());
        this.edges.add(mxCell3);
    }

    public Object addNode(String string1, String string2, String string3, Image image, String string4, String string5) {
        nodeImages.put(string1, image);
        if (string2.length() > 0) {
            if (string3.length() > 0) {
                string3 = string3 + "\n" + string2;
            } else {
                string3 = string2;
            }
        }
        mxCell mxCell2;
        if (!this.nodes.containsKey(string1)) {
            mxCell2 = (mxCell) this.graph.insertVertex(this.parent, string1, string3, 0.0D, 0.0D, 125.0D, 97.0D);
            this.nodes.put(string1, mxCell2);
        } else {
            mxCell2 = (mxCell) this.nodes.get(string1);
            mxCell2.setValue(string3);
        }
        this.nodes.touch(string1);
        this.tooltips.put(mxCell2, string4);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("shape=image;image=" + string1 + ";");
        if ("good".equals(string5)) {
            stringBuffer.append("fontColor=#c6efce;");
        } else if ("bad".equals(string5)) {
            stringBuffer.append("fontColor=#ffc7ce;");
        } else if ("neutral".equals(string5)) {
            stringBuffer.append("fontColor=#ffeb9c;");
        } else if ("ignore".equals(string5)) {
            stringBuffer.append("fontColor=#a5a5a5;");
        } else if ("cancel".equals(string5)) {
            stringBuffer.append("fontColor=#3d579e;");
        } else {
            stringBuffer.append("fontColor=" + Prefs.getPreferences().getString("graph.foreground.color", "#cccccc") + ";");
        }
        Font font = Prefs.getPreferences().getFont("graph.font.font", "Monospaced BOLD 14");
        stringBuffer.append("fontSize=" + font.getSize() + ";");
        stringBuffer.append("fontFamily=" + font.getFamily() + ";");
        stringBuffer.append("fontStyle=" + font.getStyle() + ";");
        stringBuffer.append("verticalLabelPosition=bottom;verticalAlign=top");
        mxCell2.setStyle(stringBuffer.toString());
        return mxCell2;
    }

    public boolean requestFocusInWindow() {
        return this.component.requestFocusInWindow();
    }

    private class NetworkGraphComponent extends mxGraphComponent {
        public NetworkGraphComponent(mxGraph mxGraph2) {
            super(mxGraph2);
            setBorder(BorderFactory.createEmptyBorder());
            getHorizontalScrollBar().setUnitIncrement(15);
            getHorizontalScrollBar().setBlockIncrement(60);
            getVerticalScrollBar().setUnitIncrement(15);
            getVerticalScrollBar().setBlockIncrement(60);
        }

        public mxInteractiveCanvas createCanvas() {
            return new NetworkGraphCanvas();
        }
    }

    private class NetworkGraphCanvas extends mxInteractiveCanvas {
        private NetworkGraphCanvas() {
        }

        @Override
        public Image loadImage(String string) {
            if (NetworkGraph.this.nodeImages.containsKey(string)) {
                return (Image) nodeImages.get(string);
            }
            return super.loadImage(string);
        }
    }
}
