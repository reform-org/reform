#include "interopTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef INTEROPTEST_MAIN
#define INTEROPTEST_MAIN

void interopTest_startup();

void interopTest_transaction_local(cJSON* json, Option_Int localSource);

void interopTest_transaction_remote(cJSON* json, Option_Int fromScala, Option_String fromScalaString);

#endif