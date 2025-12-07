package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class EditTool implements MapTool {
      private SpatialEntity entity;
      private int lastMouseX;
      private int lastMouseY;
      private boolean isDragging = false;

      public EditTool(SpatialEntity entity) {
            this.entity = entity;
      }

      @Override
      public void onMousePressed(MouseEvent e, MapPanel map) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            isDragging = true;
      }

      @Override
      public void onMouseDragged(MouseEvent e, MapPanel map) {
            if (!isDragging || entity == null)
                  return;

            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;

            // Convert screen delta to world delta
            double worldDx = dx / map.getScale();
            double worldDy = dy / map.getScale();

            // Update Geometry
            JGeometry geom = entity.getGeometry();
            JGeometry newGeom = translate(geom, worldDx, worldDy);
            entity.setGeometry(newGeom);

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            map.repaint();
      }

      @Override
      public void onMouseReleased(MouseEvent e, MapPanel map) {
            isDragging = false;
      }

      @Override
      public void onMouseClicked(MouseEvent e, MapPanel map) {

      }

      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {

      }

      private JGeometry translate(JGeometry geom, double dx, double dy) {
            double[] ordinates = geom.getOrdinatesArray();
            if (ordinates == null) {
                  // Fallback for simple points if they don't return ordinates array
                  if (geom.getType() == JGeometry.GTYPE_POINT) {
                        Point2D p = geom.getJavaPoint();
                        return new JGeometry(p.getX() + dx, p.getY() + dy, geom.getSRID());
                  }
                  return geom;
            }

            double[] newOrdinates = new double[ordinates.length];
            for (int i = 0; i < ordinates.length; i += 2) {
                  newOrdinates[i] = ordinates[i] + dx;
                  newOrdinates[i + 1] = ordinates[i + 1] + dy;
            }

            return new JGeometry(geom.getType(), geom.getSRID(), geom.getElemInfo(), newOrdinates);
      }

}
