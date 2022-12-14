#include "temperatureMain.h"
#include "temperatureLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

bool anonfun_FireDetection_57_46(bool x$1, int x$2) {
  return ({
    bool _res;
    Tuple2_Boolean_Int _scrutinee = create_Tuple2_Boolean_Int(x$1, x$2);
    
    if (_scrutinee._2 == 1) {
      bool state = _scrutinee._1;
      
      _res = !(state);
    } else if (true) {
      bool state = _scrutinee._1;
      
      _res = state;
    }
    
    _res;
  });
}

Option_Boolean anonfun_FireDetection_62_46(Option_Int rawTemperature) {
  return ({
    Option_Int _temp = rawTemperature;
    Option_Boolean _res_2;
    if (_temp.defined) _res_2 = createSome_Option_Boolean(({
      int _$10 = _temp.val;
      ({
        (_$10 > 45);
      });
    })); else _res_2 = createNone_Option_Boolean();
    _res_2;
  });
}

Option_Boolean anonfun_FireDetection_64_7(Option_Boolean temperatureFiltered, bool enabled) {
  return enabled ? temperatureFiltered : createSome_Option_Boolean(false);
}

void temperature_startup() {
  enabled = true;
}

void temperature_transaction_local(cJSON* json, Option_Int rawTemperature) {
  Option_Boolean temperatureFiltered = {.defined = false};
  Option_Boolean temperatureBool = {.defined = false};
  
  cJSON* temperatureBool_json;
  
  if (rawTemperature.defined) {
    temperatureFiltered = anonfun_FireDetection_62_46(rawTemperature);
    temperatureBool = anonfun_FireDetection_64_7(temperatureFiltered, enabled);
  }
  
  temperatureBool_json = serialize_Option_Boolean(temperatureBool);
  
  cJSON_AddItemToArray(json, temperatureBool_json);
}

void temperature_transaction_remote(cJSON* json, Option_Int toggleSelection) {
  bool enabled_changed = false;
  Option_Boolean temperatureBool = {.defined = false};
  
  cJSON* temperatureBool_json;
  
  if (toggleSelection.defined) {
    {
      bool _old = enabled;
      enabled = anonfun_FireDetection_57_46(enabled, toggleSelection.val);
      enabled_changed = !((_old == enabled));
    }
  }
  
  
  if (enabled_changed) {
    temperatureBool = anonfun_FireDetection_64_7(createNone_Option_Boolean(), enabled);
  }
  
  temperatureBool_json = serialize_Option_Boolean(temperatureBool);
  
  cJSON_AddItemToArray(json, temperatureBool_json);
}