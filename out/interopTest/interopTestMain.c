#include "interopTestMain.h"
#include "interopTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include <stdlib.h>
#include <stdio.h>
#include "dyad.h"

Option_Int anonfun_InteropTest_15_32(Option_Int localSource) {
  return ({
    Option_Int _temp = localSource;
    Option_Int _res;
    if (_temp.defined) _res = createSome_Option_Int(({
      int _$5 = _temp.val;
      ({
        (_$5 / 2);
      });
    })); else _res = createNone_Option_Int();
    _res;
  });
}

Option_String anonfun_InteropTest_17_26(Option_String fromScalaString) {
  return fromScalaString;
}

Option_Int anonfun_InteropTest_19_29(Option_Int fromScala) {
  return ({
    Option_Int _temp_2 = fromScala;
    Option_Int _res_2;
    if (_temp_2.defined) _res_2 = createSome_Option_Int(({
      int _$6 = _temp_2.val;
      ({
        (_$6 + 1);
      });
    })); else _res_2 = createNone_Option_Int();
    _res_2;
  });
}

Option_Int anonfun_InteropTest_21_27(Option_Int plusOne) {
  return ({
    Option_Int _temp_3 = plusOne;
    Option_Int _res_3;
    if (_temp_3.defined) _res_3 = createSome_Option_Int(({
      int _$7 = _temp_3.val;
      ({
        (_$7 * 2);
      });
    })); else _res_3 = createNone_Option_Int();
    _res_3;
  });
}

Option_Boolean anonfun_InteropTest_23_26(Option_Int fromScala) {
  return ({
    Option_Int _temp_4 = fromScala;
    Option_Boolean _res_4;
    if (_temp_4.defined) _res_4 = createSome_Option_Boolean(({
      int _$8 = _temp_4.val;
      ({
        ((_$8 % 2) == 0);
      });
    })); else _res_4 = createNone_Option_Boolean();
    _res_4;
  });
}

Option_Int anonfun_InteropTest_25_13(Option_Int toScala) {
  {
    Option_Int _temp_5 = toScala;
    if (_temp_5.defined) ({
      int i = _temp_5.val;
      {
        {
          printf("%d", i);
          printf("\n");
        }
      }
    });
  }
  return createNone_Option_Int();
}

void interopTest_startup() {

}

void interopTest_transaction_local(cJSON* json, Option_Int localSource) {
  Option_Int localMap = {.defined = false};
  
  cJSON* toScala_json = cJSON_CreateNull();
  cJSON* even_json = cJSON_CreateNull();
  
  if (localSource.defined) {
    localMap = anonfun_InteropTest_15_32(localSource);
  }
  
  
  cJSON_AddItemToArray(json, toScala_json);
  cJSON_AddItemToArray(json, even_json);
}

void interopTest_transaction_remote(cJSON* json, Option_Int fromScala, Option_String fromScalaString) {
  Option_String strCopy = {.defined = false};
  Option_Boolean even = {.defined = false};
  Option_Int plusOne = {.defined = false};
  Option_Int toScala = {.defined = false};
  Option_Int event_25_13 = {.defined = false};
  
  cJSON* toScala_json;
  cJSON* even_json;
  
  if (fromScalaString.defined) {
    strCopy = anonfun_InteropTest_17_26(fromScalaString);
  }
  
  
  if (fromScala.defined) {
    even = anonfun_InteropTest_23_26(fromScala);
    plusOne = anonfun_InteropTest_19_29(fromScala);
    toScala = anonfun_InteropTest_21_27(plusOne);
    event_25_13 = anonfun_InteropTest_25_13(toScala);
  }
  
  even_json = serialize_Option_Boolean(even);
  toScala_json = serialize_Option_Int(toScala);
  
  cJSON_AddItemToArray(json, toScala_json);
  cJSON_AddItemToArray(json, even_json);
}