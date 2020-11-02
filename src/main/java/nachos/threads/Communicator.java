package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */

    Condition condition;
    int word;
    int speakerCount;
    int listenerCount;
    Lock lock;

    public Communicator() {
        lock = new Lock();
        this.condition = new Condition(lock);
        speakerCount = 0;
        listenerCount = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        speakerCount++;
        while (listenerCount == 0)
            condition.sleep();

        this.word = word;
        System.out.println(KThread.currentThread().toString() + "spoke " + word);
        condition.wake();
        speakerCount--;
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        lock.acquire();
        listenerCount++;
        while (speakerCount == 0){
            condition.sleep();
        }

        condition.wakeAll();
        condition.sleep();
        int word = this.word;
        System.out.println(KThread.currentThread().toString() + "listen " + word);
        listenerCount--;
        lock.release();
        return word;
    }
}
