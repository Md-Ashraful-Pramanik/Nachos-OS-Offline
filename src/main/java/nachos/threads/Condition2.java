package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {

    private Lock conditionLock;
    private ThreadQueue waitQueue =
            ThreadedKernel.scheduler.newThreadQueue(false);

    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        /*******************start************************/
        //V
        conditionLock.release();//not by me
        boolean status=Machine.interrupt().disable();
        waitQueue.waitForAccess(KThread.currentThread());
        KThread.sleep();
        Machine.interrupt().restore(status);
        conditionLock.acquire();//not by me
        /*******************end***************************/
    }
    /*
    A condition variable uses a lock and a queue to store all the values of the condition variable. It has a list of threads that line up to check for this condition variable.
Whenever sleep() is called, a value is added to the queue of values and waits for the lock to be released by any other thread who might have acquired it. If the value remains unchanged, the thread will wait for the currently running thread and go to sleep afterwards.
Whenever wake is called on a condition variable, firstly it is checked whether the lock is held by the current thread. If the queue of values is not empty, then the first value is removed and if there is a thread waiting in line, it will be marked as ready to be run.
wakeAll() method of the condition variable calls wake() until the queue of values is empty.
     */
    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        /****************start***********************/
        //P
        boolean status=Machine.interrupt().disable();
        KThread thread=waitQueue.nextThread();
        if(thread!=null){
            thread.ready();
        }
        Machine.interrupt().restore(status);
        /*************end*********************/
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        /*****************start***********************/
        boolean status=Machine.interrupt().disable();
        KThread thread=waitQueue.nextThread();
        while(thread!=null) {
            thread.ready();
            thread=waitQueue.nextThread();
        }
        Machine.interrupt().restore(status);
        /****************end*************************/
    }

}
