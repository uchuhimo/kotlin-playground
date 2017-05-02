package com.uchuhimo.typeclass;

//import static com.uchuhimo.typeclass.implement1.ImplementShowWithOverloadKt.log;
import static com.uchuhimo.typeclass.implement2.ImplementShowWithOverload2Kt.log;

import java.util.ArrayList;

public class UseOverloadInPackageInJava {
  public static void main(String[] args) {
    log("test");
    log(new ArrayList<Base>() {
      {
        add(new Base());
        add(new Base());
      }
    });
    log(new ArrayList<Base>() {
      {
        add(new Derived());
        add(new Derived());
      }
    });
  }
}
