package com.implemica.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CalcModel {
  private static final MathContext CEILING = new MathContext(10100, RoundingMode.CEILING);
  private static final MathContext HALF_UP = new MathContext(10100, RoundingMode.HALF_UP);

  private CalcModel() {
  }

  public static BigDecimal exponentOfTwo(BigDecimal value) {
    return (value.pow(2, HALF_UP));
  }

  public static BigDecimal squareRootOfTwo(BigDecimal value) {
    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new NumberFormatException("Invalid input");
    }
    return (value.sqrt(HALF_UP));
  }

  public static BigDecimal negate(BigDecimal value) {
    return (value.negate(HALF_UP));
  }

  public static BigDecimal oneDividedByX(BigDecimal value) {
    return (division(BigDecimal.ONE, value));
  }

  public static BigDecimal addition(BigDecimal firstValue, BigDecimal secondValue) {
    return (firstValue.add(secondValue, CEILING));
  }

  public static BigDecimal subtraction(BigDecimal firstValue, BigDecimal secondValue) {
    return (firstValue.subtract(secondValue, CEILING));
  }

  public static BigDecimal multiplication(BigDecimal firstValue, BigDecimal secondValue) {
    return (firstValue.multiply(secondValue, CEILING));
  }

  public static BigDecimal division(BigDecimal firstValue, BigDecimal secondValue) {
    if (secondValue.compareTo(BigDecimal.ZERO) == 0) {
      throw new NumberFormatException("Cannot divide by zero");
    }
    return (firstValue.divide(secondValue, CEILING));
  }

  public static BigDecimal percent(BigDecimal firstValue, BigDecimal secondValue) {
    if (secondValue.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return (multiplication(firstValue, secondValue.movePointLeft(2)));
  }
}
