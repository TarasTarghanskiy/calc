package com.implemica.view.components.buttons;

import com.implemica.model.operation.CalcOperator;
import javafx.scene.control.Button;
import javafx.beans.NamedArg;
import lombok.Getter;

public class OperatorButton extends Button implements CalcButton {

  @Getter
  private CalcOperator operator;
  @Getter
  private boolean errorAllowed;

  public OperatorButton(@NamedArg("operator") CalcOperator operator, @NamedArg("errorAllowed") boolean errorAllowed) {
    this.operator = operator;
    this.errorAllowed = errorAllowed;
  }

  @Override
  public String toString() {
    return operator.toString();
  }
}
