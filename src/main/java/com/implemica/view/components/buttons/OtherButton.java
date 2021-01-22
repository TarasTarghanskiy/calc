package com.implemica.view.components.buttons;

import com.implemica.model.operation.CalcOther;
import javafx.beans.NamedArg;
import javafx.scene.control.Button;
import lombok.Getter;

public class OtherButton extends Button implements CalcButton {

  @Getter
  private CalcOther type;
  @Getter
  private boolean errorAllowed;

  public OtherButton(@NamedArg("type") CalcOther type, @NamedArg("errorAllowed") boolean errorAllowed) {
    this.type = type;
    this.errorAllowed = errorAllowed;
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
