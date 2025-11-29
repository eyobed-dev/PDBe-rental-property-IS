package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.event.MouseEvent;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DrawTool implements MapTool {

      private List<Point2D> points = new ArrayList<>();
      private Point2D currentMousePos;
      private Consumer<JGeometry> onComplete;

      public DrawTool(Consumer<JGeometry> onComplete) {
            this.onComplete = onComplete;
      }

      @Override
      public void onMousePressed(MouseEvent e, MapPanel map) {
            // idk
      }

      @Override
      public void onMouseDragged(MouseEvent e, MapPanel map) {
            // idk may be drag to pan while drawing?
      }

      @Override
      public void onMouseReleased(MouseEvent e, MapPanel map) {
      }

      @Override
      public void onMouseClicked(MouseEvent e, MapPanel map) {
            // Convert screen coordinates to world coordinates
            double worldX = (e.getX() - map.getOffsetX()) / map.getScale();
            double worldY = (e.getY() - map.getOffsetY()) / map.getScale();
            Point2D point = new Point2D.Double(worldX, worldY);

            if (e.getClickCount() == 2 && points.size() >= 3) {
                  // Double click to finish
                  finishDrawing();
            } else {
                  points.add(point);
            }
            map.repaint();
      }

      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {
            double worldX = (e.getX() - map.getOffsetX()) / map.getScale();
            double worldY = (e.getY() - map.getOffsetY()) / map.getScale();
            currentMousePos = new Point2D.Double(worldX, worldY);
            map.repaint();
      }

      @Override
      public void render(Graphics2D g) {
            if (points.isEmpty())
                  return;

            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.0f));

            // Draw lines between existing points
            for (int i = 0; i < points.size() - 1; i++) {
                  Point2D p1 = points.get(i);
                  Point2D p2 = points.get(i + 1);
                  g.draw(new Line2D.Double(p1, p2));
            }

            // Draw rubber band line to mouse cursor
            if (currentMousePos != null && !points.isEmpty()) {
                  Point2D lastPoint = points.get(points.size() - 1);
                  g.draw(new Line2D.Double(lastPoint, currentMousePos));

                  // Also draw line to start point to show closing
                  if (points.size() >= 2) {
                        g.setColor(new Color(255, 0, 0, 100)); // Transparent red
                        g.draw(new Line2D.Double(currentMousePos, points.get(0)));
                  }
            }

            // Draw vertices
            g.setColor(Color.RED);
            double r = 4.0; // Radius
            for (Point2D p : points) {
                  g.fill(new java.awt.geom.Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r));
            }
      }

      private void finishDrawing() {
            if (points.size() < 3)
                  return;

            // Convert points to ordinate array
            // Oracle Spatial Polygon: x1, y1, x2, y2, ..., x1, y1 (must close)
            double[] ordinates = new double[(points.size() + 1) * 2];
            for (int i = 0; i < points.size(); i++) {
                  ordinates[i * 2] = points.get(i).getX();
                  ordinates[i * 2 + 1] = points.get(i).getY();
            }
            // Close the loop
            ordinates[points.size() * 2] = points.get(0).getX();
            ordinates[points.size() * 2 + 1] = points.get(0).getY();

            // Create a JGeometry polygon object from the collected ordinates.
            // JGeometry.createLinearPolygon expects:
            // 1. An array of ordinates (double[]), which are the x,y coordinates of the
            // polygon vertices.
            // The array must be closed, meaning the first and last points are identical.
            // 2. The dimension of the coordinates (int), which is 2 for 2D (x, y).
            // 3. The Spatial Reference ID (SRID) (int), which is 0 here, indicating an
            // undefined or local coordinate system.
            JGeometry polygon = JGeometry.createLinearPolygon(ordinates, 2, 0);

            if (onComplete != null) {
                  onComplete.accept(polygon);
            }

            points.clear();
            currentMousePos = null;
      }
}
