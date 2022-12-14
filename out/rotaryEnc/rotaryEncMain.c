#include "rotaryEncMain.h"
#include "rotaryEncLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Option_Int anonfun_FireDetection_12_41(Option_Int rawRotaryEncoder) {
  return ({
    Option_Int _temp = rawRotaryEncoder;
    Option_Int _res;
    if (_temp.defined) _res = createSome_Option_Int(({
      int _$6 = _temp.val;
      ({
        (_$6 % 3);
      });
    })); else _res = createNone_Option_Int();
    _res;
  });
}

void rotaryEnc_startup() {

}

void rotaryEnc_transaction_local(cJSON* json, Option_Int rawRotaryEncoder) {
  Option_Int selectionInt = {.defined = false};
  
  cJSON* selectionInt_json;
  
  if (rawRotaryEncoder.defined) {
    selectionInt = anonfun_FireDetection_12_41(rawRotaryEncoder);
  }
  
  selectionInt_json = serialize_Option_Int(selectionInt);
  
  cJSON_AddItemToArray(json, selectionInt_json);
}