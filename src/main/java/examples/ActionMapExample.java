/*
* Copyright (C) 2006 Sun Microsystems, Inc. All rights reserved. Use is
* subject to license terms.
*/

package examples;

import org.jdesktop.application.*;
import org.jdesktop.application.ProxyAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


/**
 * This is a very simple example of a reusable {@code @Actions} class.  The code defines
 * a JComponent subclass called BaseScenePanel that defines two @Actions: create
 * and remove, that add/remove an icon from the scene panel.  These actions are added
 * to a right button popup menu for the component.
 * [TBD: demo resource shadowing too]
 *
 * @author Hans Muller (Hans.Muller@Sun.COM)
 */
public class ActionMapExample extends SingleFrameApplication {
    private static final Logger logger = Logger.getLogger(ActionMapExample.class.getName());
    private static final Insets zeroInsets = new Insets(0, 0, 0, 0);

    @Override
    protected void startup() {
        View view = getMainView();
        view.setComponent(createMainPanel());
        show(view);
    }

    public static void main(String[] args) {
        Launcher.getInstance().launch(ActionMapExample.class, args);
    }

    private JComponent createMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        initGridBagConstraints(c);
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new BaseScenePanel(this), c);

        initGridBagConstraints(c);
        c.weightx = 0.5;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(new DerivedScenePanelA(this), c);

        initGridBagConstraints(c);
        c.weightx = 0.5;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(new DerivedScenePanelB(this), c);
        return mainPanel;
    }

    private void initGridBagConstraints(GridBagConstraints c) {
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = GridBagConstraints.RELATIVE;
        c.insets = zeroInsets;
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 0.0;
        c.weighty = 0.0;
    }


    /**
     * A JComponent that renders a Scene and defines two {@code @Actions}:
     * <ul>
     * <li> {@code create} - adds a new Node to the scene to the right of the last one
     * <li> {@code remove} - removes the selected Node
     * </ul>
     * These actions are added to a popup menu.
     * <p/>
     * Subclasses can override the {@code create} and {@code remove} methods to
     * change the corresponding actions.
     */
    public static class BaseScenePanel extends JComponent implements PropertyChangeListener {
        private final Scene scene;
        private final Application application;

        @ProxyAction
        public void create() {
            ResourceMap resourceMap = getContext().getResourceMap(getClass(), BaseScenePanel.class);
            Node node = new Node(resourceMap.getIcon("circleIcon"));
            Insets insets = getInsets();
            int iconGap = 8;
            Rectangle r = getScene().getLastNodeBounds();
            int x = insets.left + ((r.width == 0) ? 0 : (r.x + r.width + iconGap));
            int y = insets.top;
            node.setLocation(new Point(x, y));
            getScene().add(node);
        }

        @ProxyAction
        public void remove() {
            getScene().remove(getScene().getSelectedNode());
        }

        public BaseScenePanel(Application application) {
            if (application == null) {
                throw new IllegalArgumentException("null applicaiton");
            }
            this.application = application;
            setLayout(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder(getClass().getSimpleName()));
            ActionMap actionMap = getContext().getActionMap(BaseScenePanel.class, this);
            JPopupMenu menu = new JPopupMenu();
            menu.add(actionMap.get("create"));
            menu.add(actionMap.get("remove"));
            addMouseListener(new PopupMenuListener(menu));
            addMouseListener(new SelectionListener());
            scene = new Scene();
            scene.addPropertyChangeListener(this);
        }

        private class SelectionListener extends MouseAdapter {
            public void mousePressed(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    Node node = getScene().nodeAt(e.getX(), e.getY());
                    if (node != null) {
                        getScene().setSelectedNode(node);
                    }
                }
            }
        }

        protected final Scene getScene() {
            return scene;
        }

        protected final Application getApplication() {
            return application;
        }

        protected final ApplicationContext getContext() {
            return application.getContext();
        }

        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName() == null) {
                repaint();
            } else if (e.getPropertyName().equals("selectedNode")) {
                Node node = getScene().getSelectedNode();
                repaint(); // todo oldSelection + newSelection bounds
            }
        }

        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            for (Node node : getScene().getNodes()) {
                Icon icon = node.getIcon();
                Point location = node.getLocation();
                icon.paintIcon(this, g, location.x, location.y);
                if (node == getScene().getSelectedNode()) {
                    g.setColor(getForeground());
                    Rectangle r = node.getBounds();
                    g.drawRect(r.x, r.y, r.width, r.height);
                }
            }
        }

        public Dimension getPreferredSize() {
            List<Node> nodes = getScene().getNodes();
            int maxX = 128;
            int maxY = 128;
            for (Node node : nodes) {
                Rectangle r = node.getBounds();
                maxX = Math.max(maxX, r.x);
                maxY = Math.max(maxY, r.y);
            }
            Insets insets = getInsets();
            return new Dimension(maxX + insets.left + insets.right, maxY + insets.top + insets.bottom);
        }
    }


    public static class DerivedScenePanelA extends BaseScenePanel {
        public DerivedScenePanelA(Application application) {
            super(application);
        }

        @Override
        public void create() {
            ResourceMap resourceMap = getContext().getResourceMap(getClass(), BaseScenePanel.class);
            Node node = new Node(resourceMap.getIcon("squareIcon"));
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top;
            List<Node> nodes = getScene().getNodes();
            if (nodes.size() > 0) {
                int iconGap = 8;
                Node lastNode = nodes.get(nodes.size() - 1);
                Rectangle r = lastNode.getBounds();
                x += r.x + r.width + iconGap;
            }
            node.setLocation(new Point(x, y));
            getScene().add(node);
        }
    }

    public static class DerivedScenePanelB extends BaseScenePanel {
        public DerivedScenePanelB(Application application) {
            super(application);
        }

        @Override
        public void create() {
            ResourceMap resourceMap = getContext().getResourceMap(getClass(), BaseScenePanel.class);
            Node node = new Node(resourceMap.getIcon("triangleIcon"));
            Insets insets = getInsets();
            int x = insets.left;
            int y = insets.top;
            List<Node> nodes = getScene().getNodes();
            if (nodes.size() > 0) {
                int iconGap = 8;
                Node lastNode = nodes.get(nodes.size() - 1);
                Rectangle r = lastNode.getBounds();
                y += r.y + r.height + iconGap;
            }
            node.setLocation(new Point(x, y));
            getScene().add(node);
        }
    }


    /* This is essentially boilerplate: popup the specified menu when
     * the platform-specific mouse press/release event occurs.
     */
    private static class PopupMenuListener extends MouseAdapter {
        private final JPopupMenu menu;

        PopupMenuListener(JPopupMenu menu) {
            this.menu = menu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }


    /* Trivial scene model: just a list of Nodes and a selected 
     * node property.  Any change to the list of nodes is reported
     * to listeners with a PropertyChangeEvent whose name and old/new
     * values are null.  This is a more or less conventional use of
     * the class, it means that "that an arbitrary set of 
     * [the source object's] properties have changed".
     * See http://java.sun.com/javase/6/docs/api/java/beans/PropertyChangeEvent.html
     */
    private static class Scene extends AbstractBean {
        private final List<Node> nodes = new ArrayList<Node>();
        private Node selectedNode = null;

        public void add(Node node) {
            if (node == null) {
                throw new IllegalArgumentException("null node");
            }
            nodes.add(node);
            firePropertyChange(null, null, null);
        }

        public void remove(Node node) {
            if (nodes.remove(node)) {
                firePropertyChange(null, null, null);
            }
        }

        public final List<Node> getNodes() {
            return Collections.unmodifiableList(nodes);
        }

        public final Rectangle getLastNodeBounds() {
            if (nodes.size() == 0) {
                return new Rectangle(0, 0, 0, 0);
            } else {
                Node lastNode = nodes.get(nodes.size() - 1);
                return lastNode.getBounds();
            }
        }

        public final Node nodeAt(int x, int y) {
            Node lastNode = null;
            for (Node node : nodes) {
                if (node.getBounds().contains(x, y)) {
                    lastNode = node;
                }
            }
            return lastNode;
        }

        public Node getSelectedNode() {
            return selectedNode;
        }

        public void setSelectedNode(Node selectedNode) {
            Node oldValue = getSelectedNode();
            this.selectedNode = selectedNode;
            firePropertyChange("selectedNode", oldValue, this.selectedNode);
        }
    }


    /* Trivial scene model element: an icon and the location it's to
     * appear in.  The location property is bound.
     */
    private static class Node extends AbstractBean {
        private final Icon icon;
        private final Point location = new Point(0, 0);

        public Node(Icon icon) {
            if (icon == null) {
                throw new IllegalArgumentException("null icon");
            }
            this.icon = icon;
        }

        public final Icon getIcon() {
            return icon;
        }

        public Point getLocation() {
            return new Point(location);
        }

        public void setLocation(Point location) {
            if (location == null) {
                throw new IllegalArgumentException("null location");
            }
            Point oldValue = getLocation();
            this.location.setLocation(location);
            firePropertyChange("location", oldValue, getLocation());
        }

        public final Rectangle getBounds() {
            Point p = getLocation();
            return new Rectangle(p.x, p.y, icon.getIconWidth(), icon.getIconHeight());
        }
    }

}

