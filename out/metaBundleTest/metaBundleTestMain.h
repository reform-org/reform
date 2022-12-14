#include "metaBundleTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include "hashmap.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

#ifndef METABUNDLETEST_MAIN
#define METABUNDLETEST_MAIN

Map_String_String foldResult;

int source;

int derived;

Array_Int arraySignal;

void metaBundleTest_startup();

void metaBundleTest_transaction_local(Option_String esource, Option_Int source_param);

#endif