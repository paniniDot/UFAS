import tensorflow as tf
import numpy as np
import cv2
import os

class FireNet:
    def __init__(self):
        self.model = tf.keras.models.load_model(r'C:\Users\ricca\Documents\uni\arduino\UFAS\house-service\python\fire_detection.h5')

    def predict(self, image):
        image = cv2.imdecode(np.asarray(bytearray(image), dtype="uint8"), cv2.IMREAD_COLOR)
        image = tf.image.resize(image, (128, 128))
        return self.model.predict(np.array([image/255.0])).argmax()

    def get_weights(self):
        return self.model.get_weights()

    def get_config(self):
        return self.model.get_config()

    def summary(self):
        return self.model.summary()