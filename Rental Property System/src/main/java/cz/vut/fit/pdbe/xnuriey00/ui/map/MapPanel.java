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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapPanel extends JPanel {

    // Selection Listeners
    private List<Consumer<cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity>> selectionListeners = new ArrayList<>();

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
    private BikeRouteLayer bikeRouteLayer;

    // Viewport State
    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    public MapPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Initialize Layers
        gridLayer = new GridLayer();
        overlayLayer = new OverlayLayer();

        propertyLayer = new PropertyLayer();
        schoolLayer = new SchoolLayer();
        transitLayer = new TransitLayer();
        cellularLayer = new CellularLayer();
        bikeRouteLayer = new BikeRouteLayer();

        // Add layers to the list (order matters - first = bottom, last = top)
        layers.add(gridLayer);

        // Data layers
        layers.add(propertyLayer);
        layers.add(schoolLayer);
        layers.add(transitLayer);
        layers.add(cellularLayer);
        layers.add(bikeRouteLayer);

        // Overlay layer MUST be last so it renders on top
        layers.add(overlayLayer);

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

    public void setBikeRoutes(List<BikeRoute> routes) {
        bikeRouteLayer.setRoutes(routes);
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

        // Render Layers by passing the graphics and map to the layer sequentially
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
        return new Ellipse2D.Double(point2D.getX() - 8, point2D.getY() - 8, 16, 16);
    }

    /**
     * Make a Shape object from a JGeometry object (the Java representation of
     * SDO_GEOMETRY data). The
     * Shape objects are drawable into Java GUI.
     *
     * @param jGeometry the JGeometry object as an input
     * @return the Shape object corresponding to the input JGeometry object
     * @throws Exception if cannot make the Shape object from the JGeometry object
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

    // Selection Handling
    public void addSelectionListener(Consumer<cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity> listener) {
        selectionListeners.add(listener);
    }

    public void selectAt(int screenX, int screenY) {
        // Convert screen to world coordinates
        double worldX = (screenX - offsetX) / scale;
        double worldY = (screenY - offsetY) / scale;
        Point2D worldPoint = new Point2D.Double(worldX, worldY);

        // Iterate through layers in reverse order (top to bottom)
        for (int i = layers.size() - 1; i >= 0; i--) {
            MapLayer layer = layers.get(i);
            if (layer.isVisible()) {
                cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity entity = layer.findEntityAt(worldPoint, this);
                if (entity != null) {
                    // Notify listeners
                    for (Consumer<cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity> listener : selectionListeners) {
                        listener.accept(entity);
                    }
                    return; // Stop after finding the top-most entity
                }
            }
        }
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
