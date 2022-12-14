#include <stdbool.h>

#ifndef NATURALGRAPH_LIB
#define NATURALGRAPH_LIB

typedef struct {
  int _1;
  int _2;
  int _3;
} Tuple3_Int_Int_Int;

typedef struct {
  int val;
  bool defined;
} Option_Int;

Option_Int createNone_Option_Int();

Option_Int createSome_Option_Int(int val);

Tuple3_Int_Int_Int create_Tuple3_Int_Int_Int(int _1, int _2, int _3);

bool equals_Tuple3_Int_Int_Int(Tuple3_Int_Int_Int left, Tuple3_Int_Int_Int right);

#endif