#include "buttonMain.h"
#include "buttonLib.h"
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Tuple2_Boolean_Boolean anonfun_FireDetection_23_55(Tuple2_Boolean_Boolean x$1, bool x$2) {
  return ({
    Tuple2_Boolean_Boolean _res;
    Tuple2_Tuple2_Boolean_Boolean_Boolean _scrutinee = create_Tuple2_Tuple2_Boolean_Boolean_Boolean(x$1, x$2);
    
    if (true) {
      bool input = _scrutinee._2;
      bool lastInput = _scrutinee._1._2;
      bool oldState = _scrutinee._1._1;
      
      _res = (input == lastInput) ? create_Tuple2_Boolean_Boolean(input, input) : create_Tuple2_Boolean_Boolean(oldState, input);
    }
    
    _res;
  });
}

bool anonfun_FireDetection_27_39(Tuple2_Boolean_Boolean buttonFilter) {
  return ({
    Tuple2_Boolean_Boolean _$7$proxy1 = buttonFilter;
    _$7$proxy1._1;
  });
}

Option_Boolean anonfun_FireDetection_28_43(bool buttonFiltered) {
  return ({
    Option_Boolean _res_2;
    bool _scrutinee_2 = buttonFiltered;
    
    if (_scrutinee_2 == true) {
      _res_2 = createSome_Option_Boolean(buttonFiltered);
    } else if (true) {
      _res_2 = createNone_Option_Boolean();
    }
    
    _res_2;
  });
}

void button_startup() {
  buttonFilter = create_Tuple2_Boolean_Boolean(false, false);
  buttonFiltered = anonfun_FireDetection_27_39(buttonFilter);
}

void button_transaction_local(cJSON* json, Option_Boolean rawButton) {
  bool buttonFilter_changed = false;
  bool buttonFiltered_changed = false;
  Option_Boolean buttonSingleEmit = {.defined = false};
  
  cJSON* buttonSingleEmit_json;
  
  if (rawButton.defined) {
    {
      Tuple2_Boolean_Boolean _old = buttonFilter;
      buttonFilter = anonfun_FireDetection_23_55(buttonFilter, rawButton.val);
      buttonFilter_changed = !(equals_Tuple2_Boolean_Boolean(_old, buttonFilter));
    }
  }
  
  
  if (buttonFilter_changed) {
    {
      bool _old = buttonFiltered;
      buttonFiltered = anonfun_FireDetection_27_39(buttonFilter);
      buttonFiltered_changed = !((_old == buttonFiltered));
    }
  }
  
  
  if (buttonFiltered_changed) {
    buttonSingleEmit = anonfun_FireDetection_28_43(buttonFiltered);
  }
  
  buttonSingleEmit_json = serialize_Option_Boolean(buttonSingleEmit);
  
  cJSON_AddItemToArray(json, buttonSingleEmit_json);
}