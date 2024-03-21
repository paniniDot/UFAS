#include "Pir.h"

Pir::Pir(int pir_pin) {
  this->pir_pin = pir_pin;
  pinMode(pir_pin, INPUT);
  for (int i = 0; i < 5; i++) {
    delay(1000);
  }
}

int Pir::getMotion() {
  return digitalRead(this->pir_pin) == HIGH ? 1 : 0;
};

void Pir::notify() {
  Event<int> *e = new Event<int>(EventSourceType::PIR, new int(this->getMotion()));
  for (int i = 0; i < this->getNObservers(); i++) {
    this->getObservers()[i]->update(e);
  }
  delete e;
}
