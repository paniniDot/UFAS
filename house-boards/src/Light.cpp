#include "Light.h"

Light::Light(int pin)
  : JSONSensor("light") {
  this->pin = pin;
  pinMode(this->pin, OUTPUT);
  this->lightState = 0;
  this->pir_state = 0;
  this->photoresistor_state = 0;
  this->manual_state = 0;
}

void Light::update(Event<Msg> *e) {
  this->handleMessage(e->getEventArgs());
  this->updateLightState();
  this->notify();
}

void Light::handleMessage(Msg *msg) {
  String sensorName = msg->getSensorName();
  long timestamp = msg->getTimestamp();
  int measure = msg->getMeasure();
  if (sensorName.equals("manual_light")) {
    this->manual_state = measure;
  } else if (sensorName.equals("pir_sensor")) {
    this->pir_state = measure;
  } else if (sensorName.equals("photo_resistor")) {
    this->photoresistor_state = measure;
  } else if (sensorName.equals("light")) {
    this->lightState = measure;
  }
}

void Light::updateLightState() {
  if (!this->manual_state) {
    this->lightState = (pir_state && photoresistor_state) ? 1 : 0;
  }
  digitalWrite(this->pin, this->lightState ? HIGH : LOW);
}

void Light::notify() {
  String msg = this->getJson(this->lightState);
  Event<Msg> *e = new Event<Msg>(EventSourceType::LIGHT, new Msg(msg));
  for (int i = 0; i < this->getNObservers(); i++) {
    this->getObservers()[i]->update(e);
  }
  delete e;
}