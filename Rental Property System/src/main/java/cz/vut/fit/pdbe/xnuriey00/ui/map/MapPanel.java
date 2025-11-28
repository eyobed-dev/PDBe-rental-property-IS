package cz.vut.fit.pdbe.xnuriey00.ui.map;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
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

    private List<MapLayer> layers = new ArrayList<>();
    private MapTool currentTool;

    // Specific Layers 
    private GridLayer gridLayer;
    private PropertyLayer propertyLayer;
    // Overlay layer can be generic or specific
    private List<MapOverlay> overlays = new ArrayList<>(); 

    // Viewport State
    private double scale = 10.0; 
    private double offsetX = 0;
    private double offsetY = 0;

    public MapPanel() {
        setBackground(new Color(194, 236, 255));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Initialize Layers
        gridLayer = new GridLayer();
        propertyLayer = new PropertyLayer();

        layers.add(gridLayer);
        layers.add(propertyLayer);
        // Add overlay layer logic here or keep it simple for now
        layers.add((g, map) -> drawOverlays(g));

        // Initialize Tool
        currentTool = new NavigationTool();

        // Mouse Listeners
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

    public void setProperties(List<Property> properties) {
        propertyLayer.setProperties(properties);
        repaint();
    }

    public void addOverlay(JGeometry geometry, Color color, String label) {
        overlays.add(new MapOverlay(geometry, color, label));
        repaint();
    }

    public void clearOverlays() {
        overlays.clear();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Save the original transform
        AffineTransform originalTransform = graphics.getTransform();

        // Apply Pan and Zoom
        graphics.translate(offsetX, offsetY);
        graphics.scale(scale, scale);

        // Render Layers
        for (MapLayer layer : layers) {
            layer.render(graphics, this);
        }

        // Restore original transform
        graphics.setTransform(originalTransform);
    }

    private void drawOverlays(Graphics2D graphics) {
        // Draw Overlays
        for (MapOverlay overlay : overlays) {
            try {
                Shape shape = jGeometryToShape(overlay.getGeometry());

                // Draw interior (semi-transparent)
                Color c = overlay.getColor();
                graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                graphics.fill(shape);

                // Draw boundary (solid)
                graphics.setColor(c);
                // Adjust stroke for zoom
                float strokeWidth = (float) (2.0 / scale);
                graphics.setStroke(new BasicStroke(strokeWidth));
                graphics.draw(shape);

                // Draw Label
                if (overlay.getLabel() != null) {
                    Rectangle2D bounds = shape.getBounds2D();
                    double labelX = bounds.getCenterX();
                    double labelY = bounds.getCenterY();

                    Font font = new Font("SansSerif", Font.BOLD, 14);
                    Font derivedFont = font.deriveFont((float) (14.0 / scale));
                    graphics.setFont(derivedFont);
                    graphics.setColor(Color.BLACK); // Label color
                    graphics.drawString(overlay.getLabel(), (float) labelX, (float) labelY);
                }

            } catch (Exception e) {
                System.err.println("Error drawing overlay: " + e.getMessage());
            }
        }
    }

    // =================The following is copied from the solution during the
    // labs===========//
    /**
     * Create a rectangular "point" for a given point.
     *
     * @param point2D a point
     * @return a rectangular shape centered over the point location with unit
     *         dimensions
     */
    public Shape point2dToShape(Point2D point2D) {
        // Fixed screen size (e.g., 10 pixels)
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

    // Overlay Class (Keep it here for now)
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
