package com.implemica.view.components.buttons;

import com.implemica.model.operation.CalcFunction;
import javafx.beans.NamedArg;
import javafx.scene.control.Button;
import lombok.Getter;

public class FunctionButton extends Button implements CalcButton {
  @Getter
  private CalcFunction function;
  @Getter
  private boolean errorAllowed;

  public FunctionButton(@NamedArg("function") CalcFunction function, @NamedArg("errorAllowed") boolean errorAllowed) {
    this.function = function;
    this.errorAllowed = errorAllowed;
  }

  @Override
  public String toString() {
    return function.toString();
  }
}
