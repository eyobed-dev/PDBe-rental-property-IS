package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.Graphics2D;

public abstract class MapLayer {
      private boolean visible = true;
      private String name;

      public MapLayer(String name) {
            this.name = name;
      }

      public boolean isVisible() {
            return visible;
      }

      public void setVisible(boolean visible) {
            this.visible = visible;
      }

      public String getName() {
            return name;
      }

      public abstract void render(Graphics2D g, MapPanel map);

      public abstract cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity findEntityAt(java.awt.geom.Point2D p, MapPanel map);
}
