package com.implemica.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.implemica.Main;
import com.implemica.view.View;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.util.WaitForAsyncUtils;

import static javafx.scene.Cursor.*;

class CalcControllerTest extends ApplicationTest {

  private Logger log = Logger.getRootLogger();
  private Map<String, Node> keyButtonMap = new HashMap<>();
  private FxRobot robot;
  private Robot awtRobot = new Robot();
  private Stage stage;

  CalcControllerTest() throws AWTException {
  }

  @Start
  public void start(Stage stage) throws Exception {
    this.stage = stage;
    new View().start(stage);
    ((GridPane) stage.getScene().lookup("#numPad")).getChildren().forEach(b -> {

    });
  }

  @Override
  public void init() throws Exception {
    FxToolkit.registerStage(Stage::new);
  }

  void assertCursor(double x, double y, Cursor cursor) {
    awtRobot.mouseMove((int) x, (int) y);
    WaitForAsyncUtils.waitForFxEvents();
    awtRobot.delay(100);
    Assertions.assertEquals(cursor, stage.getScene().getCursor());
  }

  @Test
  void resizeCursorType() throws InterruptedException {
    // first row

    assertCursor(789 - 1, 176, null);
    assertCursor(789, 176, NW_RESIZE);
    assertCursor(789 + 1, 176, NW_RESIZE);

    assertCursor(799 - 1, 176, NW_RESIZE);
    assertCursor(799, 176, DEFAULT);
    assertCursor(799 + 1, 176, DEFAULT);

    assertCursor(1123 - 1, 176, DEFAULT);
    assertCursor(1123, 176, SW_RESIZE);
    assertCursor(1123 + 1, 176, SW_RESIZE);

    // second row

    assertCursor(789 - 1, 177, SW_RESIZE);
    assertCursor(789, 177, NW_RESIZE);
    assertCursor(789 + 1, 177, NW_RESIZE);

    assertCursor(799 - 1, 177, NW_RESIZE);
    assertCursor(799, 177, DEFAULT);
    assertCursor(799 + 1, 177, DEFAULT);

    assertCursor(805 - 1, 177, DEFAULT);
    assertCursor(805, 177, N_RESIZE);
    assertCursor(805 + 1, 177, N_RESIZE);

    assertCursor(1123 - 1, 177, N_RESIZE);
    assertCursor(1123, 177, SW_RESIZE);
    assertCursor(1123 + 1, 177, SW_RESIZE);


  }
  // на ресайз
}