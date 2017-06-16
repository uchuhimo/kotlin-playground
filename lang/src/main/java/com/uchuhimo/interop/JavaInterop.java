package com.uchuhimo.interop;

public class JavaInterop {
  public static void main(String[] args) {
    System.out.println(Test.constField);
    System.out.println(Test.jvmField);
    System.out.println(Test.Companion.getField());
    System.out.println(TopLevel.SecondLevel.field);
    System.out.println(Config.TopLevel.SecondLevel.field);
    System.out.println(Test.TopLevel.SecondLevel.field);
    System.out.println(Test.Config.TopLevel.SecondLevel.field);
    System.out.println(new Test().new InnerConfig().new TopLevel().new SecondLevel().getField());
  }
}
