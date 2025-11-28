package cz.vut.fit.pdbe.xnuriey00.ui.map;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class NavigationTool implements MapTool {
      private int lastMouseX;
      private int lastMouseY;

      @Override
      public void onMousePressed(MouseEvent e, MapPanel map) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
      }

      @Override
      public void onMouseDragged(MouseEvent e, MapPanel map) {
            int dx = e.getX() - lastMouseX;
            int dy = e.getY() - lastMouseY;
            map.pan(dx, dy);
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            map.repaint();
      }

      @Override
      public void onMouseReleased(MouseEvent e, MapPanel map) {
      }

      @Override
      public void onMouseClicked(MouseEvent e, MapPanel map) {
            // Optional: Click to center or select
      }

      @Override
      public void onMouseWheelMoved(MouseWheelEvent e, MapPanel map) {
            if (e.getWheelRotation() < 0) {
                  map.zoomIn();
            } else {
                  map.zoomOut();
            }
            map.repaint();
      }

      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {
      }
}
