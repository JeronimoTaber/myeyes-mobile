# myeyes mobile
Aplicacion Android que detecta objetos y provee indicaciones sobre su posicion respecto al dispositivo

## Features
- Deteccion de billeteras, lentes y llaves

To Do:
- Seleccion de objetos
- Guia hacia objetos

## Instalacion 
1) Instalar [Android Studio] (https://developer.android.com/studio/install#linux)
2) Importar aplicacion
2.1) Generar un emulador de android o generar la coleccion a un dispositivo movil 


## Cambio de modelo
En el caso que se desee cambia el modelo utilizado se deben reemplazar los achivos
- `detect.tflite`
- `labelmap.txt`
que se encuentran en `./app/src/main/assets`

Se deben hacer modificaciones en `DetectorActivity`

```java
  private static final int TF_OD_API_INPUT_SIZE = ; //INPUT SIZE DEL MODELO
  private static final boolean TF_OD_API_IS_QUANTIZED = ; //TRUE o FALSE dependiendo si esta quantizado
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite"; //Nombre del archivo del modelo
  private static final String TF_OD_API_LABELS_FILE = "labelmap.txt"; //Nombre del archivo que contiene los labels
```

## Tecnologias
El proyecto fue creado con :
* Android Studio version: Bumblebee | 2021.1.1
* Android SDK minimo version: 21
* Android SDK target version: 31

###Acknowledgment
- Aplicacion base de [Tensorflow Lite Object Detection](https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android)

