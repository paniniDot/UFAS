#ifndef __PIR__
#define __PIR__
#include "Arduino.h"
#include "observer/Subject.h"
#include "observer/Event.h"

class Pir : public Subject<int>
{
private:
  int pir_pin;
  int getMotion();

public:
  Pir(int pir_pin);
  void notify();
};

#endif