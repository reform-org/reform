#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef ROTARYENC_LIB
#define ROTARYENC_LIB

typedef struct {
  int val;
  bool defined;
} Option_Int;

Option_Int createNone_Option_Int();

Option_Int createSome_Option_Int(int val);

cJSON* serialize_Option_Int(Option_Int opt);

#endif