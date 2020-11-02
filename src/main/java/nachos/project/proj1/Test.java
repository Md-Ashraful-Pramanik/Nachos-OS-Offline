package nachos.project.proj1;

import nachos.threads.Alarm;
import nachos.threads.Communicator;
import nachos.threads.KThread;

import java.util.Random;

public class Test {
    public static void runTest() {
        TestInterface test = getTestClass(4);
        if(test != null)
            test.startTesting();
    }

    public static TestInterface getTestClass(int taskNo){
        switch (taskNo){
            case 1: return new JoinTest();
            case 3: return new WaitUntilTest();
            case 4: return new CommunicatorTest();
        }

        return null;
    }
}

