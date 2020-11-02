package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    //edited by abser
    /*********************start******************/

    private ArrayList<Long>timeList;
    private ArrayList<KThread>threadList;

    /*********************end*******************/

    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        /****************start********************/
        timeList=new ArrayList<>();
        threadList=new ArrayList<>();
        /****************end*********************/

        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() { timerInterrupt(); }
            });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {

        /*********************start*****************/
        long current=Machine.timer().getTime();
        boolean status=Machine.interrupt().disable();
        int i;
        for(i=0;i<timeList.size();i++){
            if(timeList.get(i)<=current){
                threadList.get(i).ready();
            }else{
                break;
            }
        }
        for(int j=0;j<i;j++) {
            threadList.remove(0);
            timeList.remove(0);
        }
	    KThread.currentThread().yield();//provided line
        Machine.interrupt().restore(status);
        /*******************end******************/
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        long wakeTime = Machine.timer().getTime() + x;
        /**********************start****************/
        KThread current=KThread.currentThread();
        boolean status=Machine.interrupt().disable();
        for (int i=timeList.size()-1;i>=0;i--){
            if(timeList.get(i)<=wakeTime){
                timeList.add(i,wakeTime);
                threadList.add(i,current);
                break;
            }
        }
        current.sleep();
        Machine.interrupt().restore(status);
        /*********************end*****************/
        /*while (wakeTime > Machine.timer().getTime())
            KThread.yield();
        */
    }
}
