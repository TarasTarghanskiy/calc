package com.implemica.util.constant;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class CalcConstant {

  private CalcConstant() {
  }

  public static final Pattern DIGIT_PATTERN = Pattern.compile("^(-)?[1-9]$");
  public static final MathContext HALF_UP_16 = new MathContext(16, RoundingMode.HALF_UP);
  public static final MathContext HALF_DOWN_16 = new MathContext(16, RoundingMode.HALF_DOWN);

  public static final String OVERFLOW = "Overflow";
}
