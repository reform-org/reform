#include "naturalGraphMain.h"
#include "naturalGraphLib.h"
#include <stdbool.h>

int init_signal_8_18() {
  return 0;
}

int anonfun_NaturalGraph_11_21(int source) {
  return ({
    int _$18$proxy1 = source;
    (_$18$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_14_17(int c1) {
  return ({
    int _$19$proxy1 = c1;
    (_$19$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_15_17(int b1) {
  return ({
    int _$20$proxy1 = b1;
    (_$20$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_16_17(int b2) {
  return ({
    int _$21$proxy1 = b2;
    (_$21$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_19_17(int b3) {
  return ({
    int _$22$proxy1 = b3;
    (_$22$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_20_22(int c2) {
  c2;
  return 0;
}

int anonfun_NaturalGraph_21_17(int c3) {
  return ({
    int _$23$proxy1 = c3;
    (_$23$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_24_17(int b2) {
  return ({
    int _$24$proxy1 = b2;
    (_$24$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_25_17(int a1) {
  return ({
    int _$25$proxy1 = a1;
    (_$25$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_26_24(int b2, int a2) {
  return (a2 + b2);
}

int anonfun_NaturalGraph_27_17(int a3) {
  return ({
    int _$26$proxy1 = a3;
    (_$26$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_30_22(int b3, int a4) {
  a4;
  b3;
  return 0;
}

int anonfun_NaturalGraph_31_17(int b4) {
  return ({
    int _$27$proxy1 = b4;
    (_$27$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_32_17(int b5) {
  return ({
    int _$28$proxy1 = b5;
    (_$28$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_33_17(int b6) {
  return ({
    int _$29$proxy1 = b6;
    (_$29$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_34_24(int b7, int c2) {
  return (b7 + c2);
}

int anonfun_NaturalGraph_37_24(int c4, int b8) {
  return (c4 + b8);
}

int anonfun_NaturalGraph_40_17(int c2) {
  return ({
    int _$30$proxy1 = c2;
    (_$30$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_43_22(int c1) {
  c1;
  return 0;
}

int anonfun_NaturalGraph_44_17(int e1) {
  return ({
    int _$31$proxy1 = e1;
    (_$31$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_45_17(int e2) {
  return ({
    int _$32$proxy1 = e2;
    (_$32$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_46_17(int e3) {
  return ({
    int _$33$proxy1 = e3;
    (_$33$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_47_24(int c2, int e4) {
  return (e4 + c2);
}

int anonfun_NaturalGraph_49_17(int c2) {
  return ({
    int _$34$proxy1 = c2;
    (_$34$proxy1 + 1);
  });
}

int anonfun_NaturalGraph_50_24(int e6, int d1) {
  return (e6 + d1);
}

Tuple3_Int_Int_Int anonfun_NaturalGraph_52_28(int e5, int c5, int e7) {
  return create_Tuple3_Int_Int_Int(c5, e5, e7);
}

void naturalGraph_startup() {
  e5 = anonfun_NaturalGraph_47_24(c2, e4);
  e3 = anonfun_NaturalGraph_45_17(e2);
  e1 = anonfun_NaturalGraph_43_22(c1);
  c5 = anonfun_NaturalGraph_37_24(c4, b8);
  b1 = anonfun_NaturalGraph_14_17(c1);
  source = init_signal_8_18();
  b7 = anonfun_NaturalGraph_33_17(b6);
  b8 = anonfun_NaturalGraph_34_24(b7, c2);
  b2 = anonfun_NaturalGraph_15_17(b1);
  c4 = anonfun_NaturalGraph_21_17(c3);
  e6 = anonfun_NaturalGraph_49_17(c2);
  b6 = anonfun_NaturalGraph_32_17(b5);
  c3 = anonfun_NaturalGraph_20_22(c2);
  b4 = anonfun_NaturalGraph_30_22(b3, a4);
  a4 = anonfun_NaturalGraph_27_17(a3);
  a3 = anonfun_NaturalGraph_26_24(b2, a2);
  b5 = anonfun_NaturalGraph_31_17(b4);
  a2 = anonfun_NaturalGraph_25_17(a1);
  c1 = anonfun_NaturalGraph_11_21(source);
  b3 = anonfun_NaturalGraph_16_17(b2);
  a1 = anonfun_NaturalGraph_24_17(b2);
  c2 = anonfun_NaturalGraph_19_17(b3);
  d1 = anonfun_NaturalGraph_40_17(c2);
  e7 = anonfun_NaturalGraph_50_24(e6, d1);
  e4 = anonfun_NaturalGraph_46_17(e3);
  e2 = anonfun_NaturalGraph_44_17(e1);
  result = anonfun_NaturalGraph_52_28(e5, c5, e7);
}

void naturalGraph_transaction_local(Option_Int source_param) {
  bool source_changed = false;
  bool c1_changed = false;
  bool e1_changed = false;
  bool b1_changed = false;
  bool e2_changed = false;
  bool e3_changed = false;
  bool e4_changed = false;
  bool b2_changed = false;
  bool a1_changed = false;
  bool b3_changed = false;
  bool a2_changed = false;
  bool a3_changed = false;
  bool a4_changed = false;
  bool b4_changed = false;
  bool b5_changed = false;
  bool b6_changed = false;
  bool b7_changed = false;
  bool c2_changed = false;
  bool c3_changed = false;
  bool e6_changed = false;
  bool d1_changed = false;
  bool c4_changed = false;
  bool e7_changed = false;
  bool b8_changed = false;
  bool c5_changed = false;
  bool e5_changed = false;
  bool result_changed = false;
  
  if (source_param.defined) {
    {
      int _old = source;
      source = source_param.val;
      source_changed = !((_old == source));
    }
  }
  
  
  if (source_changed) {
    {
      int _old = c1;
      c1 = anonfun_NaturalGraph_11_21(source);
      c1_changed = !((_old == c1));
    }
  }
  
  
  if (c1_changed) {
    {
      int _old = e1;
      e1 = anonfun_NaturalGraph_43_22(c1);
      e1_changed = !((_old == e1));
    }
    {
      int _old = b1;
      b1 = anonfun_NaturalGraph_14_17(c1);
      b1_changed = !((_old == b1));
    }
  }
  
  
  if (e1_changed) {
    {
      int _old = e2;
      e2 = anonfun_NaturalGraph_44_17(e1);
      e2_changed = !((_old == e2));
    }
  }
  
  
  if (e2_changed) {
    {
      int _old = e3;
      e3 = anonfun_NaturalGraph_45_17(e2);
      e3_changed = !((_old == e3));
    }
  }
  
  
  if (e3_changed) {
    {
      int _old = e4;
      e4 = anonfun_NaturalGraph_46_17(e3);
      e4_changed = !((_old == e4));
    }
  }
  
  
  if (b1_changed) {
    {
      int _old = b2;
      b2 = anonfun_NaturalGraph_15_17(b1);
      b2_changed = !((_old == b2));
    }
  }
  
  
  if (b2_changed) {
    {
      int _old = a1;
      a1 = anonfun_NaturalGraph_24_17(b2);
      a1_changed = !((_old == a1));
    }
    {
      int _old = b3;
      b3 = anonfun_NaturalGraph_16_17(b2);
      b3_changed = !((_old == b3));
    }
  }
  
  
  if (a1_changed) {
    {
      int _old = a2;
      a2 = anonfun_NaturalGraph_25_17(a1);
      a2_changed = !((_old == a2));
    }
  }
  
  
  if (b2_changed || a2_changed) {
    {
      int _old = a3;
      a3 = anonfun_NaturalGraph_26_24(b2, a2);
      a3_changed = !((_old == a3));
    }
  }
  
  
  if (a3_changed) {
    {
      int _old = a4;
      a4 = anonfun_NaturalGraph_27_17(a3);
      a4_changed = !((_old == a4));
    }
  }
  
  
  if (b3_changed || a4_changed) {
    {
      int _old = b4;
      b4 = anonfun_NaturalGraph_30_22(b3, a4);
      b4_changed = !((_old == b4));
    }
  }
  
  
  if (b4_changed) {
    {
      int _old = b5;
      b5 = anonfun_NaturalGraph_31_17(b4);
      b5_changed = !((_old == b5));
    }
  }
  
  
  if (b5_changed) {
    {
      int _old = b6;
      b6 = anonfun_NaturalGraph_32_17(b5);
      b6_changed = !((_old == b6));
    }
  }
  
  
  if (b6_changed) {
    {
      int _old = b7;
      b7 = anonfun_NaturalGraph_33_17(b6);
      b7_changed = !((_old == b7));
    }
  }
  
  
  if (b3_changed) {
    {
      int _old = c2;
      c2 = anonfun_NaturalGraph_19_17(b3);
      c2_changed = !((_old == c2));
    }
  }
  
  
  if (c2_changed) {
    {
      int _old = c3;
      c3 = anonfun_NaturalGraph_20_22(c2);
      c3_changed = !((_old == c3));
    }
    {
      int _old = e6;
      e6 = anonfun_NaturalGraph_49_17(c2);
      e6_changed = !((_old == e6));
    }
    {
      int _old = d1;
      d1 = anonfun_NaturalGraph_40_17(c2);
      d1_changed = !((_old == d1));
    }
  }
  
  
  if (c3_changed) {
    {
      int _old = c4;
      c4 = anonfun_NaturalGraph_21_17(c3);
      c4_changed = !((_old == c4));
    }
  }
  
  
  if (e6_changed || d1_changed) {
    {
      int _old = e7;
      e7 = anonfun_NaturalGraph_50_24(e6, d1);
      e7_changed = !((_old == e7));
    }
  }
  
  
  if (b7_changed || c2_changed) {
    {
      int _old = b8;
      b8 = anonfun_NaturalGraph_34_24(b7, c2);
      b8_changed = !((_old == b8));
    }
  }
  
  
  if (c4_changed || b8_changed) {
    {
      int _old = c5;
      c5 = anonfun_NaturalGraph_37_24(c4, b8);
      c5_changed = !((_old == c5));
    }
  }
  
  
  if (c2_changed || e4_changed) {
    {
      int _old = e5;
      e5 = anonfun_NaturalGraph_47_24(c2, e4);
      e5_changed = !((_old == e5));
    }
  }
  
  
  if (e5_changed || c5_changed || e7_changed) {
    {
      Tuple3_Int_Int_Int _old = result;
      result = anonfun_NaturalGraph_52_28(e5, c5, e7);
      result_changed = !(equals_Tuple3_Int_Int_Int(_old, result));
    }
  }
}