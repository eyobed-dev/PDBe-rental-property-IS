package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface MapTool {
      void onMousePressed(MouseEvent e, MapPanel map);
      void onMouseDragged(MouseEvent e, MapPanel map);
      void onMouseReleased(MouseEvent e, MapPanel map);
      void onMouseClicked(MouseEvent e, MapPanel map);
      void onMouseMoved(MouseEvent e, MapPanel map);
      //This is everywhere in all tools (zoom in and out) 
      default void onMouseWheelMoved(MouseWheelEvent e, MapPanel map) {
            if (e.getWheelRotation() < 0) {
                  map.zoomIn();
            } else {
                  map.zoomOut();
            }
            map.repaint();
      }
      // Allow tools to draw on the map
      default void render(java.awt.Graphics2D g) {
      }
}
