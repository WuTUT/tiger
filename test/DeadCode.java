class DeadCode {
  public static void main(String[] a) {
    System.out.println(new Doit().doit());
  }
}

class Doit {
  public int doit() {
    int a;
    if (true)
      System.out.println(1);
    else
      System.out.println(0);
    while (false) {
      System.out.println(1);
      System.out.println(1);
    }
    if (1 < 2)
      System.out.println(3);
    else
      System.out.println(4);
    if (false) {
      System.out.println(5);
    } else {
      System.out.println(6);
      System.out.println(7);
      if (false) {
        System.out.println(8);
      } else {
        System.out.println(9);
      }
      while (2 < 1) {
        System.out.println(1);
      }
      System.out.println(10);
    }
    return 0;
  }
}
