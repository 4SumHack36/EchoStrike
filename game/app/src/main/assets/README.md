# Motion Model

Place your TensorFlow Lite model file here with the name motion_model.tflite for the motion detection feature to work correctly. The model should be trained to classify motion patterns from accelerometer and gyroscope data into three classes (0, 1, 2).

If no model file is found, the application will use a random classification fallback for demonstration purposes.

## Model Requirements

The model should:
- Accept input tensor of 600 float values (100 rows × 6 features: 3 for accelerometer xyz, 3 for gyroscope xyz)
- Output 3 class probabilities (for the three motion classes)
- Be saved in TensorFlow Lite format (*.tflite)

## Training Data Format

Training data should be collected from phone motion with:
- Linear acceleration sensor (x, y, z)
- Gyroscope sensor (x, y, z)
- Values normalized to [-1, 1] range
