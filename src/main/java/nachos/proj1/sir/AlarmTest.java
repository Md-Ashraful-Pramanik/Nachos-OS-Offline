package nachos.proj1.sir;

import nachos.machine.Machine;
import nachos.threads.Alarm;
import nachos.threads.KThread;

class AlarmTest {

    private static class AlarmTestRunnable implements Runnable {

        AlarmTestRunnable(long time, Alarm alarm) {
            this.time = time;
            this.alarm = alarm;
        }

        public void run() {
            System.out.println(KThread.currentThread().getName() + " rings at " +
                    Machine.timer().getTime());
            alarm.waitUntil(time);
            System.out.println(KThread.currentThread().getName() + " rings at " +
                    Machine.timer().getTime());
        }

        private final long time;
        private final Alarm alarm;

    }

    public void performTest() {

        System.out.println("testing for task 3 initiated");
        System.out.println("-------------------------------");

        long time1 = 900000;
        long time2 = 1000000;
        long time3 = 600000;

        Alarm alarm = new Alarm();

        // create threads for each aTest object
        KThread t1 = new KThread(new AlarmTest.AlarmTestRunnable(time1, alarm)).setName("Alarm thread 1");
        KThread t2 = new KThread(new AlarmTest.AlarmTestRunnable(time2, alarm)).setName("Alarm thread 2");
        KThread t3 = new KThread(new AlarmTest.AlarmTestRunnable(time3, alarm)).setName("Alarm thread 3");

        // run threads with alarms
        //System.out.println("here"+Machine.timer().getTime());
        alarm.waitUntil(time1);
        //System.out.println("started "+Machine.timer().getTime());
        t2.fork();
        alarm.waitUntil(time2);
        t1.fork();
        alarm.waitUntil(time3);
        t3.fork();

        KThread.yield();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("-----------------------------------------------");
        System.out.println("testing for task 3 finished");
        System.out.println("-----------------------------------------------");
    }
}
