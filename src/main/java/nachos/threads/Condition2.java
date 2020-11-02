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
    //edited by abser
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
        valueQueue=new LinkedList();

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
        this.value=0;
        valueQueue.add(value);
        conditionLock.release();//not by me
        boolean status=Machine.interrupt().disable();
        if (this.value == 0) {
            waitQueue.waitForAccess(KThread.currentThread());
            KThread.sleep();
        }
        else {
            this.value--;
        }
        Machine.interrupt().restore(status);
        conditionLock.acquire();//not by me
        /*******************end***************************/

    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());


        /****************start***********************/
        //P
        boolean status=Machine.interrupt().disable();
        if (!valueQueue.isEmpty()){
            valueQueue.removeFirst();
            KThread thread=waitQueue.nextThread();
            if(thread!=null){
                thread.ready();
            }else{
                this.value++;
            }
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
        while (!valueQueue.isEmpty()){
            wake();
        }
        /****************end*************************/

    }

    private int value;
    private Lock conditionLock;
    private LinkedList<Integer> valueQueue;
    private ThreadQueue waitQueue =
            ThreadedKernel.scheduler.newThreadQueue(false);
}
