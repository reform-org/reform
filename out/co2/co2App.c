#include "co2Main.h"
#include "co2Lib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

void onData(dyad_Event* e) {
  cJSON* json = cJSON_Parse(e->data);
  cJSON* outputMsg = cJSON_CreateArray();
  co2_transaction_remote(outputMsg, deserialize_Option_Int(cJSON_GetArrayItem(json, 0)));
  dyad_writef(e->stream, "%s\n", cJSON_Print(outputMsg));
  cJSON_Delete(outputMsg);
  cJSON_Delete(json);
}

dyad_Stream* clientStream;

void onAccept(dyad_Event* e) {
  dyad_addListener(e->remote, DYAD_EVENT_DATA, onData, NULL);
  clientStream = e->remote;
}

int main(int argc, char* argv[]) {
  if (argc < 2) {
    printf("Listen port expected as command line argument\n");
    return 1;
  }
  
  co2_startup();
  dyad_init();
  
  dyad_Stream* s = dyad_newStream();
  dyad_addListener(s, DYAD_EVENT_ACCEPT, onAccept, NULL);
  dyad_listen(s, atoi(argv[1]));
  
  while (dyad_getStreamCount() > 0) {
    dyad_update();
  }
  
  dyad_shutdown();
  return 0;
}