#include "photoelectricLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

#ifndef PHOTOELECTRIC_MAIN
#define PHOTOELECTRIC_MAIN

bool enabled;

void photoelectric_startup();

void photoelectric_transaction_local(cJSON* json, Option_Boolean rawPhotoelec);

void photoelectric_transaction_remote(cJSON* json, Option_Int toggleSelection);

#endif