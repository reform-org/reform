#include "buttonLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef BUTTON_MAIN
#define BUTTON_MAIN

Tuple2_Boolean_Boolean buttonFilter;

bool buttonFiltered;

void button_startup();

void button_transaction_local(cJSON* json, Option_Boolean rawButton);

#endif