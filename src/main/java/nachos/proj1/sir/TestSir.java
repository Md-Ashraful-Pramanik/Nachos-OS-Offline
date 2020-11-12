package nachos.proj1.sir;

import nachos.proj1.sir.AlarmTest;
import nachos.proj1.sir.JoinTest1;

public class TestSir {
    public static void selfTest() {
        new JoinTest1().performTest();
        new Condition2Test().performTest();
        new AlarmTest().performTest();
    }

}

