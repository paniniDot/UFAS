import tensorflow as tf
import numpy as np
import cv2

class FireNet:
    def __init__(self):
        self.model = tf.keras.models.load_model('fire_detection.model')
        self.model._make_predict_function()

    def predict(self, image):
        image = cv2.imdecode(np.frombuffer(image, np.uint8), -1)
        if image.shape != (224, 224, 3):
            image = tf.image.resize(image, (128, 128))
        return self.model.predict(np.array([image/255.0])).argmax()

    def get_weights(self):
        return self.model.get_weights()

    def get_config(self):
        return self.model.get_config()

    def summary(self):
        return self.model.summary()