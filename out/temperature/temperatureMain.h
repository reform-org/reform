#include "temperatureLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef TEMPERATURE_MAIN
#define TEMPERATURE_MAIN

bool enabled;

void temperature_startup();

void temperature_transaction_local(cJSON* json, Option_Int rawTemperature);

void temperature_transaction_remote(cJSON* json, Option_Int toggleSelection);

#endif