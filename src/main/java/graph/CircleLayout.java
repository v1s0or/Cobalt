package graph;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import java.util.ArrayList;

public class CircleLayout extends mxCircleLayout {
    public CircleLayout(mxGraph mxGraph2, double d) {
        super(mxGraph2, d);
    }

    public void execute(Object object, int n1, int n2, double d) {
        mxIGraphModel mxIGraphModel2 = this.graph.getModel();
        mxIGraphModel2.beginUpdate();
        try {
            double d1 = 0.0D;
            Double double1 = null;
            Double double2 = null;
            ArrayList<Object> arrayList = new ArrayList();
            int i = mxIGraphModel2.getChildCount(object);
            int j;
            for (j = 0; j < i; j++) {
                Object child = mxIGraphModel2.getChildAt(object, j);
                if (!isVertexIgnored(child)) {
                    arrayList.add(child);
                    mxRectangle mxRectangle = getVertexBounds(child);
                    if (double1 == null) {
                        double1 = mxRectangle.getY();
                    } else {
                        double1 = Math.min(double1, mxRectangle.getY());
                    }
                    if (double2 == null) {
                        double2 = mxRectangle.getX();
                    } else {
                        double2 = Math.min(double2, mxRectangle.getX());
                    }
                    d1 = Math.min(d1, Math.max(mxRectangle.getWidth(), mxRectangle.getHeight()));
                } else if (!isEdgeIgnored(child)) {
                    if (isResetEdges())
                        this.graph.resetEdge(child);
                    if (isDisableEdgeStyle())
                        setEdgeStyleEnabled(child, false);
                }
            }
            j = arrayList.size();
            double d2 = ((n1 > n2) ? n2 : n1) / 2.8D * d;
            if (this.moveCircle) {
                double1 = this.x0;
                double2 = this.y0;
            }
            circle(arrayList.toArray(), d2, double2, double1);
        } finally {
            mxIGraphModel2.endUpdate();
        }
    }
}
