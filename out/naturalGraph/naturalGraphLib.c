#include "naturalGraphLib.h"
#include <stdbool.h>

Option_Int createNone_Option_Int() {
  Option_Int opt = {.defined = false};
  return opt;
}

Option_Int createSome_Option_Int(int val) {
  Option_Int opt = {.val = val, .defined = true};
  return opt;
}

Tuple3_Int_Int_Int create_Tuple3_Int_Int_Int(int _1, int _2, int _3) {
  Tuple3_Int_Int_Int prod = {._1 = _1, ._2 = _2, ._3 = _3};
  return prod;
}

bool equals_Tuple3_Int_Int_Int(Tuple3_Int_Int_Int left, Tuple3_Int_Int_Int right) {
  return (left._1 == right._1) && (left._2 == right._2) && (left._3 == right._3);
}