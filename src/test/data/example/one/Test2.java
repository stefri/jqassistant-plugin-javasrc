package example.one;                                                // 1

import java.util.Date;                                              // 2


class ClassLocalTest extends MyOtherClass {
  
    @Overrride
    public void fooBar(String text) {
      System.out.println(text);
    }
  
}


public class Test1 {                                                // 3

    private static Date date1;                                      // 4

    static {                                                        // 5
        date1 = new Date();                                         // 6
        System.out.println(new Date());                             // 7
    }
  

    private int a, b = 2, c = 3;                                    // 8 + 9 + 10
    private int myInt;                                              // 11

    public Test1() {                                                // 12
        this(5);                                                    // 13
    }
    
    public Test1(int myInt) {                                       // 14
        this.myInt = myInt;                                         // 15
    }

    public static void main(String[] args) {                        // 16
        int three = 3;                                              // 17
        double myRand;                                              // 18
        myRand = Math.random();                                     // 19 + 20
    
        System.out.println("Hello World");                          // 21
        if (three > 2) {                                            // 22
            System.out.println("treffer");                          // 23
        }
    }
    
    private String genString(String a, String b) {                  // 24
        return a+b;                                                 // 25
    }

}
