package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * the DrawTool class handles interactive drawing of shapes (geometrries) on
 * the map.
 * it listens to mouse events (clicks, drags, moves) n builds the geom step by step.
 * once geom is done, its turned into an Oracle Spatial JGeometry thing.
 *
 * Concepts:
 * - MapTool: an interface saying how a tool interacts w/ MapPanel
 * - JGeometry: the oracle spacial class for a geom (point line polygon etc)
 * - Ordinates: flat array of doubles x1 y1 x2 y2 ...
 * - ElemInfo: int array describing how ordinates r interpreted (line? poly?)
 */
public class DrawTool implements MapTool {

      // --- fields for storeing the geometry being drawen ---

      /**
       * stores the “parts” of the geom.
       * complex geoms can hve many parts (poly with hole, multi line…)
       * each part saved as double[] like [x1,y1,x2,y2...]
       */
      private final List<double[]> parts = new ArrayList<>();

      /**
       * keeps ElemInfo triplets for JGeom.
       * triplet:
       * 1. offset: where in ordinates this starts (1-based)
       * 2. eType: like 1=point 2=line 1003=polygon ext
       * 3. interpretation: 1=straight segments, 3=rectangle etc
       */
      private final List<Integer> elemInfoList = new ArrayList<>();

      /**
       * points in the *current* part we're drawing now.
       * temporary until part is finalized
       */
      private final List<Point2D> currentPoints = new ArrayList<>();

      /**
       * mouse pos in world coords (not screen px)
       */
      private Point2D currentMousePos;

      /**
       * callback to run when the geometry is done.
       * lets the creator get the JGeometry final result
       */
      private final Consumer<JGeometry> onComplete;

      /**
       * settings panel so we know what geom type to draw (point/line/poly etc)
       */
      private final DrawSettingsPanel settings;

      /**
       * constructor
       *
       * @param settings panel with drawing settings (type selected, hole mode…)
       * @param onComplete runs when drawing finishes
       */
      public DrawTool(DrawSettingsPanel settings, Consumer<JGeometry> onComplete) {
            this.settings = settings;
            this.onComplete = onComplete;

            // if settings exist, setup listeners for finishing or reset
            if (settings != null) {
                  // when user hits “finish” call finishGeometry()
                  settings.setOnFinishCallback(this::finishGeometry);

                  // requirement update: reset only by manual reset button now
                  // no more auto reset on type change
                  settings.setOnResetCallback(this::reset);

                  // removed: settings.addTypeChangeListener(e -> reset());
            }
      }

      /**
       * resets tool back to start, clears points n parts
       */
      private void reset() {
            parts.clear();
            elemInfoList.clear();
            currentPoints.clear();
            currentMousePos = null;
      }

      // --- mouse handlers ---

      @Override
      public void onMousePressed(MouseEvent e, MapPanel map) {
            // not used here
      }

      /**
       * handles drag
       * used for setting radius of circle while dragging
       */
      @Override
      public void onMouseDragged(MouseEvent e, MapPanel map) {
            // if drawing circle and center exists update mouse pos to show radius preview
            if (settings.getSelectedType() == GeometryType.CIRCLE && !currentPoints.isEmpty()) {
                  updateMousePos(e, map);
            }
      }

      // flag so click event doesnt start a new circle instantly after release ends one
      private boolean circleJustFinished = false;

      /**
       * handles release
       * for circle: releasing finishes it
       */
      @Override
      public void onMouseReleased(MouseEvent e, MapPanel map) {
            if (settings.getSelectedType() == GeometryType.CIRCLE && !currentPoints.isEmpty()) {
                  finishPart();
                  circleJustFinished = true; // ignore next click
            }
      }

