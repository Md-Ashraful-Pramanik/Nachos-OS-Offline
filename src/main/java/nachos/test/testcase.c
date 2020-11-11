#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
    char[][] args;
    printf("---------------INSIDE Testcase.c---------------\n");
    
    printf("---------------EXEC echo.coff---------------\n");
    char execArgs[3][10]={"test","hello","google"};
    int echoPId = exec("echo.coff",3,execArgs);

    printf("---------------EXEC UNHANDLED syscall.coff---------------\n");
    exec("syscall.coff",0,args);

    printf("---------------EXEC halt.coff---------------\n");
    exec("halt.coff",0,args);
    printf("---------------MACHINE NOT HALTING---------------\n");
    
    printf("---------------EXEC matmul.coff---------------\n");
    int matmalPId = exec("matmul.coff",0,args);
    
    printf("---------------EXEC sort.coff---------------\n");
    int sortPId = exec("sort.coff",0,args);
    
    printf("---------------WAIT for matmul FINISH---------------\n");
    int status = 0;
    status = join(matmalPId, &status);
    printf("---------------JOINING from matmul.coff---------------\n");
    printf("---------------MATMAL RETURN %d-----------------------\n", status);
    
    printf("---------------WAIT for sort FINISH---------------\n");
    status = 0;
    status = join(matmalPId, &status);
    printf("---------------JOINING from sort.coff---------------\n");

    printf("---------------Done Testcase.c---------------\n");
    return 0;
}