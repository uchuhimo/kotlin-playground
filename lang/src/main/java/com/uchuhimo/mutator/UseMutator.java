package com.uchuhimo.mutator;

public class UseMutator {
  public static void main(String[] args) {
    final PersonInKotlin person = new PersonInKotlin();
    person.setName("test");
    System.out.println(person.getName());
    person.setDeceased(true);
    System.out.println(person.isDeceased());
  }
}
