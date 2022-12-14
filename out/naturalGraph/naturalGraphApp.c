#include "naturalGraphMain.h"
#include "naturalGraphLib.h"
#include <stdbool.h>

int main(int argc, char* argv[]) {
  naturalGraph_startup();
  naturalGraph_transaction_local(createNone_Option_Int());
  return 0;
}