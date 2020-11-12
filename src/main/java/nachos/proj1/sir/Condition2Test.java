package nachos.proj1.sir;

import nachos.threads.Communicator;
import nachos.threads.KThread;

class Condition2Test {

    public Condition2Test() {
        com = new Communicator();
    }

    public void performTest() {

        System.out.println("testing for task 2 & 4 initiated");
        System.out.println("--------------------------------");

        KThread l1 = new KThread(new Condition2Test.Listener(1, com)).setName("listener thread 1");
        KThread l2 = new KThread(new Condition2Test.Listener(2, com)).setName("listener thread 2");

        KThread s1 = new KThread(new Condition2Test.Speaker(1, com)).setName("speaker thread 1");
        KThread s2 = new KThread(new Condition2Test.Speaker(2, com)).setName("speaker thread 2");
        KThread s3 = new KThread(new Condition2Test.Speaker(3, com)).setName("speaker thread 3");

        l1.fork();
        l2.fork();
        s1.fork();
        s2.fork();
        s3.fork();

        l1.join();
        l2.join();
        s1.join();
        s2.join();
        s3.join();

        System.out.println("-----------------------------------------------");
        System.out.println("testing for task 2 & 4 finished");
        System.out.println("-----------------------------------------------");

    }

    private static class Listener implements Runnable {

        Listener(int which, Communicator com) {
            this.which = which;
            this.com = com;
        }

        public void run() {
            for (int i = 0; i < 3; i++) {
                KThread.yield();
                com.listen();
                KThread.yield();
            }
        }

        private int which;
        private Communicator com;
    }

    private static class Speaker implements Runnable {

        Speaker(int which, Communicator com) {
            this.which = which;
            this.com = com;
        }

        public void run() {
            for (int i = 0; i < 2; i++) {
                KThread.yield();
                com.speak(i);
                KThread.yield();
            }
        }

        private int which;
        private Communicator com;
    }

    Communicator com;

}
