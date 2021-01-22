package com.implemica.view;

import com.implemica.Main;
import com.implemica.controller.Controller;
import com.implemica.util.resize.ResizeMode;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class View extends Application {
  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(Main.class.getResource("../../scene/main_scene.fxml"));

    Scene scene = new Scene(loader.load());
    scene.setFill(Color.TRANSPARENT);

    stage.setScene(scene);
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setMinWidth(340);
    stage.setMinHeight(510);
    stage.show();
    ((Controller) loader.getController()).init(stage);
    ResizeMode.makeResizable(stage);
  }
}
