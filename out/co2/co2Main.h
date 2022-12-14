#include "co2Lib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef CO2_MAIN
#define CO2_MAIN

bool enabled;

void co2_startup();

void co2_transaction_local(cJSON* json, Option_Int rawCo2);

void co2_transaction_remote(cJSON* json, Option_Int toggleSelection);

#endif