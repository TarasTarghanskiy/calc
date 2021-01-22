package com.implemica.model.operation;

import lombok.Getter;

public enum CalcMemory {
  MEMORY_CLEAR(true),
  MEMORY_RECALL(true),
  MEMORY_SUBTRACTION(false),
  MEMORY_ADDITION(false),
  MEMORY_STORE(false),
  MEMORY_SHOW(true);

  @Getter
  private final boolean defaultEnabling;

  CalcMemory(boolean defaultEnabling) {
    this.defaultEnabling = defaultEnabling;
  }
}
