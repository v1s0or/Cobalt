package ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JTabbedPane;

public class DraggableTabbedPane extends JTabbedPane {

    /*
     * https://stackoverflow.com/questions/60269/how-to-implement-draggable-tab-using-java-swing
     */

    private boolean dragging = false;

    private Image tabImage = null;

    private Point currentMouseLocation = null;

    private int draggedTabIndex = 0;

    public DraggableTabbedPane() {
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (!dragging) {
                    // Gets the tab index based on the mouse position
                    int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
                    if (tabNumber >= 0) {
                        draggedTabIndex = tabNumber;
                        Rectangle rectangle = getUI().getTabBounds(DraggableTabbedPane.this, tabNumber);

                        // Paint the tabbed pane to a buffer
                        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), 2);
                        Graphics graphics1 = bufferedImage.getGraphics();
                        graphics1.setClip(rectangle);
                        // Don't be double buffered when painting to a static image.
                        setDoubleBuffered(false);
                        paint(graphics1);
                        // Paint just the dragged tab to the buffer
                        tabImage = new BufferedImage(rectangle.width, rectangle.height, 2);
                        Graphics graphics2 = tabImage.getGraphics();
                        graphics2.drawImage(bufferedImage, 0, 0, rectangle.width, rectangle.height, rectangle.x, rectangle.y, rectangle.x + rectangle.width, rectangle.y + rectangle.height, DraggableTabbedPane.this);
                        dragging = true;
                        repaint();
                        graphics2.dispose();
                        graphics1.dispose();
                    }
                } else {
                    currentMouseLocation = e.getPoint();
                    repaint();
                }
                super.mouseDragged(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (dragging) {
                    int tabNumber = getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10);
                    if (e.getX() < 0) {
                        tabNumber = 0;
                    } else if (tabNumber == -1) {
                        tabNumber = getTabCount() - 1;
                    }
                    if (tabNumber >= 0) {
                        Component component1 = getComponentAt(draggedTabIndex);
                        Component component2 = getTabComponentAt(draggedTabIndex);
                        removeTabAt(draggedTabIndex);
                        insertTab("", null, component1, null, tabNumber);
                        setTabComponentAt(tabNumber, component2);
                        setSelectedIndex(tabNumber);
                    }
                }
                dragging = false;
                tabImage = null;
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Are we dragging?
        if (dragging && currentMouseLocation != null && tabImage != null) {
            // Draw the dragged tab
            g.drawImage(tabImage, currentMouseLocation.x, currentMouseLocation.y, this);
        }
    }
}
