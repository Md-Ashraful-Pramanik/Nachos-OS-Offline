#include "syscall.h"

int main()
{
    printf("----------------INSIDE UNHANDLED SYSTEM CALLS---------------\n");
    accept(0);
    printf("----------------UNHANDLED SYSTEM CALLS DOES NOT HANDLED BY NACHOS---------------\n");
    return 0;
}
