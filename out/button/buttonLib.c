#include "buttonLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Option_Boolean createNone_Option_Boolean() {
  Option_Boolean opt = {.defined = false};
  return opt;
}

Option_Boolean createSome_Option_Boolean(bool val) {
  Option_Boolean opt = {.val = val, .defined = true};
  return opt;
}

Tuple2_Boolean_Boolean create_Tuple2_Boolean_Boolean(bool _1, bool _2) {
  Tuple2_Boolean_Boolean prod = {._1 = _1, ._2 = _2};
  return prod;
}

Tuple2_Tuple2_Boolean_Boolean_Boolean create_Tuple2_Tuple2_Boolean_Boolean_Boolean(Tuple2_Boolean_Boolean _1, bool _2) {
  Tuple2_Tuple2_Boolean_Boolean_Boolean prod = {._1 = _1, ._2 = _2};
  return prod;
}

bool equals_Tuple2_Boolean_Boolean(Tuple2_Boolean_Boolean left, Tuple2_Boolean_Boolean right) {
  return (left._1 == right._1) && (left._2 == right._2);
}

cJSON* serialize_Option_Boolean(Option_Boolean opt) {
  if (opt.defined) return cJSON_CreateBool(opt.val); else return cJSON_CreateNull();
}