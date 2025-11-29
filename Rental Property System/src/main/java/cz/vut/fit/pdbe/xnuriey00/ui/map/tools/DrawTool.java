package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DrawTool implements MapTool {

      private List<double[]> parts = new ArrayList<>(); // List of ordinates for each part
      private List<Integer> elemInfoList = new ArrayList<>(); // ElemInfo triplet list
      private List<Point2D> currentPoints = new ArrayList<>(); // Points for current part

      private Point2D currentMousePos;
      private Consumer<JGeometry> onComplete;
      private DrawSettingsPanel settings;

      public DrawTool(DrawSettingsPanel settings, Consumer<JGeometry> onComplete) {
            this.settings = settings;
            this.onComplete = onComplete;

            // Listen for manual finish (for Multi geometries)
            if (settings != null) {
                  settings.setOnFinishCallback(this::finishGeometry);
                  settings.addTypeChangeListener(e -> reset());
            }
      }

      private void reset() {
            parts.clear();
            elemInfoList.clear();
            currentPoints.clear();
            currentMousePos = null;
      }

      @Override
      public void onMousePressed(MouseEvent e, MapPanel map) {
      }

      @Override
      public void onMouseDragged(MouseEvent e, MapPanel map) {
            if (settings.getSelectedType() == GeometryType.CIRCLE && !currentPoints.isEmpty()) {
                  // Dragging for circle radius
                  updateMousePos(e, map);
            }
      }

      @Override
      public void onMouseReleased(MouseEvent e, MapPanel map) {
            if (settings.getSelectedType() == GeometryType.CIRCLE && !currentPoints.isEmpty()) {
                  // Finish circle on release
                  finishPart();
                  if (settings.getSelectedType() != GeometryType.MULTI_POINT) { // Circle is single usually
                        finishGeometry();
                  }
            }
      }

      @Override
      public void onMouseClicked(MouseEvent e, MapPanel map) {
            updateMousePos(e, map);
            Point2D point = currentMousePos;
            GeometryType type = settings.getSelectedType();

            if (type == GeometryType.CIRCLE) {
                  if (currentPoints.isEmpty()) {
                        currentPoints.add(point); // Center
                  }
                  return;
            }

            if (e.getClickCount() == 2) {
                  // Double click to finish part
                  if (type == GeometryType.LINE || type == GeometryType.POLYGON ||
                              type == GeometryType.MULTI_LINE || type == GeometryType.MULTI_POLYGON) {
                        finishPart();

                        // If single geometry, finish completely
                        if (type == GeometryType.LINE || type == GeometryType.POLYGON) {
                              finishGeometry();
                        }
                  }
            } else {
                  // Single click adds point
                  currentPoints.add(point);

                  // Point type finishes immediately
                  if (type == GeometryType.POINT) {
                        finishPart();
                        finishGeometry();
                  } else if (type == GeometryType.MULTI_POINT) {
                        finishPart();
                  }
            }
            map.repaint();
      }

      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {
            updateMousePos(e, map);
            map.repaint();
      }

      private void updateMousePos(MouseEvent e, MapPanel map) {
            double worldX = (e.getX() - map.getOffsetX()) / map.getScale();
            double worldY = (e.getY() - map.getOffsetY()) / map.getScale();
            currentMousePos = new Point2D.Double(worldX, worldY);
      }

      private void finishPart() {
            if (currentPoints.isEmpty())
                  return;

            GeometryType type = settings.getSelectedType();
            boolean isHole = settings.isHole();

            // 1. Construct Ordinates for this part
            double[] ordinates;

            if (type == GeometryType.CIRCLE) {
                  // Circle as Polygon (Area)
                  // 3 points on circumference
                  Point2D center = currentPoints.get(0);
                  double radius = center.distance(currentMousePos);

                  ordinates = new double[] {
                              center.getX() + radius, center.getY(), // Right
                              center.getX(), center.getY() + radius, // Top
                              center.getX() - radius, center.getY() // Left
                  };

                  // ElemInfo for Circle Polygon:
                  // Exterior: [offset, 1003, 4]
                  // Interior: [offset, 2003, 4]
                  addElemInfo(isHole ? 2003 : 1003, 4);

            } else {
                  // Linear types
                  int size = currentPoints.size();
                  if (type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON) {
                        // Close polygon
                        size++;
                  }

                  ordinates = new double[size * 2];
                  for (int i = 0; i < currentPoints.size(); i++) {
                        ordinates[i * 2] = currentPoints.get(i).getX();
                        ordinates[i * 2 + 1] = currentPoints.get(i).getY();
                  }

                  if (type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON) {
                        // Close loop
                        ordinates[(size - 1) * 2] = currentPoints.get(0).getX();
                        ordinates[(size - 1) * 2 + 1] = currentPoints.get(0).getY();

                        // ElemInfo:
                        // Exterior: [offset, 1003, 1]
                        // Interior (Hole): [offset, 2003, 1]
                        addElemInfo(isHole ? 2003 : 1003, 1);
                  } else if (type == GeometryType.POINT || type == GeometryType.MULTI_POINT) {
                        // Point: [offset, 1, 1]
                        addElemInfo(1, 1);
                  } else {
                        // Line: [offset, 2, 1]
                        addElemInfo(2, 1);
                  }
            }

            parts.add(ordinates);
            currentPoints.clear();
      }

      private void addElemInfo(int etype, int interpretation) {
            // Calculate current offset (1-based index in ordinates array)
            int offset = 1;
            for (double[] part : parts) {
                  offset += part.length;
            }
            elemInfoList.add(offset);
            elemInfoList.add(etype);
            elemInfoList.add(interpretation);
      }

      public void finishGeometry() {
            if (parts.isEmpty() && currentPoints.isEmpty())
                  return;

            // If there are pending points, try to finish part
            if (!currentPoints.isEmpty())
                  finishPart();
            if (parts.isEmpty())
                  return;

            // Flatten ordinates
            int totalOrds = 0;
            for (double[] part : parts)
                  totalOrds += part.length;

            double[] allOrdinates = new double[totalOrds];
            int idx = 0;
            for (double[] part : parts) {
                  System.arraycopy(part, 0, allOrdinates, idx, part.length);
                  idx += part.length;
            }

            // Create ElemInfo array
            int[] elemInfo = new int[elemInfoList.size()];
            for (int i = 0; i < elemInfoList.size(); i++)
                  elemInfo[i] = elemInfoList.get(i);

            // Determine GType
            int gtype = 2000; // Unknown
            GeometryType type = settings.getSelectedType();
            switch (type) {
                  case POINT:
                        gtype = 2001;
                        break;
                  case LINE:
                        gtype = 2002;
                        break;
                  case POLYGON:
                        gtype = 2003;
                        break;
                  case CIRCLE:
                        gtype = 2003;
                        break; // Treated as Polygon
                  case MULTI_POINT:
                        gtype = 2005;
                        break;
                  case MULTI_LINE:
                        gtype = 2006;
                        break;
                  case MULTI_POLYGON:
                        gtype = 2007;
                        break;
            }

            JGeometry geom = new JGeometry(gtype, 0, elemInfo, allOrdinates);

            if (onComplete != null) {
                  onComplete.accept(geom);
            }
            reset();
      }

      @Override
      public void render(Graphics2D g) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2.0f));

            // Draw finished parts
            for (int i = 0; i < parts.size(); i++) {
                  double[] ords = parts.get(i);
                  // Simple rendering of parts
                  for (int j = 0; j < ords.length - 2; j += 2) {
                        g.draw(new Line2D.Double(ords[j], ords[j + 1], ords[j + 2], ords[j + 3]));
                  }
            }

            // Draw current points
            if (!currentPoints.isEmpty()) {
                  for (int i = 0; i < currentPoints.size() - 1; i++) {
                        Point2D p1 = currentPoints.get(i);
                        Point2D p2 = currentPoints.get(i + 1);
                        g.draw(new Line2D.Double(p1, p2));
                  }

                  // Rubber band
                  if (currentMousePos != null) {
                        Point2D last = currentPoints.get(currentPoints.size() - 1);
                        if (settings.getSelectedType() == GeometryType.CIRCLE) {
                              double r = last.distance(currentMousePos);
                              g.draw(new Ellipse2D.Double(last.getX() - r, last.getY() - r, 2 * r, 2 * r));
                        } else {
                              g.draw(new Line2D.Double(last, currentMousePos));
                        }
                  }

                  // Vertices
                  double r = 3.0;
                  for (Point2D p : currentPoints) {
                        g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r));
                  }
            }
      }
}
