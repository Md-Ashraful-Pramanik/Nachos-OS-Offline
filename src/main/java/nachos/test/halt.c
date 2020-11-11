/* halt.c
 *	Simple program to test whether running a user program works.
 *	
 *	Just do a "syscall" that shuts down the OS.
 *
 * 	NOTE: for some reason, user programs with global data structures 
 *	sometimes haven't worked in the Nachos environment.  So be careful
 *	out there!  One option is to allocate data structures as 
 * 	automatics within a procedure, but if you do this, you have to
 *	be careful to allocate a big enough stack to hold the automatics!
 */

#include "syscall.h"

int main()
{
    printf("halt started!\n");
    printf("halt prints a line.\n");
    printf("halt prints another line.\n");

    int processId;
    char execArgs[3][10]={"test","hello","google"};
    processId = exec("demo.coff",3,execArgs);


    printf("Process Id: %d\n",processId);
    int status1;
    int k = join(processId, &status1);

    printf("halt tries to halt Nachos\n");
    accept(0);
    halt();

    printf("Nachos not halted!\n");
    return 0;
    /* not reached */
}
