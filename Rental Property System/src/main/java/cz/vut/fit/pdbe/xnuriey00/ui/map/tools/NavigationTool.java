package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.event.MouseEvent;

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
            // probably select the property. ignore for now
      }

      @Override
      public void onMouseMoved(MouseEvent e, MapPanel map) {
      }
}
