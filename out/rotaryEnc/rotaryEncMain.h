#include "rotaryEncLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef ROTARYENC_MAIN
#define ROTARYENC_MAIN

void rotaryEnc_startup();

void rotaryEnc_transaction_local(cJSON* json, Option_Int rawRotaryEncoder);

#endif