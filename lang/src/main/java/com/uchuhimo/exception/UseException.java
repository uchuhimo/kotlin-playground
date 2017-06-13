package com.uchuhimo.exception;

public class UseException {
  public static void main(String[] args) {
    try {
      ExceptionKt.throwException(true);
    } catch (MyException e) {
      e.printStackTrace();
    }
  }
}
