package com.implemica.model.operation;

import com.implemica.model.CalcModel;
import java.math.BigDecimal;
import lombok.Getter;

/**
 * this enum represents operators of calculator
 *
 * operator - operation with two parameters
 */
public enum CalcOperator {
  // divide first value by second
  DIVISION("/","\uE94A", CalcModel::division),
  // multiply first value by second
  MULTIPLICATION("*","\uE947", CalcModel::multiplication),
  // a difference between first and second values
  SUBTRACTION("-","\uE949", CalcModel::subtraction),
  // a sum of first and second values
  ADDITION("+","\uE948", CalcModel::addition),
  // represent result of current program operations
  RESULT("=","\ue94e", null);

  @Getter
  // a s/ingle standard symbol that represents operator
  private final String baseSymbol;
  @Getter
  //
  private final String symbol;
  @Getter
  private final Operator operation;

  public BigDecimal modelExecuting(BigDecimal a, BigDecimal b) {
    return operation.modelExecution(a, b);
  }

  CalcOperator(String baseSymbol, String symbol, Operator operation) {
    this.baseSymbol = baseSymbol;
    this.symbol = symbol;
    this.operation = operation;
  }

  public static String replace(String string) {
    for (CalcOperator value : CalcOperator.values()) {
      string = string.replace(" " + value.baseSymbol + " "," " +  value.symbol + " ");
    }
    return string;
  }

  @Override
  public String toString() {
    return baseSymbol;
  }
}
