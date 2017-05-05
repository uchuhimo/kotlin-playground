package com.uchuhimo.multiBound;

public class UseMultiBound {
  public static void main(String[] args) {
    PlayKt.<A, A2>test(new A1(), new A2());
  }
}
