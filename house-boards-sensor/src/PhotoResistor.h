#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__
#include "Arduino.h"
#include "observer/Subject.h"
#include "observer/Event.h"

class PhotoResistor : public Subject<int>
{

private:
  int pin;
  int isDark();

public:
  PhotoResistor(int pin);
  void notify();
};

#endif