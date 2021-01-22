package com.implemica.view.components.buttons;

import javafx.scene.control.Button;

public class NumberButton extends Button implements CalcButton {
  @Override
  public boolean isErrorAllowed() {
    return true;
  }

  @Override
  public String toString() {
    return this.getText();
  }
}
