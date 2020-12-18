#include "stdio.h"
#include "stdlib.h"
#include "syscall.h"


int main()
{
    char** args;
    printf("---------------INSIDE Testcase.c    testing ---------------\n");

    printf("---------------EXEC echo.coff---------------\n");
    int echoPId = exec("echo.coff",0,args);

    printf("---------------EXEC matmult.coff---------------\n");
    int matmalPId = exec("matmult.coff",0,args);

    printf("---------------EXEC sort.coff---------------\n");
    int sortPId = exec("sort.coff",0,args);

    printf("---------------WAIT for matmult FINISH---------------\n");
    int status = 0;
    status = join(matmalPId, &status);
    printf("---------------JOINING from matmult.coff---------------\n");

    printf("---------------Done Testcase.c---------------\n");
    return 0;
}