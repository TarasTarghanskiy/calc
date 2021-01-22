package com.implemica.util.resize;

import java.util.HashMap;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * The class for javaFX stage resizing
 */
public class ResizeMode {

  // windows screen width
  private static final double SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();
  // windows screen height
  private static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
  // targeted stage
  private static Stage stage;
  /**
   * pack values for resizing
   */
  // stage width
  private static double storedWidth;
  // stage height
  private static double storedHeight;
  // the mouse x position relative to a scene
  private static double storedSceneX;
  // the mouse y position relative to a scene
  private static double storedSceneY;
  // the mouse x position relative to a screen
  private static double storedScreenX;
  // the mouse y position relative to a screen
  private static double storedScreenY;


  // stage x position relative to a screen
  private static double storedXWindowsMode;
  // stage y position relative to a screen
  private static double storedYWindowsMode;
  // stage width
  private static double storedWidthWindowsMode;
  // stage height
  private static double storedHeightWindowsMode;

  @Getter
  // check, is stage size equals to screen size or false
  private static boolean isMaximized;

  // saves events variants for stage resizing
  private static final Map<Cursor, EventHandler<MouseEvent>> LISTENER = new HashMap<>();

  /**
   * this block declares all variants of stage resizing (left, right, up-right, etc.)
   */
  static {
    LISTENER.put(Cursor.NW_RESIZE, event -> {
      double newWidth = storedWidth - (event.getScreenX() - storedScreenX);
      double newHeight = storedHeight - (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setY(event.getScreenY() - storedSceneY);
        stage.setHeight(newHeight);
      }
      if (newWidth > stage.getMinWidth()) {
        stage.setX(event.getScreenX() - storedSceneX);
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.NE_RESIZE, event -> {
      double newWidth = storedWidth - (event.getScreenX() - storedScreenX);
      double newHeight = storedHeight + (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setHeight(newHeight);
      }
      if (newWidth > stage.getMinWidth()) {
        stage.setX(event.getScreenX() - storedSceneX);
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.SW_RESIZE, event -> {
      double newWidth = storedWidth + (event.getScreenX() - storedScreenX);
      double newHeight = storedHeight - (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setHeight(newHeight);
        stage.setY(event.getScreenY() - storedSceneY);
      }
      if (newWidth > stage.getMinWidth()) {
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.SE_RESIZE, event -> {
      double newWidth = storedWidth + (event.getScreenX() - storedScreenX);
      double newHeight = storedHeight + (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setHeight(newHeight);
      }
      if (newWidth > stage.getMinWidth()) {
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.E_RESIZE, event -> {
      double newWidth = storedWidth - (event.getScreenX() - storedScreenX);
      if (newWidth > stage.getMinWidth()) {
        stage.setX(event.getScreenX() - storedSceneX);
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.W_RESIZE, event -> {
      double newWidth = storedWidth + (event.getScreenX() - storedScreenX);
      if (newWidth > stage.getMinWidth()) {
        stage.setWidth(newWidth);
      }
    });

    LISTENER.put(Cursor.N_RESIZE, event -> {
      double newHeight = storedHeight - (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setY(event.getScreenY() - storedSceneY);
        stage.setHeight(newHeight);
      }
    });

    LISTENER.put(Cursor.S_RESIZE, event -> {
      double newHeight = storedHeight + (event.getScreenY() - storedScreenY);
      if (newHeight > stage.getMinHeight()) {
        stage.setHeight(newHeight);
      }
    });

    LISTENER.put(Cursor.DEFAULT, null);
  }

  public ResizeMode() {
  }

  /**
   * Makes input stage resizeable
   *
   * @param stage that we wanted to make resizeable
   */
  public static void makeResizable(Stage stage) {
    ResizeMode.stage = stage;

    // this event changes cursor view after the mouse moving
    stage.getScene().addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
      double sx = e.getSceneX();
      double sy = e.getSceneY();

      boolean rTrigger = sx < stage.getScene().getWidth() && sx > stage.getScene().getWidth() - 9;
      boolean uTrigger = (sy < stage.getScene().getHeight() && sy >= stage.getScene().getHeight() - 10);
      boolean lTrigger = (sx >= 0 && sx <= 9) || (uTrigger && sx >= 0 && sx <= 16);
      boolean dTrigger = ((lTrigger || rTrigger) && sy >= 0 && sy <= 7) || ((sx < 10 || sx >= 16) && sy > 0 && sy <= 7);

      if (lTrigger && dTrigger) {
        stage.getScene().setCursor(Cursor.NW_RESIZE);
      } else if (lTrigger && uTrigger) {
        stage.getScene().setCursor(Cursor.NE_RESIZE);
      } else if (rTrigger && dTrigger) {
        stage.getScene().setCursor(Cursor.SW_RESIZE);
      } else if (rTrigger && uTrigger) {
        stage.getScene().setCursor(Cursor.SE_RESIZE);
      } else if (lTrigger) {
        stage.getScene().setCursor(Cursor.E_RESIZE);
      } else if (rTrigger) {
        stage.getScene().setCursor(Cursor.W_RESIZE);
      } else if (dTrigger) {
        stage.getScene().setCursor(Cursor.N_RESIZE);
      } else if (uTrigger) {
        stage.getScene().setCursor(Cursor.S_RESIZE);
      } else {
        stage.getScene().setCursor(Cursor.DEFAULT);
      }

    });

    // this event saves the coordinates of a mouse after a mouse pressing
    stage.getScene().setOnMousePressed(e -> {
      if (e.getButton() == MouseButton.PRIMARY) {
        storedSceneX = e.getSceneX();
        storedSceneY = e.getSceneY();
        storedScreenX = e.getScreenX();
        storedScreenY = e.getScreenY();
        storedWidth = stage.getWidth();
        storedHeight = stage.getHeight();
        stage.getScene().setOnMouseDragged(LISTENER.get(stage.getScene().getCursor()));
      }
    });
  }


  /**
   * This method changes stage size to screen size
   */
  public static void switchWindowsMode() {
    // if stage size equals screen now
    if (isMaximized) {
      stage.setY(storedYWindowsMode);
      stage.setX(storedXWindowsMode);
      stage.setWidth(storedWidthWindowsMode);
      stage.setHeight(storedHeightWindowsMode);

      isMaximized = false;

    } else {
      storedXWindowsMode = stage.getX();
      storedYWindowsMode = stage.getY();
      storedWidthWindowsMode = stage.getWidth();
      storedHeightWindowsMode = stage.getHeight();

      isMaximized = true;

      stage.setY(0);
      stage.setX(-10);
      stage.setWidth(SCREEN_WIDTH + 20);
      stage.setHeight(SCREEN_HEIGHT + 10);
      stage.setMaximized(true);
    }
  }
}
