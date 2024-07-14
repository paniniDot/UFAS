#include "CO2.h"

Co2::Co2()
  : JSONSensor("co2"), state(IDLE), lastMeasureTime(0) {
  this->measInterval = 10;
  this->co2ppm = 0;
  this->err = XENSIV_PASCO2_OK;
}

void Co2::begin() {
  // Initialize the i2c interface used by the sensor
  Wire.begin();
  Wire.setClock(400000);

  // Initialize the sensor
  this->err = this->cotwo.begin();
  if (XENSIV_PASCO2_OK != this->err) {
    Serial.print("initialization error: ");
    Serial.println(this->err);
  }
}

void Co2::startMeasurement() {
  this->err = this->cotwo.startMeasure();
  if (XENSIV_PASCO2_OK != this->err) {
    Serial.print("error: ");
    Serial.println(err);
    return;
  }
  this->state = MEASURING;
  this->lastMeasureTime = millis();
}

void Co2::measure() {
  switch (this->state) {
    case IDLE:
      this->startMeasurement();
      break;

    case MEASURING:
      if (millis() - this->lastMeasureTime >= this->measInterval * 1000) {
        this->err = this->cotwo.getCO2(this->co2ppm);
        if (XENSIV_PASCO2_OK != this->err) {
          Serial.print("error: ");
          Serial.println(this->err);
          this->state = IDLE;
        } else {
          this->state = DONE;
        }
      }
      break;

    case DONE:
      Serial.print("co2 ppm value: ");
      Serial.println(this->co2ppm);
      this->state = IDLE;
      break;
  }
}

void Co2::notify() {
  this->measure();
  if (this->state == DONE) {
    Event<String> *json = new Event<String>(EventSourceType::CO2, new String(this->getJson(this->co2ppm)));
    for (int i = 0; i < this->getNObservers(); i++) {
      this->getObservers()[i]->update(json);
    }
    delete json;
  }
}
