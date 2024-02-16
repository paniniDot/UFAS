#include "PhotoResistor.h"
#define TRESHOLD 3000

PhotoResistor::PhotoResistor(int pin) : JSONSensor<int>("photo_resistor"), pin(pin)
{
  pinMode(pin, INPUT);
}

int PhotoResistor::isDark()
{
  return analogRead(pin) <= TRESHOLD;
};