#include "metaBundleTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include "hashmap.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

void add_Map_String_String(Map_String_String set, char* elem) {
  update_Map_String_String(set, elem, elem);
}

Option_Array_String createNone_Option_Array_String() {
  Option_Array_String opt = {.defined = false, .refCount = (int*) calloc(1, sizeof(int))};
  return opt;
}

Option_Int createNone_Option_Int() {
  Option_Int opt = {.defined = false};
  return opt;
}

Option_String createNone_Option_String() {
  Option_String opt = {.defined = false};
  return opt;
}

Option_Tuple2_String_String createNone_Option_Tuple2_String_String() {
  Option_Tuple2_String_String opt = {.defined = false};
  return opt;
}

Option_Array_String createSome_Option_Array_String(Array_String val) {
  Option_Array_String opt = {.val = retain_Array_String(val), .defined = true, .refCount = (int*) calloc(1, sizeof(int))};
  return opt;
}

Option_Int createSome_Option_Int(int val) {
  Option_Int opt = {.val = val, .defined = true};
  return opt;
}

Option_String createSome_Option_String(char* val) {
  Option_String opt = {.val = val, .defined = true};
  return opt;
}

Option_Tuple2_String_String createSome_Option_Tuple2_String_String(Tuple2_String_String val) {
  Option_Tuple2_String_String opt = {.val = val, .defined = true};
  return opt;
}

Array_Int create_Array_Int(int length, ...) {
  Array_Int arr = {.data = (int*) calloc(length, sizeof(int)), .length = length, .refCount = (int*) calloc(1, sizeof(int))};
  va_list argp;
  va_start(argp, length);
  for (int i = 0; i < length; ++i) {
    arr.data[i] = va_arg(argp, int);
  }
  va_end(argp);
  return arr;
}

Array_String create_Array_String(int length, ...) {
  Array_String arr = {.data = (char**) calloc(length, sizeof(char*)), .length = length, .refCount = (int*) calloc(1, sizeof(int))};
  va_list argp;
  va_start(argp, length);
  for (int i = 0; i < length; ++i) {
    arr.data[i] = va_arg(argp, char*);
  }
  va_end(argp);
  return arr;
}

Map_String_String create_Map_String_String() {
  Map_String_String map = {.data = hashmap_new(), .refCount = (int*) calloc(1, sizeof(int))};
  return map;
}

Tuple2_Option_String_Option_String create_Tuple2_Option_String_Option_String(Option_String _1, Option_String _2) {
  Tuple2_Option_String_Option_String prod = {._1 = _1, ._2 = _2};
  return prod;
}

Tuple2_String_String create_Tuple2_String_String(char* _1, char* _2) {
  Tuple2_String_String prod = {._1 = _1, ._2 = _2};
  return prod;
}

int deepCopyIterator_Map_String_String(any_t item, char* key, any_t data) {
  map_t map = (map_t) item;
  char** valuePointer = (char**) data;
  char** valuePointerCopy = (char**) malloc(sizeof(char*));
  *valuePointerCopy = *valuePointer;
  char* keyCopy = (char*) malloc(strlen(key) * sizeof(char*));
  strcpy(keyCopy, key);
  hashmap_put(map, keyCopy, (any_t) valuePointerCopy);
  return MAP_OK;
}

Array_Int deepCopy_Array_Int(Array_Int arr) {
  Array_Int copy = {.data = (int*) calloc(arr.length, sizeof(int)), .length = arr.length, .refCount = (int*) calloc(1, sizeof(int))};
  for (int i = 0; i < arr.length; ++i) copy.data[i] = arr.data[i];
  return copy;
}

Map_String_String deepCopy_Map_String_String(Map_String_String map) {
  Map_String_String copy = create_Map_String_String();
  hashmap_iterate(map.data, &deepCopyIterator_Map_String_String, (any_t) copy.data);
  return copy;
}

int equalsIterator_Map_String_String(any_t item, char* key, any_t data) {
  char* value = *((char**) data);
  map_t other = (map_t) item;
  char*** otherValue = (char***) malloc(sizeof(char**));
  
  int status = hashmap_get(other, key, (any_t*) otherValue);
  int ret;
  if (status == MAP_OK && strcmp(value, **otherValue) == 0) ret = MAP_OK; else ret = MAP_MISSING;
  
  free(otherValue);
  return ret;
}

bool equals_Map_String_String(Map_String_String left, Map_String_String right) {
  return hashmap_iterate(left.data, &equalsIterator_Map_String_String, (any_t) right.data) == MAP_OK && hashmap_iterate(right.data, &equalsIterator_Map_String_String, (any_t) left.data) == MAP_OK;
}

int printSetIterator_Map_String_String(any_t item, char* key, any_t data) {
  int* counterPointer = (int*) item;
  char** elemPointer = (char**) data;
  if (*counterPointer > 0) printf(", ");
  printf("\"%s\"", *elemPointer);
  ++(*counterPointer);
  return MAP_OK;
}

void printSet_Map_String_String(Map_String_String set) {
  printf("Set(");
  int counter = 0;
  hashmap_iterate(set.data, &printSetIterator_Map_String_String, (any_t) &counter);
  printf(")");
}

void print_Array_Int(Array_Int arr) {
  printf("[");
  for (int i = 0; i < arr.length; ++i) {
    if (i > 0) printf(", ");
    printf("%d", arr.data[i]);
  }
  printf("]");
}

void print_Tuple2_String_String(Tuple2_String_String rec) {
  printf("(");
  {
    printf("\"%s\"", rec._1);
    printf(", ");
    printf("\"%s\"", rec._2);
  }
  printf(")");
}

void release_Array_Int(Array_Int rec, bool keep_with_zero) {
  --(*rec.refCount);
  if (*rec.refCount <= 0 && !keep_with_zero) {
    free(rec.refCount);
    free(rec.data);
  }
}

void release_Array_String(Array_String rec, bool keep_with_zero) {
  --(*rec.refCount);
  if (*rec.refCount <= 0 && !keep_with_zero) {
    free(rec.refCount);
    free(rec.data);
  }
}

void release_Map_String_String(Map_String_String rec, bool keep_with_zero) {
  --(*rec.refCount);
  if (*rec.refCount <= 0 && !keep_with_zero) {
    free(rec.refCount);
    hashmap_free(rec.data);
  }
}

void release_Option_Array_String(Option_Array_String rec, bool keep_with_zero) {
  --(*rec.refCount);
  if (*rec.refCount <= 0 && !keep_with_zero) {
    if (rec.defined) release_Array_String(rec.val, false);
    free(rec.refCount);
  }
}

Array_Int retain_Array_Int(Array_Int rec) {
  ++(*rec.refCount);
  return rec;
}

Array_String retain_Array_String(Array_String rec) {
  ++(*rec.refCount);
  return rec;
}

Map_String_String retain_Map_String_String(Map_String_String rec) {
  ++(*rec.refCount);
  return rec;
}

Option_Array_String retain_Option_Array_String(Option_Array_String rec) {
  ++(*rec.refCount);
  return rec;
}

Option_Tuple2_String_String someTuple(char* a, char* b) {
  return createSome_Option_Tuple2_String_String(create_Tuple2_String_String(a, b));
}

void update_Map_String_String(Map_String_String map, char* key, char* value) {
  cJSON* keyJSON = cJSON_CreateString(key);
  char* keyString = cJSON_Print(keyJSON);
  cJSON_Delete(keyJSON);
  char** valuePointer = (char**) malloc(sizeof(char*));
  *valuePointer = value;
  hashmap_put(map.data, keyString, (any_t) valuePointer);
}