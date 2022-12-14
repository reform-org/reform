#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef BUTTON_LIB
#define BUTTON_LIB

typedef struct {
  bool val;
  bool defined;
} Option_Boolean;

typedef struct {
  bool _1;
  bool _2;
} Tuple2_Boolean_Boolean;

typedef struct {
  Tuple2_Boolean_Boolean _1;
  bool _2;
} Tuple2_Tuple2_Boolean_Boolean_Boolean;

Option_Boolean createNone_Option_Boolean();

Option_Boolean createSome_Option_Boolean(bool val);

Tuple2_Boolean_Boolean create_Tuple2_Boolean_Boolean(bool _1, bool _2);

Tuple2_Tuple2_Boolean_Boolean_Boolean create_Tuple2_Tuple2_Boolean_Boolean_Boolean(Tuple2_Boolean_Boolean _1, bool _2);

bool equals_Tuple2_Boolean_Boolean(Tuple2_Boolean_Boolean left, Tuple2_Boolean_Boolean right);

cJSON* serialize_Option_Boolean(Option_Boolean opt);

#endif