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

    Condition speakerCondition;
    Condition listenerCondition;
    int word;
    boolean speakerExist;
    boolean listenerExist;
    Lock lock;

    public Communicator() {
        lock = new Lock();
        this.speakerCondition = new Condition(lock);
        this.listenerCondition = new Condition(lock);
        speakerExist = false;
        listenerExist = false;
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

        while (speakerExist)
            speakerCondition.sleep();
        speakerExist = true;

        while (!listenerExist)
            speakerCondition.sleep();

        this.word = word;
        System.out.println(KThread.currentThread().toString() + " spoke " + word);
        listenerCondition.wake();
        speakerExist = false;

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

        while (listenerExist)
            listenerCondition.sleep();
        listenerExist = true;

        while (!speakerExist)
            listenerCondition.sleep();

        speakerCondition.wake();
        listenerCondition.sleep();

        int word = this.word;
        System.out.println(KThread.currentThread().toString() + " listen " + word);

        listenerExist = false;
        lock.release();

        return word;
    }
}
