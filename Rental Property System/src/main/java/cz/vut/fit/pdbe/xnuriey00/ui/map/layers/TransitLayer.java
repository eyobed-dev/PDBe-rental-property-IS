package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.TransitStop;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

public class TransitLayer extends MapLayer {
      private List<TransitStop> stops;

      public TransitLayer() {
            super("Transit");
      }

      public void setStops(List<TransitStop> stops) {
            this.stops = stops;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (stops == null)
                  return;

            g.setColor(Color.ORANGE);

            for (TransitStop stop : stops) {
                  try {
                        Shape shape = map.jGeometryToShape(stop.getGeometry());
                        g.fill(shape);
                        g.setColor(Color.BLACK);
                        g.draw(shape);
                        g.setColor(Color.ORANGE); // Reset fill color
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }
}
