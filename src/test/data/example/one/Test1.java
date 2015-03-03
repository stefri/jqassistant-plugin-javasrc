package example.one;     /* special */                              // 1

import java.lang.IllegalArgumentException;
import java.lang.System;
import java.util.Date;                                              // 2

public class Test1 {                                                // 3

    private static Date date1;                                      // 4

    static {                                                        // 5
        date1 = new foo.bar.Date();                                         // 6
        System.out.println(new Date());                             // 7
        date1.after(new Date());
    }


    private int a, b = 2, c = 3;                                    // 8 + 9 + 10
    private int myInt;                                              // 11

    public Test1() {                                                // 12
        this(5);                                                    // 13
    }

    public Test1(int myInt) {                                       // 14
        /*

         Note: very important


         */
        this.myInt = /* inline one */ myInt;                         // 15
    }

    // blubba
    /**
     * Foo bar baz
     * @param args
     */
    public static void main(String[] args) throws MyException {     // 16
        int three = 3;                                              // 17
        double myRand;                                              // 18
        myRand = Math.random();                                     // 19 + 20

        switch(three) {
            case 1:
            case 2:
                three++;
                break;
            case 3:
                three--;
                break;
            default:
                three = 0;
        }

        try {
            if (foo) {
                blubb();
                foo();
            } else {
                blubb();
            }
        } catch (IllegalArgumentException e) {
            blubb();
        } catch (FooBarException e) {
            blubb();
        } finally {
            reader.close();
        }

        if (three > 0) System.out.println("foo");
        else System.out.println("bar");

        while(three > 0) {
            three -= 1;
        }

        for (int i = 0; i < j; i++) {
            for (Item it: items) {
                if (foo && bar) {
                    println(i);
                } else {
                    println(foo);
                    return;
                }
            }
        }

        System.out.println("Hello World" /* inline */);             // 21
        if (three > 2 && foo != bar) {                                            // 22
            System.out.println("treffer");                          // 23
        } else if (three < 10) {
            System.out.println("moo");
        } else {
            System.out.println("blubb");
        }
    }

    private String genString(String a, String b) throws IllegalArgumentException, foo.bar.YourException {    // 24
        // genString comment
        return a+b;                                                 // 25
    }

    private boolean testBoolean(boolean a, boolean b) {
        int a = three > 3 ? 1 : three;
        return a && b || c && d;
    }
}
