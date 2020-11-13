#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"


int main()
{
    char** args;
    printf("---------------INSIDE Testcase.c---------------\n");
    
    printf("---------------EXEC echo.coff---------------\n");
    char *execArgs[3]={"test","hello","google"};
    int echoPId = exec("echo.coff",3,execArgs);

    printf("---------------EXEC UNHANDLED syscall.coff---------------\n");
    exec("syscall.coff",0,args);

    printf("---------------EXEC halt.coff---------------\n");
    exec("halt.coff",0,args);
    printf("---------------MACHINE NOT HALTING---------------\n");
    
    printf("---------------EXEC matmult.coff---------------\n");
    int matmalPId = exec("matmult.coff",0,args);

    printf("---------------EXEC sort.coff---------------\n");
    int sortPId = exec("sort.coff",0,args);

    printf("---------------WAIT for matmult FINISH---------------\n");
    int status = 0;
    status = join(matmalPId, &status);
    printf("---------------JOINING from matmult.coff---------------\n");

    printf("---------------WAIT for sort FINISH---------------\n");
    status = 0;
    status = join(sortPId, &status);
    printf("---------------JOINING from sort.coff---------------\n");

    printf("---------------WAIT for echo to FINISH---------------\n");
    status = 0;
    status = join(echoPId, &status);
    printf("---------------JOINING from echo.coff---------------\n");

    printf("---------------TRY TO EXEC matmult.coff Again---------------\n");
    int matmalPId2 = exec("matmult.coff",0,args);

    printf("---------------WAIT for matmult FINISH---------------\n");
    int status2 = 0;
    status2 = join(matmalPId2, &status2);
    printf("---------------JOINING from matmult.coff---------------\n");
    printf("---------------matmult.coff exit with status %d---------------\n", status2);

    printf("---------------Done Testcase.c---------------\n");
    return 0;
}