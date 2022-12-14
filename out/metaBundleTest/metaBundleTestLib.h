#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include "hashmap.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

#ifndef METABUNDLETEST_LIB
#define METABUNDLETEST_LIB

typedef struct {
  int* data;
  int length;
  int* refCount;
} Array_Int;

typedef struct {
  int val;
  bool defined;
} Option_Int;

typedef struct {
  char* val;
  bool defined;
} Option_String;

typedef struct {
  char** data;
  int length;
  int* refCount;
} Array_String;

typedef struct {
  Array_String val;
  bool defined;
  int* refCount;
} Option_Array_String;

typedef struct {
  char* _1;
  char* _2;
} Tuple2_String_String;

typedef struct {
  Tuple2_String_String val;
  bool defined;
} Option_Tuple2_String_String;

typedef struct {
  Option_String _1;
  Option_String _2;
} Tuple2_Option_String_Option_String;

typedef struct {
  map_t data;
  int* refCount;
} Map_String_String;

void add_Map_String_String(Map_String_String set, char* elem);

Option_Array_String createNone_Option_Array_String();

Option_Int createNone_Option_Int();

Option_String createNone_Option_String();

Option_Tuple2_String_String createNone_Option_Tuple2_String_String();

Option_Array_String createSome_Option_Array_String(Array_String val);

Option_Int createSome_Option_Int(int val);

Option_String createSome_Option_String(char* val);

Option_Tuple2_String_String createSome_Option_Tuple2_String_String(Tuple2_String_String val);

Array_Int create_Array_Int(int length, ...);

Array_String create_Array_String(int length, ...);

Map_String_String create_Map_String_String();

Tuple2_Option_String_Option_String create_Tuple2_Option_String_Option_String(Option_String _1, Option_String _2);

Tuple2_String_String create_Tuple2_String_String(char* _1, char* _2);

int deepCopyIterator_Map_String_String(any_t item, char* key, any_t data);

Array_Int deepCopy_Array_Int(Array_Int arr);

Map_String_String deepCopy_Map_String_String(Map_String_String map);

int equalsIterator_Map_String_String(any_t item, char* key, any_t data);

bool equals_Map_String_String(Map_String_String left, Map_String_String right);

int inc;

int printSetIterator_Map_String_String(any_t item, char* key, any_t data);

void printSet_Map_String_String(Map_String_String set);

void print_Array_Int(Array_Int arr);

void print_Tuple2_String_String(Tuple2_String_String rec);

void release_Array_Int(Array_Int rec, bool keep_with_zero);

void release_Array_String(Array_String rec, bool keep_with_zero);

void release_Map_String_String(Map_String_String rec, bool keep_with_zero);

void release_Option_Array_String(Option_Array_String rec, bool keep_with_zero);

Array_Int retain_Array_Int(Array_Int rec);

Array_String retain_Array_String(Array_String rec);

Map_String_String retain_Map_String_String(Map_String_String rec);

Option_Array_String retain_Option_Array_String(Option_Array_String rec);

Option_Tuple2_String_String someTuple(char* a, char* b);

void update_Map_String_String(Map_String_String map, char* key, char* value);

#endif