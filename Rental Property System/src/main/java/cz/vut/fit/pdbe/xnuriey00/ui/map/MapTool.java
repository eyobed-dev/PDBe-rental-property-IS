package cz.vut.fit.pdbe.xnuriey00.ui.map;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public interface MapTool {
      void onMousePressed(MouseEvent e, MapPanel map);

      void onMouseDragged(MouseEvent e, MapPanel map);

      void onMouseReleased(MouseEvent e, MapPanel map);

      void onMouseClicked(MouseEvent e, MapPanel map);

      void onMouseWheelMoved(MouseWheelEvent e, MapPanel map);

      void onMouseMoved(MouseEvent e, MapPanel map);
}
