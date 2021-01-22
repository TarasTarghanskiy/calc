package com.implemica;

import com.implemica.view.View;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main {


  public static void main(String[] args) {
    Platform.startup(() -> {
      try {
        new View().start(new Stage());
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}
