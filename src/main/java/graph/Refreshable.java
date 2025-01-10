package graph;

import java.awt.Image;

public interface Refreshable {
    void start();

    Object addNode(String string1, String string2, String string3, Image image, String string4);

    void setRoutes(Route[] paramArrayOfRoute);

    void highlightRoute(String string1, String string2);

    void deleteNodes();

    void end();
}
