package nachos.proj1.sir;

import nachos.threads.KThread;

class JoinTest1 {

    public JoinTest1() {
    }

    public void performTest() {

        System.out.println("testing for task 1 initiated");
        System.out.println("-------------------------------");

        KThread t0 = new KThread(new JoinTest1.PingTest(0)).setName("forked thread 0");
        System.out.println("forked thread 0 and joining...");
        t0.fork();
        t0.join();
        System.out.println("joined with thread 0");

        new KThread(new JoinTest1.PingTest(1)).setName("forked thread 1").fork();
        new KThread(new JoinTest1.PingTest(2)).setName("forked thread 2").fork();
        new JoinTest1.PingTest(0).run();

        System.out.println();

        KThread t1 = new KThread(new JoinTest1.PingTest(1)).setName("forked thread 1");
        KThread t2 = new KThread(new JoinTest1.PingTest(2)).setName("forked thread 2");

        t1.fork();
        t2.fork();

        t1.join();
        t2.join();

        new JoinTest1.PingTest(0).run();

        System.out.println("-----------------------------------------------");
        System.out.println("testing for task 1 finished");
        System.out.println("-----------------------------------------------");
    }

    private static class PingTest implements Runnable {

        PingTest(int which) {
            this.which = which;
        }

        public void run() {
            for (int i = 0; i < 5; i++) {

                System.out.println("*** thread " + which + " looped "
                        + i + " times");
                KThread.yield();

            }
        }

        private final int which;

    }

}
