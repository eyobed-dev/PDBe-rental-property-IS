package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class OverlayLayer extends MapLayer {

      private List<MapPanel.MapOverlay> overlays = new ArrayList<>();

      public OverlayLayer() {
            super("Overlays");
      }

      public void addOverlay(MapPanel.MapOverlay overlay) {
            overlays.add(overlay);
      }

      public void clearOverlays() {
            overlays.clear();
      }

      @Override
      public void render(Graphics2D graphics, MapPanel map) {
            for (MapPanel.MapOverlay overlay : overlays) {
                  try {
                        Shape shape = map.jGeometryToShape(overlay.getGeometry());

                        // Draw interior 
                        Color c = overlay.getColor();
                        graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                        graphics.fill(shape);

                        // Draw boundary
                        graphics.setColor(c);
                        // Adjust stroke for zoom
                        float strokeWidth = (float) (2.0 / map.getScale());
                        graphics.setStroke(new BasicStroke(strokeWidth));
                        graphics.draw(shape);

                        // Draw Label
                        if (overlay.getLabel() != null) {
                              Rectangle2D bounds = shape.getBounds2D();
                              double labelX = bounds.getCenterX();
                              double labelY = bounds.getCenterY();

                              Font font = new Font("SansSerif", Font.BOLD, 14);
                              Font derivedFont = font.deriveFont((float) (14.0 / map.getScale()));
                              graphics.setFont(derivedFont);
                              graphics.setColor(Color.BLACK); // Label color
                              graphics.drawString(overlay.getLabel(), (float) labelX, (float) labelY);
                        }

                  } catch (Exception e) {
                        System.err.println("Error drawing overlay: " + e.getMessage());
                  }
            }
      }
}
