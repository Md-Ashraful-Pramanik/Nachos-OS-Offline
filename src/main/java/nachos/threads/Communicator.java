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

    public Communicator() {
        Condition condition = new Condition(new Lock());
        this.condition = condition;
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
        boolean status=Machine.interrupt().disable();
        speakerCount++;
        Machine.interrupt().restore(status);
        while (listenerCount == 0)
            condition.sleep();

        status=Machine.interrupt().disable();
        this.word = word;
        Lib.debug('t',KThread.currentThread().toString() + "spoke " + word);
        condition.wake();
        speakerCount--;
        Machine.interrupt().restore(status);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        boolean status=Machine.interrupt().disable();
        listenerCount++;
        Machine.interrupt().restore(status);
        while (speakerCount == 0)
            condition.sleep();

        condition.wakeAll();
        condition.sleep();
        status=Machine.interrupt().disable();
        int word = this.word;
        Lib.debug('t',KThread.currentThread().toString() + "listen " + word);
        listenerCount--;
        Machine.interrupt().restore(status);
        return word;
    }
}
