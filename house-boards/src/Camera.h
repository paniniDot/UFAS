#ifndef CAMERA_H
#define CAMERA_H

#include <Arduino.h>
#include "esp_camera.h"
#include "Base64.h"
#include "observer/Subject.h"
#include "observer/Event.h"
#include "observer/EventSourceType.h"
#include "JSONSensor.h"

class Camera : JSONSensor<String>, Subject<String>
{
private:
    String captureAndEncodeImage();

public:
    Camera();   
    void notify();
};

#endif