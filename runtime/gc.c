#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "control.h"
#include <time.h>
static void Tiger_gc();
int gccnt = 0;
// The Gimple Garbage Collector.

//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap
{
  int size;       // in bytes, note that this if for semi-heap size
  char *from;     // the "from" space pointer
  char *fromFree; // the next "free" space in the from space
  char *to;       // the "to" space pointer
  char *toStart;  // "start" address in the "to" space
  char *toNext;   // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init(int heapSize)
{
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
  void *p = malloc(heapSize);
  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
  heap.size = heapSize / 2;
  // #3: initialize the "from" field (with what value?)
  heap.from = p;
  // #4: initialize the "fromFree" field (with what value?)
  heap.fromFree = p;
  // #5: initialize the "to" field (with what value?)
  heap.to = p + heap.size;
  // #6: initizlize the "toStart" field with NULL;
  heap.toStart = NULL;
  // #7: initialize the "toNext" field with NULL;
  heap.toNext = NULL;
  return;
}

// The "__prev" pointer, pointing to the top frame on the GC stack.
// (see part A of Lab 4)
void *__prev = 0;

//===============================================================//
// Object Model And allocation

// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      | vptr      ---|----> (points to the virtual method table)
      |--------------|
      | isObjOrArray | (0: for normal objects)
      |--------------|
      | length       | (this field should be empty for normal objects)
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| v_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1;
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new(void *vtable, int size)
{
  if (heap.fromFree + size + sizeof(void **) * 4 > heap.from + heap.size)
  {
    Tiger_gc();
  }

  if (heap.fromFree + size + sizeof(void **) * 4 > heap.from + heap.size)
  {

    printf("alloc %d bytes but only %d free Out of memory!\n", size + sizeof(void **) * 4, heap.from + heap.size - heap.fromFree);
    exit(-1);
  }

  void *p = heap.fromFree;
  heap.fromFree += size + sizeof(void **) * 4;
  ((void **)p)[0] = vtable;
  ((void **)p)[1] = (void *)0;
  ((void **)p)[2] = (void *)0;
  ((void **)p)[3] = (void *)0;
  memset((void **)p + 4, 0, size);
  ((void **)p)[4] = vtable;
  return (void **)p + 4;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      | vptr         | (this field should be empty for an array)
      |--------------|
      | isObjOrArray | (1: for array)
      |--------------|
      | length       |
      |--------------|
      | forwarding   | 
      |--------------|\
p---->| e_0          | \ //this is vtable     
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1;
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array(int length)
{
  // Your code here:
  if (heap.fromFree + sizeof(int) * (length + 4) > heap.from + heap.size)
  {
    Tiger_gc();
  }

  if (heap.fromFree + sizeof(int) * (length + 4) > heap.from + heap.size)
  {
    printf("alloc %d bytes but only %d free Out of memory!\n", sizeof(int) * (length + 4), heap.from + heap.size - heap.fromFree);
    exit(-1);
  }

  int *p = heap.fromFree;
  heap.fromFree += sizeof(int) * (length + 4);
  memset(p + 4, 0, sizeof(int) * length);
  *(p) = 0;
  *(p + 1) = 1;
  *(p + 2) = length;
  *(p + 3) = 0;
  return p + 4;
}
//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
char *scan;
char *next;

static int intoheap(int *p)
{

  if (p >= heap.to && p < heap.to + heap.size)
    return 1;
  return 0;
}

static int infromheap(int *p)
{
  if (p >= heap.from && p < heap.from + heap.size)
    return 1;
  return 0;
}
static void *forward(int *p)
{
  if (p == 0)
    return p;
  if (infromheap(*(int **)p))
  {

    int **forwardp = (int **)*(int **)p - 1;
    //printf("%p forward\n", forwardp);
    //printf("%p *forwardp \n", *forwardp);
    if (intoheap(*forwardp) == 0)
    {
      // printf("next is %p\n", next);
      //*forwardp = (int *)next;

      *forwardp = (int *)next + 4;
      if (*(p - 3) == 1)
      {
        int length = *(p - 2);
        memcpy(next, p - 4, sizeof(int) * (length + 4));
        next = next + length + 4;
        return *forwardp;
      }
      //printf("%p forwardp \n", *forwardp);
      //printf("%p pis \n", p);
      int **obj = *(int **)p;

      int **vptr = *(obj);
      int **ptrclassmap = *vptr;
      char *classmap = *(ptrclassmap);
      //printf("class map %s\n", classmap);
      int len = strlen(classmap);
      int **copyp = (int **)obj - 4;
      memcpy(next, copyp, sizeof(int **) * 5);
      copyp += 5;
      next = (int **)next + 5;
      for (int i = 0; i < len; i++)
      {
        if (classmap[i] == '1')
        {
          //printf("copy %x \n", *copyp);
          *(int **)next = *copyp;
          //printf("to %x\n", *(int **)next);
          next = (int **)next + 1;
          copyp += 1;
        }
        else
        {
          //printf("copy %x \n", *copyp);
          *(int *)next = *(int *)copyp;
          //printf("to %x\n", *(int *)next);
          next = (int *)next + 1;
          copyp = (int *)copyp + 1;
        }
      }
    }
    return *forwardp;
  }
  return p;
}

static void Tiger_gc()
{
  // Your code here:
  clock_t t;
  t = clock();
  int beforegc_space = heap.from + heap.size - heap.fromFree;
  scan = next = heap.to;
  void **tmpprev = (void **)__prev;
  while (tmpprev != 0)
  {
    //printf("prev 's prev is %p\n", *(int **)tmpprev);
    //printf("prev[1] %p\n", (int **)tmpprev + 1);

    char *argmap = (char *)tmpprev[1];
    int numargs = strlen(argmap);
    int *argp = (int *)tmpprev[2];
    char *localmap = (char *)tmpprev[3];
    for (int i = 0; i < numargs; i++)
    {
      //printf("argmap %d %c\n", i, argmap[i]);
      if (argmap[i] == '1')
      {
        //printf("argvar is %p ,point obj is %p\n", argp, *(int **)argp);
        *((int **)argp) = forward(argp);
        argp = (int **)argp + 1;
      }
      else
      {
        argp = argp + 1;
      }
    }
    int numlocals = strlen(localmap);
    int **localp = (int **)tmpprev + 4;
    //printf("locals num %d\n", numlocals);
    for (int i = 0; i < numlocals; i++)
    {
      //printf("localvar is %p, point obj is %p\n", localp + i, *(localp + i));
      localp[i] = forward(localp[i]);
    }
    //printf("scan %p next %p \n", scan, next);
    tmpprev = (void **)(*(int **)tmpprev);
  }

  while (scan < next)
  {
    // printf("%p scan obj %p\n", scan, (int **)scan + 4);
    int isarray = *((int *)scan + 1);
    if (isarray == 1)
    {
      int length = *((int *)scan + 2);
      scan = scan + sizeof(int) * (4 + length);
      continue;
    }
    int **obj = (int **)scan + 4;
    int **vptr = *obj;
    //printf("%p vptr\n", vptr);
    int **vtable = *vptr;
    char *classmap = *(vtable);
    // printf("class map in scan -next %s\n", classmap);
    int len = strlen(classmap);
    int **sumsz = ((int **)scan + 5);
    for (int i = 0; i < len; i++)
    {
      if (classmap[i] == '1')
      {
        //printf("scan forward %p point obj is %p\n", (int **)scan + i + 5, *((int **)scan + i + 5));
        *((int **)scan + i + 5) = forward((int **)scan + i + 5);
      }
      sumsz += 1;
    }
    scan = sumsz;
  }
  //printf("beforegc %d\n", beforegc_space);
  //printf("next is %p\n", next);
  char *from = heap.from;
  heap.from = heap.to;

  heap.fromFree = next;
  heap.to = from;
  int aftergc_space = heap.from + heap.size - heap.fromFree;
  //printf("aftergc %d \n", aftergc_space);
  t = clock() - t;
  if (logflag)
    printf("%d round of GC: %.7lf s ,collected %d bytes\n", ++gccnt, ((double)t) / CLOCKS_PER_SEC, aftergc_space - beforegc_space);
}
