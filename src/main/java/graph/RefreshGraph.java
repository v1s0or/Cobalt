package graph;

import common.CommonUtils;

import java.awt.Image;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RefreshGraph implements Runnable {

    protected List nodes = new LinkedList();
    protected List highlights = new LinkedList();
    protected List routes = new LinkedList();
    protected Refreshable graph = null;

    public RefreshGraph(Refreshable refreshable) {
        this.graph = refreshable;
    }

    public void go() {
        CommonUtils.runSafe(this);
    }

    public void addRoute(Route paramRoute) {
        this.routes.add(paramRoute);
    }

    public void addNode(String string1, String string2, String string3,
                        Image image, String string4) {
        Node node = new Node();
        node.id = string1;
        node.label = string2;
        node.description = string3;
        node.iconz = image;
        node.tooltip = string4;
        this.nodes.add(node);
    }

    public void addHighlight(String string1, String string2) {
        Highlight highlight = new Highlight();
        highlight.gateway = string1;
        highlight.host = string2;
        this.highlights.add(highlight);
    }

    public void run() {
        this.graph.start();
        Iterator iterator = this.nodes.iterator();
        Object localObject;
        while (iterator.hasNext()) {
            localObject = (Node) iterator.next();
            this.graph.addNode(((Node) localObject).id, ((Node) localObject).label,
                    ((Node) localObject).description, ((Node) localObject).iconz,
                    ((Node) localObject).tooltip);
        }
        this.graph.setRoutes((Route[]) this.routes.toArray(new Route[0]));
        iterator = this.highlights.iterator();
        while (iterator.hasNext()) {
            localObject = (Highlight) iterator.next();
            this.graph.highlightRoute(((Highlight) localObject).gateway,
                    ((Highlight) localObject).host);
        }
        this.graph.deleteNodes();
        this.graph.end();
    }

    private static class Node {
        public String id = "";
        public String label = "";
        public String description = "";
        public Image iconz = null;
        public String tooltip = "";
    }

    private static class Highlight {
        public String gateway = "";
        public String host = "";
    }
}
