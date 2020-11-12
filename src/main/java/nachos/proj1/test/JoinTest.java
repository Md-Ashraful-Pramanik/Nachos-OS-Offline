package nachos.proj1.test;
import nachos.threads.KThread;

public class JoinTest implements TestInterface {

    @Override
    public void startTesting(){
        System.out.println("\n\n************IN JOIN TEST*******************");
        KThread thread = new KThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    print(i);
                }
            }
        }).setName("Testing thread 1");

        KThread thread2 = new KThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    print(i);
                }
            }
        }).setName("Testing thread 2");

        thread.fork();
        KThread.yield();
        thread2.fork();
        System.out.println("Waiting for joining other thread.");
        KThread.yield();
        thread.join();
        thread2.join();
        System.out.println("************FINISH JOIN TEST*******************\n\n");
    }

    public void print(int i){
        KThread.yield();
        System.out.println("In Test "+KThread.currentThread().toString()+" "+i);
        KThread.yield();
    }
}
