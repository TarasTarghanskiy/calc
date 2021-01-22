package com.implemica.controller;

import static com.implemica.util.constant.CalcConstant.OVERFLOW;

import com.implemica.util.constant.CalcConstant;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class OutputFormat {

  private OutputFormat() {
  }

  private static MathContext outputRounding = CalcConstant.HALF_UP_16;

  public static String outputFormat(BigDecimal resultNumber, String separator) {

    resultNumber = resultNumber.stripTrailingZeros();

    if (resultNumber.scale() > 10100) {
      resultNumber = resultNumber.round(new MathContext(16, RoundingMode.HALF_UP));
    }


    if (resultNumber.abs().compareTo(new BigDecimal("9.9999999999999995E+9999")) > 0) {
      throw new NumberFormatException(OVERFLOW);
    }

    if (resultNumber.compareTo(BigDecimal.ZERO) != 0 && resultNumber.abs().compareTo(new BigDecimal("1E-9999")) < 0) {
      throw new NumberFormatException(OVERFLOW);
    }

    if (resultNumber.scale() <= -169) {
      outputRounding = CalcConstant.HALF_DOWN_16;
    } else if (resultNumber.scale() >= -21) {
      outputRounding = CalcConstant.HALF_UP_16;
    }

    int test;

    resultNumber = resultNumber.round(new MathContext(20, RoundingMode.DOWN)).stripTrailingZeros();

    int round;
    test = resultNumber.scale() - resultNumber.precision();
    if (test >= 3) {
      round = resultNumber.scale() - (resultNumber.scale() - 16);
    } else if (test > 0) {
      round = resultNumber.precision() - (resultNumber.scale() - 16);
    } else {
      round = 16;
    }

    resultNumber = resultNumber.round(new MathContext(round, outputRounding.getRoundingMode())).stripTrailingZeros();

    test = (resultNumber.scale() - resultNumber.precision());

    if ((resultNumber.scale() < 0 && Math.abs(resultNumber.scale()) + resultNumber.precision() > 16)) {
      return toExponentialForm(resultNumber);
    }
    if (resultNumber.scale() > 16 && test >= 3) {
      return toExponentialForm(resultNumber);
    }

    return outputFormat(resultNumber.stripTrailingZeros().toPlainString(), separator);
  }

  private static String toExponentialForm(BigDecimal number) {
    number = number.round(new MathContext(16, RoundingMode.DOWN)).stripTrailingZeros();
    int num = number.precision() - 1 - number.scale();
    String result = number
        .movePointLeft(num)
        .stripTrailingZeros()
        .toPlainString()
        .replace(".", ",");
    if (num != 0) {
      return result + "e" + (num > 0 ? "+" + num : num);
    }
    return result;
  }

  public static String outputFormat(String output, String separator) {
    int position;
    if (output.contains(".")) {
      output = output.replace(".", "!.");
      position = output.lastIndexOf(".");
    } else {
      output += "!/";
      position = output.length() - 1;
    }

    while (position > 4) {
      String replacement = output.substring(position - 4, position);
      output = output.replaceFirst(replacement, "!" + replacement);
      position -= 3;
    }

    output = output.replace("!.", ",").replace("!/", "").replace("!", separator).replace("- ", "-");

    if (output.charAt(output.length() - 1) != ',') {
      while (output.contains(",") && output.length() > 1 && output.charAt(output.length() - 1) == '0' || output.charAt(output.length() - 1) == ',') {
        output = output.substring(0, output.length() - 1);
      }
    }

    return output;
  }
}
