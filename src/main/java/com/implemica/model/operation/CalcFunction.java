package com.implemica.model.operation;

import com.implemica.model.CalcModel;
import java.math.BigDecimal;
import lombok.Getter;

/**
 * this enum represents functions of calculator
 *
 * function - operation with a single parameter
 */
public enum CalcFunction {

  // value sign changing
  ADDITIVE_INVERSE("negate(#)", CalcModel::negate),
  // one by x dividing
  ONE_DIVIDED_BY_X("1/(#)", CalcModel::oneDividedByX),
  // multiplying a value by himself
  EXPONENT_TWO("sqr(#)", CalcModel::exponentOfTwo),
  // square root of value
  SQUARE_ROOT_OF_TWO("√(#)", CalcModel::squareRootOfTwo);

  @Getter
  // the history formula view
  private final String formula;
  // calculator operation with a single parameter
  private final Function function;

  /**
   * This method executes current functions
   * @param a is bigDecimal, that we wanted to change using calculator functions
   * @return result of current function executing
   */
  public BigDecimal modelExecuting(BigDecimal a) {
    return function.modelExecution(a);
  }

  CalcFunction(String formula, Function function) {
    this.formula = formula;
    this.function = function;
  }
}
