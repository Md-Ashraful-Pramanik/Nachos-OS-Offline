package nachos.proj1.test;
import nachos.proj1.test.TestInterface;
import nachos.threads.Alarm;
import nachos.threads.KThread;

public class WaitUntilTest implements TestInterface {

    @Override
    public void startTesting(){
        System.out.println("\n\n************IN WAIT UNTIL TEST*******************");
        Alarm alarm = new Alarm();

        KThread thread = new KThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Waiting until 2000 "+KThread.currentThread().toString());
                alarm.waitUntil(2000);
                System.out.println("Finished waiting "+KThread.currentThread().toString());
            }
        }).setName("Thread 1");

        KThread thread2 = new KThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Waiting until 1000 "+KThread.currentThread().toString());
                alarm.waitUntil(1000);
                System.out.println("Finished waiting "+KThread.currentThread().toString());
            }
        }).setName("Thread 2");

        thread.fork();
        thread2.fork();
        System.out.println("Waiting for joining other thread.");
        thread.join();
        System.out.println("Thread 1 joined.");
        thread2.join();
        System.out.println("Thread 2 joined.");
        System.out.println("************COMPLETE WAIT UNTIL TEST*******************\n\n");
    }
}
