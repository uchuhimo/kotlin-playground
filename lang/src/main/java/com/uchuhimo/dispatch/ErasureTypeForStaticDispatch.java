package com.uchuhimo.dispatch;

public class ErasureTypeForStaticDispatch {
  public interface Trait {

  }

  public static class A implements Trait {

  }

  public static class B implements Trait {

  }

  public static void overload(A a) {
    System.out.println("a");
  }

  public static void overload(B b) {
    System.out.println("b");
  }

  public static void overload(Trait trait) {
    System.out.println("trait");
  }

  public static void erasureTypeFunc(Trait trait) {
    overload(trait);
  }

  public static <T extends Trait> void erasureTypeGenericFunc(T trait) {
    overload(trait);
  }

  public static void main(String[] args) {
    A a = new A();
    B b = new B();
    overload(a);
    erasureTypeFunc(a);
    erasureTypeGenericFunc(b);
  }
}
