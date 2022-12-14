#include "co2Main.h"
#include "co2Lib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

bool anonfun_FireDetection_38_46(bool x$1, int x$2) {
  return ({
    bool _res;
    Tuple2_Boolean_Int _scrutinee = create_Tuple2_Boolean_Int(x$1, x$2);
    
    if (_scrutinee._2 == 0) {
      bool state = _scrutinee._1;
      
      _res = !(state);
    } else if (true) {
      bool state = _scrutinee._1;
      
      _res = state;
    }
    
    _res;
  });
}

Option_Boolean anonfun_FireDetection_43_30(Option_Int rawCo2) {
  return ({
    Option_Int _temp = rawCo2;
    Option_Boolean _res_2;
    if (_temp.defined) _res_2 = createSome_Option_Boolean(({
      int _$9 = _temp.val;
      ({
        (_$9 > 3000);
      });
    })); else _res_2 = createNone_Option_Boolean();
    _res_2;
  });
}

Option_Boolean anonfun_FireDetection_45_7(Option_Boolean co2Filtered, bool enabled) {
  return enabled ? co2Filtered : createSome_Option_Boolean(false);
}

void co2_startup() {
  enabled = true;
}

void co2_transaction_local(cJSON* json, Option_Int rawCo2) {
  Option_Boolean co2Filtered = {.defined = false};
  Option_Boolean co2Bool = {.defined = false};
  
  cJSON* co2Bool_json;
  
  if (rawCo2.defined) {
    co2Filtered = anonfun_FireDetection_43_30(rawCo2);
    co2Bool = anonfun_FireDetection_45_7(co2Filtered, enabled);
  }
  
  co2Bool_json = serialize_Option_Boolean(co2Bool);
  
  cJSON_AddItemToArray(json, co2Bool_json);
}

void co2_transaction_remote(cJSON* json, Option_Int toggleSelection) {
  bool enabled_changed = false;
  Option_Boolean co2Bool = {.defined = false};
  
  cJSON* co2Bool_json;
  
  if (toggleSelection.defined) {
    {
      bool _old = enabled;
      enabled = anonfun_FireDetection_38_46(enabled, toggleSelection.val);
      enabled_changed = !((_old == enabled));
    }
  }
  
  
  if (enabled_changed) {
    co2Bool = anonfun_FireDetection_45_7(createNone_Option_Boolean(), enabled);
  }
  
  co2Bool_json = serialize_Option_Boolean(co2Bool);
  
  cJSON_AddItemToArray(json, co2Bool_json);
}