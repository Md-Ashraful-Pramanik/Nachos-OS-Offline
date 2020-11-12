#include "stdio.h"
#include "stdlib.h"

int main(int argc, char* argv[])
{
    int i, j, num;
    char buf[30];

    printf("---------------INSIDE ECHO---------------\n");
    printf("----------------PRINTING RECEIVED ARGUMENT-------------\n");
    printf("Received Argument Count: %d\n", argc);
    for(i=0; i<argc; i++){
        printf("%d argument: %s\n", (i+1), argv[i]);
    }
    printf("----------------ARGUMENT PRINT DONE-------------\n");

    printf("Enter a number: ");
    readline(buf, 10);
    num = atoi(buf);
    printf("number is %d\n",num);
    for(i = 0; i < num; i++) {
        for(j = 0; j < i; j++) {
            printf("*");
        }
        printf("\n");
    }
    printf("\n");
   
    printf("Enter any string: ");
    readline(buf, 30);
    printf("INPUT by user : %s\n", buf);
   
    printf("------------CHECKING INVALID READ CALLS--------------\n");
    num = read(3, &buf, 10);
    printf("Return on invalid file descriptor: %d\n", num);
    num = read(0, -12, 10);
    printf("Return on invalid vaddr: %d\n", num);
    num = read(0, &buf, -2);
    printf("Return on invalid size: %d\n", num);
    printf("------------END CHECKING INVALID READ CALLS--------------\n");
   
    printf("\n------------CHECKING INVALID WRITE CALLS--------------\n");
    num = write(3, &buf, 10);
    printf("Return on invalid file descriptor: %d\n", num);
    num = write(0, -12, 10);
    printf("Return on invalid vaddr: %d\n", num);
    num = write(0, &buf, -2);
    printf("Return on invalid size: %d\n", num);
    printf("------------END CHECKING INVALID WRITE CALLS--------------\n");
   
    printf("\n------------CHECKING INVALID JOIN CALLS--------------\n");
    num = join(2, &num);
    printf("Return for join on pid 2 : %d\n", num);
    num = join(3, &num);
    printf("Return for join on pid 3 : %d\n", num);
    printf("\n------------END CHECKING INVALID JOIN CALLS--------------\n");

    printf("\n-------------TRY TO HALT IN ECHO-------------------------\n");
    halt();
    printf("\n-------------IN ECHO NOT HALTING-------------------------\n");
    printf("---------------FINISH ECHO---------------\n");
    return 0;
}