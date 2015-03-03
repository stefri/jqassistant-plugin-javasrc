package example.one;

import java.util.List;
import java.util.ArrayList;

public class Test3 {

    private String foobar = "foobar";

    public static void main(String[] args) {
         System.out.println("Test 1");
         InnerTest1 it1 = new InnerTest1();
         System.out.println(it1.myText());
         AnotherTest1 at1 = new AnotherTest1();
         System.out.println(at1.myText());
    }

    static class InnerTest1 {
        public String myText() {
            return "Inner Test 1";
        }

        public String runDoIt = (new MyInterface() {

            @Override
            public String doIt(String foobar) {
                return foobar.toLowerCase();
            }

        }).doIt(myText());
    }

    interface MyInterface {
        public String doIt(String foobar);
    }
}

class AnotherTest1 {
    public String myText() {
         return "Another Test 1";
    }
}
