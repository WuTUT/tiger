#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "control.h"
#include "command-line.h"
extern void Tiger_heap_init(int);
int Tiger_main();
int main(int argc, char **argv)
{
  // Lab 4, exercise 13:
  // You should add some command arguments to the generated executable
  // to control the behaviour of your Gimple garbage collector.
  // For instance, you should run:
  //   $ a.out @tiger -heapSize 1 @
  // to set the Java heap size to 1K. Or you can run
  //   $ a.out @tiger -gcLog @
  // to generate the log (which is discussed in this exercise).
  // You can use the offered function in file "control.c"
  // and "command-line.c"
  // Your code here:
  CommandLine_doarg(argc, argv);
  // initialize the Java heap
  //printf("init heapsize is %d\n", Control_heapSize);
  //printf("islog : ");
  //if (logflag)
  //{
  //printf("true\n");
  //}
  //else
  //{
  //printf("false\n");
  //}
  Tiger_heap_init(Control_heapSize);

  // enter Java code...
  Tiger_main();
}
