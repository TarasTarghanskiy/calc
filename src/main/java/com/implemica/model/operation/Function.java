package com.implemica.model.operation;

import java.math.BigDecimal;

@FunctionalInterface
/**
 * represents operations of calculator with a single parameter
 */
public interface Function {
  BigDecimal modelExecution(BigDecimal a);
}
