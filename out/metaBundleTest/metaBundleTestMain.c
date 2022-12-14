#include "metaBundleTestMain.h"
#include "metaBundleTestLib.h"
#include <string.h>
#include <stdbool.h>
#include "cJSON.h"
#include "hashmap.h"
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

int init_signal_10_18() {
  return 5;
}

int anonfun_MetaBundleExample_15_7(int source) {
  return (source + inc);
}

Array_Int anonfun_MetaBundleExample_19_7(int derived) {
  return create_Array_Int(1, derived);
}

Option_Int anonfun_MetaBundleExample_22_17(Array_Int arraySignal) {
  {
    Array_Int a$proxy2 = retain_Array_Int(arraySignal);
    {
      print_Array_Int(a$proxy2);
      printf("\n");
    }
    
    release_Array_Int(a$proxy2, false);
  }
  return createNone_Option_Int();
}

Option_String anonfun_MetaBundleExample_26_27(Option_String esource) {
  return ({
    Option_String _temp = esource;
    Option_String _res;
    if (_temp.defined) _res = createSome_Option_String(({
      char* str = _temp.val;
      ({
        str;
      });
    })); else _res = createNone_Option_String();
    _res;
  });
}

Option_Array_String anonfun_MetaBundleExample_28_30(Option_String emapped) {
  return ({
    Option_String _temp_2 = emapped;
    Option_Array_String _res_2;
    if (_temp_2.defined) _res_2 = createSome_Option_Array_String(({
      char* str = _temp_2.val;
      ({
        create_Array_String(1, str);
      });
    })); else _res_2 = createNone_Option_Array_String();
    _res_2;
  });
}

Option_String anonfun_MetaBundleExample_31_7(Option_String emapped) {
  return ({
    Option_String _temp_3 = emapped;
    Option_String _res_3;
    if (_temp_3.defined && ({
      char* _$4 = _temp_3.val;
      ({
        (strlen(_$4) == 3);
      });
    })) _res_3 = _temp_3; else _res_3 = createNone_Option_String();
    _res_3;
  });
}

Option_Tuple2_String_String anonfun_MetaBundleExample_37_7(Option_String filtered, Option_String emapped) {
  return ({
    Option_Tuple2_String_String _res_4;
    Tuple2_Option_String_Option_String _scrutinee = create_Tuple2_Option_String_Option_String(emapped, filtered);
    
    if (_scrutinee._1.defined && _scrutinee._2.defined) {
      char* right = _scrutinee._2.val;
      char* left = _scrutinee._1.val;
      
      _res_4 = someTuple(left, right);
    } else if (true) {
      _res_4 = createNone_Option_Tuple2_String_String();
    }
    
    _res_4;
  });
}

Option_Int anonfun_MetaBundleExample_42_12(Option_Tuple2_String_String zipped) {
  {
    Option_Tuple2_String_String _temp_4 = zipped;
    if (_temp_4.defined) ({
      Tuple2_String_String t = _temp_4.val;
      {
        {
          print_Tuple2_String_String(t);
          printf("\n");
        }
      }
    });
  }
  return createNone_Option_Int();
}

Option_Int anonfun_MetaBundleExample_45_7(Option_String esource, int derived) {
  return ({
    Option_String _temp_5 = esource;
    Option_Int _res_5;
    if (_temp_5.defined) _res_5 = createSome_Option_Int(({
      char* _$5 = _temp_5.val;
      ({
        derived;
      });
    })); else _res_5 = createNone_Option_Int();
    _res_5;
  });
}

Option_Int anonfun_MetaBundleExample_48_33(Option_String esource, int derived) {
  return ({
    Option_String _temp_6 = esource;
    Option_Int _res_6;
    if (_temp_6.defined) _res_6 = createSome_Option_Int(({
      char* _$6 = _temp_6.val;
      ({
        derived;
      });
    })); else _res_6 = createNone_Option_Int();
    _res_6;
  });
}

