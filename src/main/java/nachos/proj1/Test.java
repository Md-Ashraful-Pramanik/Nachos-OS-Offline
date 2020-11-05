package nachos.proj1;

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

