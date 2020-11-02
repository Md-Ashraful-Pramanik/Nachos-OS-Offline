package nachos.threads;

import nachos.machine.Machine;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator2 {
    /**
     * Allocate a new communicator.
     */
    int word;
    Condition speakerCondition;
    Condition listenerCondition;
    Lock speakerLock;
    Lock listenerLock;

    public Communicator2() {
        this.speakerLock = new Lock();
        this.speakerCondition = new Condition(speakerLock);
        this.listenerLock = new Lock();
        this.listenerCondition = new Condition(listenerLock);
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
        listenerLock.acquire();
        listenerCondition.wake();
        listenerLock.release();

        speakerLock.acquire();
        speakerCondition.sleep();
        this.word = word;

        listenerLock.acquire();
        listenerCondition.wake();
        listenerLock.release();

        speakerLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {
        speakerLock.acquire();
        speakerCondition.wake();
        speakerLock.release();

        listenerLock.acquire();
        listenerCondition.sleep();
        int word = this.word;
        listenerLock.release();

        return word;
    }
}
