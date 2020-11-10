#include "syscall.h"

int main()
{
    printf("\n----------INSIDE ERROR------------\n");
    int a,num;
    char buf[30];
    readline(buf, 10);
    printf("ENTER 0:");
    readline(buf, 10);
    num = atoi(buf);
    a=5/num;
    printf("\nUNHANDLED EXCEPTION NOT WORKING\n");
    return 0;
}