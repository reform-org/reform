#include "metaBundleTestMain.h"
#include "metaBundleTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include "hashmap.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

int main(int argc, char* argv[]) {
  metaBundleTest_startup();
  metaBundleTest_transaction_local(createNone_Option_String(), createNone_Option_Int());
  release_Array_Int(arraySignal, false);
  release_Map_String_String(foldResult, false);
  return 0;
}