      /**
       * main click handler
       * adds points or finishes parts etc
       */
      @Override
      public void onMouseClicked(MouseEvent e, MapPanel map) {
            // ignore click after circle finish
            if (circleJustFinished) {
                  circleJustFinished = false;
                  return;
            }

            // update mouse pos
            updateMousePos(e, map);
            Point2D point = currentMousePos;
            GeometryType type = settings.getSelectedType();

            // circle: first click = center, drag shows radius, release ends it
            if (type == GeometryType.CIRCLE) {
                  if (currentPoints.isEmpty()) {
                        currentPoints.add(point);
                  }
                  return;
            }

            // double click finishes a part (line segment or polygon ring)
            if (e.getClickCount() == 2) {

                  // polygon needs >=3 pts
                  if ((type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON)
                              && currentPoints.size() < 3) {
                        JOptionPane.showMessageDialog(map, "Polygon must have at least 3 points", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        return;
                  }
                  // line needs >=2 pts
                  if ((type == GeometryType.LINE || type == GeometryType.MULTI_LINE) && currentPoints.size() < 2) {
                        JOptionPane.showMessageDialog(map, "Line must have at least 2 points", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        return;
                  }

                  // finish the part
                  if (type == GeometryType.LINE || type == GeometryType.POLYGON || type == GeometryType.MULTI_LINE
                              || type == GeometryType.MULTI_POLYGON) {
                        finishPart();
                        // double click only ends part, user must still press final Finish button
                  }

            } else {
                  // single click adds point
                  currentPoints.add(point);

                  // point ends instantly
                  if (type == GeometryType.POINT) {
                        finishPart();
                  }
                  // multi point ends each point as separate part
                  else if (type == GeometryType.MULTI_POINT) {
                        finishPart();
                  }
            }
            map.repaint();
      }

      /**
       * updates mouse pos when moving for rubber-band previews
       */
      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {
            updateMousePos(e, map);
            map.repaint();
      }

      /**
       * convert screen px -> world coords.
       * world = (screen - offset) / scale
       */
      private void updateMousePos(MouseEvent e, MapPanel map) {
            double worldX = (e.getX() - map.getOffsetX()) / map.getScale();
            double worldY = (e.getY() - map.getOffsetY()) / map.getScale();
            currentMousePos = new Point2D.Double(worldX, worldY);
      }

      // --- geometry build logic ---

      /**
       * turns currentPoints into a “part”
       * saved in parts + elemInfoList
       */
      private void finishPart() {
            if (currentPoints.isEmpty())
                  return;

            GeometryType type = settings.getSelectedType();
            boolean isHole = settings.isHole(); // if drawing interior ring

            double[] ordinates;

            // circle logic
            // oracle spatial doesn’t store center+radius normally
            // so we approximate circle w/ 3 pts on circumference
            if (type == GeometryType.CIRCLE) {
                  Point2D center = currentPoints.get(0);
                  double radius = center.distance(currentMousePos);

                  // 3 pts: right, top, left
                  ordinates = new double[] {
                              center.getX() + radius, center.getY(),
                              center.getX(), center.getY() + radius,
                              center.getX() - radius, center.getY()
                  };

                  // ElemInfo: 1003 or 2003 ext/int poly
                  // interpretation 4 == circle by 3 pts
                  addElemInfo(isHole ? 2003 : 1003, 4);

            } else {
                  // line / poly / point
                  int size = currentPoints.size();

                  // poly needs closing loop (repeat first point)
                  if (type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON) {
                        size++;
                  }

                  ordinates = new double[size * 2];
                  for (int i = 0; i < currentPoints.size(); i++) {
                        ordinates[i * 2] = currentPoints.get(i).getX();
                        ordinates[i * 2 + 1] = currentPoints.get(i).getY();
                  }

                  // close poly
                  if (type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON) {
                        ordinates[(size - 1) * 2] = currentPoints.get(0).getX();
                        ordinates[(size - 1) * 2 + 1] = currentPoints.get(0).getY();

                        // polygon elemInfo: 1003 ext or 2003 hole, interpretation 1 straight lines
                        addElemInfo(isHole ? 2003 : 1003, 1);
                  }
                  // point or multipoint
                  else if (type == GeometryType.POINT || type == GeometryType.MULTI_POINT) {
                        addElemInfo(1, 1);
                  }
                  // line
                  else {
                        addElemInfo(2, 1);
                  }
            }

            parts.add(ordinates);
            currentPoints.clear();
      }

      /**
       * adds elemInfo triplet into list
       *
       * @param etype element type
       * @param interpretation how to interpret it
       */
      private void addElemInfo(int etype, int interpretation) {
            // calc offset 1-based
            int offset = 1;
            for (double[] part : parts) {
                  offset += part.length;
            }
            elemInfoList.add(offset);
            elemInfoList.add(etype);
            elemInfoList.add(interpretation);
      }

      /**
       * completes entire geometry,
       * builds JGeometry and calls callback
       */
      public void finishGeometry() {
            // nothing drawn
            if (parts.isEmpty() && currentPoints.isEmpty())
                  return;

            // if user forgot double click, finish last part
            if (!currentPoints.isEmpty())
                  finishPart();
            if (parts.isEmpty())
                  return;

            // flatten parts into one ordinates array
            int totalOrds = 0;
            for (double[] part : parts)
                  totalOrds += part.length;

            double[] allOrdinates = new double[totalOrds];
            int idx = 0;
            for (double[] part : parts) {
                  System.arraycopy(part, 0, allOrdinates, idx, part.length);
                  idx += part.length;
            }

            // elemInfo list -> array
            int[] elemInfo = new int[elemInfoList.size()];
            for (int i = 0; i < elemInfoList.size(); i++)
                  elemInfo[i] = elemInfoList.get(i);

            // decide gtype (oracle spatial codes)
            int gtype = 2000; // default/unknown
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
                        break;
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

            // create JGeometry, SRID=0 for now
            JGeometry geom = new JGeometry(gtype, 0, elemInfo, allOrdinates);

            // send it
            if (onComplete != null) {
                  onComplete.accept(geom);
            }

            // reset for next drawing
            reset();
      }

      // --- rendering ---

      /**
       * draws geometry currently being created.
       * called by MapPanel paintComponent
       *
       * two phases:
       * 1. finished parts already drawn (like whole rings or lines)
       * 2. active part being drawn incl rubber band preview line
       */
      @Override
      public void render(Graphics2D g) {
            g.setColor(Color.BLUE);
            g.setStroke(new BasicStroke(2.0f));

            // --- phase 1: finished parts ---
            // go thru parts list
            // use elemInfo to know if it’s line vs circle

            int elemInfoIdx = 0; // tracks triplets (offset,etype,interpretation)
            for (int i = 0; i < parts.size(); i++) {
                  double[] ords = parts.get(i);

                  // 3rd val in triplet = interpretation
                  int interpretation = 1; // default straight line
                  if (elemInfoIdx + 2 < elemInfoList.size()) {
                        interpretation = elemInfoList.get(elemInfoIdx + 2);
                  }
                  elemInfoIdx += 3;

                  if (interpretation == 4) {
                        // drawing a circle
                        // oracle circle = 3 pts on circumference
                        // reconstruct center & radius to draw ellipse

                        if (ords.length >= 4) {
                              double x1 = ords[0];
                              double y1 = ords[1];
                              double x2 = ords[2];
                              double y2 = ords[3];

                              // center & radius
                              double cx = x2;
                              double cy = y1;
                              double r = Math.abs(x1 - x2);

                              g.draw(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                        }
                  } else {
                        // lines/polys, connect pts
                        for (int j = 0; j < ords.length - 2; j += 2) {
                              g.draw(new Line2D.Double(ords[j], ords[j + 1], ords[j + 2], ords[j + 3]));
                        }
                  }
            }

            // --- phase 2: active part ---
            if (!currentPoints.isEmpty()) {
                  // draw lines between clicked pts
                  for (int i = 0; i < currentPoints.size() - 1; i++) {
                        Point2D p1 = currentPoints.get(i);
                        Point2D p2 = currentPoints.get(i + 1);
                        g.draw(new Line2D.Double(p1, p2));
                  }

                  // rubber band to mouse
                  if (currentMousePos != null) {
                        Point2D last = currentPoints.get(currentPoints.size() - 1);

                        if (settings.getSelectedType() == GeometryType.CIRCLE) {
                              // preview circle
                              double r = last.distance(currentMousePos);
                              g.draw(new Ellipse2D.Double(last.getX() - r, last.getY() - r, 2 * r, 2 * r));
                        } else {
                              // preview line
                              g.draw(new Line2D.Double(last, currentMousePos));
                        }
                  }

                  // draw small dots at vertices
                  double r = 3.0;
                  for (Point2D p : currentPoints) {
                        g.fill(new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r));
                  }
            }
      }
}
