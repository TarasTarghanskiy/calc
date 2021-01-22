package com.implemica.model;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CalcModelTest extends Assert {

  private static final DecimalFormat COMPARING_FORMAT = new DecimalFormat("0.#################E0");
  private static final BigDecimal NUM = new BigDecimal("0.0000000000000001");


  private void assertAddition(BigDecimal expected, BigDecimal first, BigDecimal second) {
    assertEquals(expected.stripTrailingZeros(), CalcModel.addition(first, second).stripTrailingZeros());
  }

  private void additionTests(Object resultValue, Object firstValue, Object secondValue) {
    BigDecimal res = new BigDecimal(String.valueOf(resultValue));
    BigDecimal first = new BigDecimal(String.valueOf(firstValue));
    BigDecimal second = new BigDecimal(String.valueOf(secondValue));

    assertAddition(res, first, second);
    assertAddition(res.add(NUM), first.add(NUM), second);
    assertAddition(res.add(NUM), first, second.add(NUM));
    assertAddition(res.subtract(NUM), first.subtract(NUM), second);
    assertAddition(res.subtract(NUM), first, second.subtract(NUM));
  }

  @Test
  void additionTesting() {
    additionTests(0.00001, 0.0000068324827834, 0.0000031675172166);
    additionTests(0.00001, 0.0000212493482394, -0.0000112493482394);

    additionTests(0.0001, 0.0000429823758427, 0.0000570176241573);
    additionTests(0.0001, 0.0003129489218491, -0.0002129489218491);

    additionTests(0.001, 0.0013325482358273, -0.0003325482358273);
    additionTests(0.001, 0.0004263762378463, 0.0005736237621537);

    additionTests(0.01, 0.00156345435, 0.00843654565);
    additionTests(0.01, 0.0761234612534612, -0.0661234612534612);

    additionTests(0.1, 0.05, 0.05);
    additionTests(0.1, 0.6, -0.5);

    additionTests(0, 5, -5);
    additionTests(0, -3.5, 3.5);

    additionTests(10, 9, 1);
    additionTests(10, 23, -13);
    additionTests(10, 7.5, 2.5);
    additionTests(10, 10.5, -0.5);

    additionTests(100, 97, 3);
    additionTests(100, 560, -460);
    additionTests(100, 86.12457346736298, 13.87542653263702);
    additionTests(100, 230.4843573487598, -130.4843573487598);

    additionTests(1000, 458, 542);
    additionTests(1000, 1001, -1);
    additionTests(1000, 960.4, 39.6);
    additionTests(1000, 1467.3, -467.3);
  }

  // уникальніе от калькултора
}