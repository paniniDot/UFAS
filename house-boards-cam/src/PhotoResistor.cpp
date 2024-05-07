#include "PhotoResistor.h"
#define TRESHOLD 3000

PhotoResistor::PhotoResistor(int pin) : pin(pin)
{
  pinMode(pin, INPUT);
}

int PhotoResistor::isDark()
{
  return analogRead(pin) <= TRESHOLD;
};

void PhotoResistor::notify()
{
  Event<int> *e = new Event<int>(EventSourceType::PHOTO_RESISTOR, new int(this->isDark()));
  for (int i = 0; i < this->getNObservers(); i++) {
    this->getObservers()[i]->update(e);
  }
  delete e;
}