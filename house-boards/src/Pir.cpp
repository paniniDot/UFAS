#include "Pir.h"

Pir::Pir(int pir_pin) : pir_pin(pir_pin), led_pin(led_pin) {
  pinMode(pir_pin, INPUT);
  for (int i = 0; i < 5; i++) {
    delay(1000);
  }
}

int Pir::getMotion() {
  int motion = digitalRead(this->pir_pin) == HIGH ? 1 : 0;
  analogWrite(this->led_pin, motion * 255);
  return motion;
};

void Pir::notify() {
  Event<int> *e = new Event<int>(EventSourceType::PIR, new int(this->getMotion()));
  for (auto observer : this->observers) {
    observer->update(e);
  }
  delete e;
}
