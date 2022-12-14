#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef INTEROPTEST_LIB
#define INTEROPTEST_LIB

typedef struct {
  int val;
  bool defined;
} Option_Int;

typedef struct {
  char* val;
  bool defined;
} Option_String;

typedef struct {
  bool val;
  bool defined;
} Option_Boolean;

Option_Boolean createNone_Option_Boolean();

Option_Int createNone_Option_Int();

Option_String createNone_Option_String();

Option_Boolean createSome_Option_Boolean(bool val);

Option_Int createSome_Option_Int(int val);

Option_String createSome_Option_String(char* val);

Option_Int deserialize_Option_Int(cJSON* json);

Option_String deserialize_Option_String(cJSON* json);

char* deserialize_String(cJSON* json);

cJSON* serialize_Option_Boolean(Option_Boolean opt);

cJSON* serialize_Option_Int(Option_Int opt);

#endif