Map_String_String anonfun_MetaBundleExample_50_60(Map_String_String acc, char* next) {
  retain_Map_String_String(acc);
  
  add_Map_String_String(acc, next);
  Map_String_String _f_res = retain_Map_String_String(acc);
  
  release_Map_String_String(acc, false);
  
  release_Map_String_String(_f_res, true);
  return _f_res;
}

Option_Int anonfun_MetaBundleExample_55_16(Map_String_String foldResult) {
  {
    Map_String_String s$proxy2 = retain_Map_String_String(foldResult);
    {
      printSet_Map_String_String(s$proxy2);
      printf("\n");
    }
    
    release_Map_String_String(s$proxy2, false);
  }
  return createNone_Option_Int();
}

void metaBundleTest_startup() {
  inc = 1;
  source = init_signal_10_18();
  derived = anonfun_MetaBundleExample_15_7(source);
  arraySignal = retain_Array_Int(anonfun_MetaBundleExample_19_7(derived));
  foldResult = retain_Map_String_String(create_Map_String_String());
}

void metaBundleTest_transaction_local(Option_String esource, Option_Int source_param) {
  bool foldResult_changed = false;
  Option_String emapped = {.defined = false};
  Option_String filtered = {.defined = false};
  Option_Array_String arrayEvent = {.defined = false};
  Option_Int event_55_16 = {.defined = false};
  Option_Tuple2_String_String zipped = {.defined = false};
  Option_Int event_42_12 = {.defined = false};
  bool source_changed = false;
  bool derived_changed = false;
  Option_Int snapshotLike2 = {.defined = false};
  Option_Int snapshotLike = {.defined = false};
  bool arraySignal_changed = false;
  Option_Int event_22_17 = {.defined = false};
  
  if (esource.defined) {
    {
      Map_String_String temp = foldResult;
      Map_String_String _old = retain_Map_String_String(deepCopy_Map_String_String(foldResult));
      foldResult = retain_Map_String_String(anonfun_MetaBundleExample_50_60(foldResult, esource.val));
      foldResult_changed = !(equals_Map_String_String(_old, foldResult));
      release_Map_String_String(_old, false);
      release_Map_String_String(temp, false);
    }
    emapped = anonfun_MetaBundleExample_26_27(esource);
    filtered = anonfun_MetaBundleExample_31_7(emapped);
    arrayEvent = retain_Option_Array_String(anonfun_MetaBundleExample_28_30(emapped));
  }
  
  
  if (arrayEvent.defined) release_Option_Array_String(arrayEvent, false);
  
  if (foldResult_changed) {
    event_55_16 = anonfun_MetaBundleExample_55_16(foldResult);
  }
  
  
  if (filtered.defined || esource.defined) {
    zipped = anonfun_MetaBundleExample_37_7(filtered, emapped);
  }
  
  
  if (zipped.defined) {
    event_42_12 = anonfun_MetaBundleExample_42_12(zipped);
  }
  
  
  if (source_param.defined) {
    {
      int _old = source;
      source = source_param.val;
      source_changed = !((_old == source));
    }
  }
  
  
  if (source_changed) {
    {
      int _old = derived;
      derived = anonfun_MetaBundleExample_15_7(source);
      derived_changed = !((_old == derived));
    }
  }
  
  
  if (esource.defined || derived_changed) {
    snapshotLike2 = anonfun_MetaBundleExample_48_33(esource, derived);
    snapshotLike = anonfun_MetaBundleExample_45_7(esource, derived);
  }
  
  
  if (derived_changed) {
    {
      Array_Int temp = arraySignal;
      Array_Int _old = retain_Array_Int(deepCopy_Array_Int(arraySignal));
      arraySignal = retain_Array_Int(anonfun_MetaBundleExample_19_7(derived));
      arraySignal_changed = !((_old.data == arraySignal.data));
      release_Array_Int(_old, false);
      release_Array_Int(temp, false);
    }
  }
  
  
  if (arraySignal_changed) {
    event_22_17 = anonfun_MetaBundleExample_22_17(arraySignal);
  }
}