#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef CO2_LIB
#define CO2_LIB

typedef struct {
  int val;
  bool defined;
} Option_Int;

typedef struct {
  bool _1;
  int _2;
} Tuple2_Boolean_Int;

typedef struct {
  bool val;
  bool defined;
} Option_Boolean;

Option_Boolean createNone_Option_Boolean();

Option_Int createNone_Option_Int();

Option_Boolean createSome_Option_Boolean(bool val);

Option_Int createSome_Option_Int(int val);

Tuple2_Boolean_Int create_Tuple2_Boolean_Int(bool _1, int _2);

Option_Int deserialize_Option_Int(cJSON* json);

cJSON* serialize_Option_Boolean(Option_Boolean opt);

#endif