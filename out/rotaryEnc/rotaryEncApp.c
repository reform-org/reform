#include "rotaryEncMain.h"
#include "rotaryEncLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

dyad_Stream* clientStream;

void onAccept(dyad_Event* e) {
  clientStream = e->remote;
}

int main(int argc, char* argv[]) {
  if (argc < 2) {
    printf("Listen port expected as command line argument\n");
    return 1;
  }
  
  rotaryEnc_startup();
  dyad_init();
  
  dyad_Stream* s = dyad_newStream();
  dyad_addListener(s, DYAD_EVENT_ACCEPT, onAccept, NULL);
  dyad_listen(s, atoi(argv[1]));
  
  while (dyad_getStreamCount() > 0) {
    cJSON* outputMsg = cJSON_CreateArray();
    rotaryEnc_transaction_local(outputMsg, createNone_Option_Int());
    if (clientStream != NULL && dyad_getState(clientStream) == DYAD_STATE_CONNECTED) dyad_writef(clientStream, "%s\n", cJSON_Print(outputMsg));
    cJSON_Delete(outputMsg);
    dyad_update();
  }
  
  dyad_shutdown();
  return 0;
}