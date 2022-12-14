#include "rotaryEncLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Option_Int createNone_Option_Int() {
  Option_Int opt = {.defined = false};
  return opt;
}

Option_Int createSome_Option_Int(int val) {
  Option_Int opt = {.val = val, .defined = true};
  return opt;
}

cJSON* serialize_Option_Int(Option_Int opt) {
  if (opt.defined) return cJSON_CreateNumber(opt.val); else return cJSON_CreateNull();
}