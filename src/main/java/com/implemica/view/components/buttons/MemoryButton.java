package com.implemica.view.components.buttons;

import com.implemica.model.operation.CalcMemory;
import javafx.beans.NamedArg;
import javafx.scene.control.Button;
import lombok.Getter;

public class MemoryButton extends Button implements CalcButton {
  @Getter
  private CalcMemory type;

  public MemoryButton(@NamedArg("type") CalcMemory type) {
    super.setDisable(type.isDefaultEnabling());
    this.type = type;
  }



  @Override
  public String toString() {
    return type.toString();
  }

  @Override
  public boolean isErrorAllowed() {
    return false;
  }

  public void setDefaultEnabling() {
    super.setDisable(type.isDefaultEnabling());
  }
}
