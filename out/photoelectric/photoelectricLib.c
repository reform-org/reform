#include "photoelectricLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Option_Boolean createNone_Option_Boolean() {
  Option_Boolean opt = {.defined = false};
  return opt;
}

Option_Int createNone_Option_Int() {
  Option_Int opt = {.defined = false};
  return opt;
}

Option_Boolean createSome_Option_Boolean(bool val) {
  Option_Boolean opt = {.val = val, .defined = true};
  return opt;
}

Option_Int createSome_Option_Int(int val) {
  Option_Int opt = {.val = val, .defined = true};
  return opt;
}

Tuple2_Boolean_Int create_Tuple2_Boolean_Int(bool _1, int _2) {
  Tuple2_Boolean_Int prod = {._1 = _1, ._2 = _2};
  return prod;
}

Option_Int deserialize_Option_Int(cJSON* json) {
  if (cJSON_IsNull(json)) return createNone_Option_Int(); else return createSome_Option_Int((json)->valueint);
}

cJSON* serialize_Option_Boolean(Option_Boolean opt) {
  if (opt.defined) return cJSON_CreateBool(opt.val); else return cJSON_CreateNull();
}