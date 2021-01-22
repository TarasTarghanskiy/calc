package com.implemica.model.operation;

import java.math.BigDecimal;

@FunctionalInterface
/**
 * represents operations of calculator with two parameters
 */
public interface Operator {
  BigDecimal modelExecution(BigDecimal a, BigDecimal b);
}
