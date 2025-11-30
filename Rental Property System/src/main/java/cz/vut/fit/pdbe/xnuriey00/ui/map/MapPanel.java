package cz.vut.fit.pdbe.xnuriey00.ui.map;

import cz.vut.fit.pdbe.xnuriey00.model.*;
import cz.vut.fit.pdbe.xnuriey00.ui.map.layers.*;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.*;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MapPanel extends JPanel {

    // Layers list
    private List<MapLayer> layers = new ArrayList<>();

    // Current tool
    private MapTool currentTool;

    // UI Layers
    private GridLayer gridLayer;
    private OverlayLayer overlayLayer;

    // Data Layers
    private PropertyLayer propertyLayer;
    private SchoolLayer schoolLayer;
    private TransitLayer transitLayer;
    private CellularLayer cellularLayer;

    // Viewport State
    private double scale = 10.0;
    private double offsetX = 0;
    private double offsetY = 0;

    public MapPanel() {
        setBackground(new Color(194, 236, 255));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Initialize Layers
        gridLayer = new GridLayer();
        overlayLayer = new OverlayLayer();

        propertyLayer = new PropertyLayer();
        schoolLayer = new SchoolLayer();
        transitLayer = new TransitLayer();
        cellularLayer = new CellularLayer();

        // Add layers to the list
        layers.add(gridLayer);
        layers.add(overlayLayer);

        layers.add(propertyLayer);
        layers.add(schoolLayer);
        layers.add(transitLayer);
        layers.add(cellularLayer);

        // Initialize Tool
        currentTool = new NavigationTool();

        // Mouse Listeners for the tool to handle mouse events  
        // The MapPanel forwards all mouse events to whichever tool is currently active
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentTool != null)
                    currentTool.onMousePressed(e, MapPanel.this);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentTool != null)
                    currentTool.onMouseDragged(e, MapPanel.this);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentTool != null)
                    currentTool.onMouseReleased(e, MapPanel.this);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentTool != null)
                    currentTool.onMouseClicked(e, MapPanel.this);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (currentTool != null)
                    currentTool.onMouseWheelMoved(e, MapPanel.this);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentTool != null)
                    currentTool.onMouseMoved(e, MapPanel.this);
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    // Setters for the layers to update the data
    public void setProperties(List<Property> properties) {
        propertyLayer.setProperties(properties);
        repaint();
    }

    public void setSchools(List<School> schools) {
        schoolLayer.setAreas(schools);
        repaint();
    }

    public void setTransitStops(List<Transit> stops) {
        transitLayer.setStops(stops);
        repaint();
    }

    public void setCellularCoverage(List<Cellular> coverages) {
        cellularLayer.setCoverages(coverages);
        repaint();
    }

    // Setters for the tool to update the current tool
    public void setCurrentTool(MapTool tool) {
        this.currentTool = tool;
    }

    // Setters for the overlay layer to update the overlay
    public void addOverlay(JGeometry geometry, Color color, String label) {
        overlayLayer.addOverlay(new MapOverlay(geometry, color, label));
        repaint();
    }

    // Setters for the overlay layer to clear the overlay
    public void clearOverlays() {
        overlayLayer.clearOverlays();
        repaint();
    }

    // Viewport Control Methods
    public void pan(int dx, int dy) {
        offsetX += dx;
        offsetY += dy;
    }

    public void zoomIn() {
        scale *= 1.5;
    }

    public void zoomOut() {
        scale /= 1.5;
    }

    public double getScale() {
        return scale;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    // Paint the map that is its layers
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        // Set antialiasing for smooth rendering
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Save the original transform
        AffineTransform originalTransform = graphics.getTransform();

        // Apply Pan and Zoom
        graphics.translate(offsetX, offsetY);
        graphics.scale(scale, scale);

        // Render Layers
        for (MapLayer layer : layers) {
            if (layer.isVisible()) {
                layer.render(graphics, this);
            }
        }

        // Render Tool operations (if drawing is in progress)
        if (currentTool != null) {
            currentTool.render(graphics);
        }

        // Restore original transform
        graphics.setTransform(originalTransform);
    }

    // The following is copied from the solution during the labs

    /**
     * Create a rectangular "point" for a given point.
     *
     * @param point2D a point
     * @return a rectangular shape centered over the point location with unit
     *         dimensions
     */
    public Shape point2dToShape(Point2D point2D) {
        // Fixed screen size . looks good on my pc,
        double screenPixelSize = 10.0;
        double worldSize = screenPixelSize / scale;
        return new Rectangle2D.Double(point2D.getX() - (worldSize / 2), point2D.getY() - (worldSize / 2), worldSize,
                worldSize);
    }

    /**
     * Make a Shape object from a JGeometry object.
     */
    public Shape jGeometryToShape(JGeometry jGeometry) throws Exception {
        switch (jGeometry.getType()) {
            case JGeometry.GTYPE_CURVE:
            case JGeometry.GTYPE_POLYGON:
            case JGeometry.GTYPE_COLLECTION:
            case JGeometry.GTYPE_MULTICURVE:
            case JGeometry.GTYPE_MULTIPOLYGON:
                return jGeometry.createShape();
            case JGeometry.GTYPE_POINT:
                return point2dToShape(jGeometry.getJavaPoint());
            case JGeometry.GTYPE_MULTIPOINT:
                final Area area = new Area();
                for (Point2D point2D : jGeometry.getJavaPoints()) {
                    area.add(new Area(point2dToShape(point2D)));
                }
                return area;
            default:
                throw new Exception("Unknown Geometry Type");
        }
    }

    public List<MapLayer> getLayers() {
        return layers;
    }

    // Overlay Class (I keep it here for now)
    public static class MapOverlay {
        private JGeometry geometry;
        private Color color;
        private String label;

        public MapOverlay(JGeometry geometry, Color color, String label) {
            this.geometry = geometry;
            this.color = color;
            this.label = label;
        }

        public JGeometry getGeometry() {
            return geometry;
        }

        public Color getColor() {
            return color;
        }

        public String getLabel() {
            return label;
        }
    }
}
