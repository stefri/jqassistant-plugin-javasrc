package foo.bar.baz;

import java.util.List;

public abstract class FooClass {

    private volatile String fooBar = "foo bar baz";
    private int a = 5, b = 10;

    public static final String getFoo() {
        String myFoo = "my foo";
        return fooBar + myFoo;
    }
}
