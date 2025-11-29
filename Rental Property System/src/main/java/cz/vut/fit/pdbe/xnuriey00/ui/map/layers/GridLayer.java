package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

public class GridLayer extends MapLayer {

      public GridLayer() {
            super("Grid");
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            double gridSpacing = 50.0;
            double scale = map.getScale();
            double offsetX = map.getOffsetX();
            double offsetY = map.getOffsetY();

            // Calculate visible world bounds
            double minWorldX = -offsetX / scale;
            double minWorldY = -offsetY / scale;
            double maxWorldX = (map.getWidth() - offsetX) / scale;
            double maxWorldY = (map.getHeight() - offsetY) / scale;

            g.setColor(new Color(200, 200, 200)); // Light Gray
            float strokeWidth = (float) (1.0 / scale);
            g.setStroke(new BasicStroke(strokeWidth));

            // Draw Vertical Lines
            double startX = Math.floor(minWorldX / gridSpacing) * gridSpacing;
            for (double x = startX; x <= maxWorldX; x += gridSpacing) {
                  g.draw(new Line2D.Double(x, minWorldY, x, maxWorldY));
            }

            // Draw Horizontal Lines
            double startY = Math.floor(minWorldY / gridSpacing) * gridSpacing;
            for (double y = startY; y <= maxWorldY; y += gridSpacing) {
                  g.draw(new Line2D.Double(minWorldX, y, maxWorldX, y));
            }
      }
}
