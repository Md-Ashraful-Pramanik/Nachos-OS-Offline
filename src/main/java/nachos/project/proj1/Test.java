package nachos.project.proj1;

import nachos.threads.Communicator;
import nachos.threads.KThread;

import java.util.Random;

public class Test {

    public static Communicator communicator = new Communicator();
    public static Random random = new Random();

    public static void main(String[] args) {
        runTest();
    }

    public static void runTest(){
        System.out.println("Hello world.");

        KThread l1 = new KThread(Test::listenerMethod).setName("Listener Thread 1");
        KThread l2 = new KThread(Test::listenerMethod).setName("Listener Thread 2");

        KThread s1 = new KThread(Test::speakerMethod).setName("Speaker Thread 1");
        KThread s2 = new KThread(Test::speakerMethod).setName("Speaker Thread 2");
        KThread s3 = new KThread(Test::speakerMethod).setName("Speaker Thread 3");

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

    public static void listenerMethod(){
        KThread.yield();
        communicator.listen();
        KThread.yield();
    }

    public static void speakerMethod(){
        KThread.yield();
        communicator.speak(random.nextInt(100));
        KThread.yield();
    }
}
