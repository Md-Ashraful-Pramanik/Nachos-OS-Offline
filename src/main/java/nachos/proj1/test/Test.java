package nachos.proj1.test;

public class Test {
    public static void runTest() {
        for (int i = 1; i <= 3; i++) {
            TestInterface test = getTestClass(i);
            if(test != null)
                test.startTesting();
        }
    }

    public static TestInterface getTestClass(int taskNo){
        switch (taskNo){
            case 1: return new JoinTest();
            case 2: return new WaitUntilTest();
            case 3: return new CommunicatorTest();
        }

        return null;
    }
}

