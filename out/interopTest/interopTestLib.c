#include "interopTestLib.h"
#include <string.h>
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

Option_String createNone_Option_String() {
  Option_String opt = {.defined = false};
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

Option_String createSome_Option_String(char* val) {
  Option_String opt = {.val = val, .defined = true};
  return opt;
}

Option_Int deserialize_Option_Int(cJSON* json) {
  if (cJSON_IsNull(json)) return createNone_Option_Int(); else return createSome_Option_Int((json)->valueint);
}

Option_String deserialize_Option_String(cJSON* json) {
  if (cJSON_IsNull(json)) return createNone_Option_String(); else return createSome_Option_String(deserialize_String(json));
}

char* deserialize_String(cJSON* json) {
  int length = strlen((json)->valuestring);
  char* copy = (char*) calloc(length, sizeof(char));
  strcpy(copy, (json)->valuestring);
  return copy;
}

cJSON* serialize_Option_Boolean(Option_Boolean opt) {
  if (opt.defined) return cJSON_CreateBool(opt.val); else return cJSON_CreateNull();
}

cJSON* serialize_Option_Int(Option_Int opt) {
  if (opt.defined) return cJSON_CreateNumber(opt.val); else return cJSON_CreateNull();
}