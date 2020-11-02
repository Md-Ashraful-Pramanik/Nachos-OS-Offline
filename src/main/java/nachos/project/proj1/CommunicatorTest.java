package nachos.project.proj1;

import nachos.threads.Communicator;
import nachos.threads.KThread;

import java.util.Random;

public class CommunicatorTest implements TestInterface{
    public Communicator communicator = new Communicator();
    public Random random = new Random();

    @Override
    public void startTesting() {
        //System.out.println("Hello world.");

        KThread l1 = new KThread(new Runnable() {
            @Override
            public void run() {
                listenerMethod();
            }
        }).setName("Listener Thread 1");
        KThread l2 = new KThread(new Runnable() {
            @Override
            public void run() {
                listenerMethod();
            }
        }).setName("Listener Thread 2");

        KThread s1 = new KThread(new Runnable() {
            @Override
            public void run() {
                speakerMethod();
            }
        }).setName("Speaker Thread 1");
        KThread s2 = new KThread(new Runnable() {
            @Override
            public void run() {
                speakerMethod();
            }
        }).setName("Speaker Thread 2");
        KThread s3 = new KThread(new Runnable() {
            @Override
            public void run() {
                speakerMethod();
            }
        }).setName("Speaker Thread 3");

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
    }

    public void listenerMethod() {
        for (int i = 0; i < 3; i++) {
            KThread.yield();
            communicator.listen();
            KThread.yield();
        }
    }

    public void speakerMethod() {
        for (int i = 0; i < 3; i++) {
            KThread.yield();
            communicator.speak(random.nextInt(100));
            KThread.yield();
        }
    }
}
