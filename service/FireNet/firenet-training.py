import os
import cv2
import numpy as np
from tqdm import tqdm

categroiess = ['no_fire', 'fire']

IMG_SIZE = 224
VIDEO_DIR = 'videos'

def extract_frames_from_video(video_path):
    frames = []
    cap = cv2.VideoCapture(video_path)
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        frame_resized = cv2.resize(frame, (IMG_SIZE, IMG_SIZE))
        frames.append(frame_resized)
    cap.release()
    return frames

def create_training_data():
    training_data = []
    for category in os.listdir(VIDEO_DIR):
        category_path = os.path.join(VIDEO_DIR, category)
        if os.path.isdir(category_path):
            class_num = len(training_data) 
            for video_file in tqdm(os.listdir(category_path)):
                if video_file.endswith('.mp4'):
                    video_path = os.path.join(category_path, video_file)
                    frames = extract_frames_from_video(video_path)
                    for frame in frames:
                        training_data.append([frame, class_num])
    return training_data

training_data = create_training_data()