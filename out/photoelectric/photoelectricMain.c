#include "photoelectricMain.h"
#include "photoelectricLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

bool anonfun_FireDetection_76_46(bool x$1, int x$2) {
  return ({
    bool _res;
    Tuple2_Boolean_Int _scrutinee = create_Tuple2_Boolean_Int(x$1, x$2);
    
    if (_scrutinee._2 == 2) {
      bool state = _scrutinee._1;
      
      _res = !(state);
    } else if (true) {
      bool state = _scrutinee._1;
      
      _res = state;
    }
    
    _res;
  });
}

Option_Boolean anonfun_FireDetection_82_7(Option_Boolean rawPhotoelec, bool enabled) {
  return enabled ? rawPhotoelec : createSome_Option_Boolean(false);
}

void photoelectric_startup() {
  enabled = true;
}

void photoelectric_transaction_local(cJSON* json, Option_Boolean rawPhotoelec) {
  Option_Boolean photoelecBool = {.defined = false};
  
  cJSON* photoelecBool_json;
  
  if (rawPhotoelec.defined) {
    photoelecBool = anonfun_FireDetection_82_7(rawPhotoelec, enabled);
  }
  
  photoelecBool_json = serialize_Option_Boolean(photoelecBool);
  
  cJSON_AddItemToArray(json, photoelecBool_json);
}

void photoelectric_transaction_remote(cJSON* json, Option_Int toggleSelection) {
  bool enabled_changed = false;
  Option_Boolean photoelecBool = {.defined = false};
  
  cJSON* photoelecBool_json;
  
  if (toggleSelection.defined) {
    {
      bool _old = enabled;
      enabled = anonfun_FireDetection_76_46(enabled, toggleSelection.val);
      enabled_changed = !((_old == enabled));
    }
  }
  
  
  if (enabled_changed) {
    photoelecBool = anonfun_FireDetection_82_7(createNone_Option_Boolean(), enabled);
  }
  
  photoelecBool_json = serialize_Option_Boolean(photoelecBool);
  
  cJSON_AddItemToArray(json, photoelecBool_json);
}