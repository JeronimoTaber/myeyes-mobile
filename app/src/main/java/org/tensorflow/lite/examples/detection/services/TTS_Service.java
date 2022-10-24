package org.tensorflow.lite.examples.detection.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.env.Logger;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TextToSpeech Service
 * Servicio de texto a voz inicializable desde cualquier clase
 * Antes de poder hacer uso del motor, el mismo debe estar inicializado (se tiene que haber llamado
 * al metodo onInit, lo cual sucede automaticamente cuando se completa el proceso de inicializacion)
 *
 * Cada vez que se quiera usar, se debe iniciar este servicio con un intento, el cual debe traer
 * como EXTRA el texto que se quiere reproducir.
 */
public class TTS_Service extends Service implements TextToSpeech.OnInitListener{
    private static final String TAG = "TTS_Service";
    public static final String EXTRA_TTS_TEXT = "com.package.EXTRA_TTS_TEXT";
    private String textToSpeak;
    private static boolean initDone;
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private static final Logger LOGGER = new Logger();
    private boolean play = false;

    /* ON CREATE */
    /* ********* */
    @Override
    public void onCreate() {
        super.onCreate();
        initDone = false;
        tts = new TextToSpeech(this,this/*OnInitListener*/);
        mediaPlayer = MediaPlayer.create(this, R.raw.z);

        LOGGER.i(TAG, "TTS lala");
    }
    /* ON INIT */
    /* ******* */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
            initDone = true;
            LOGGER.i(TAG, "TTS inicializado correctamente");
            speakOut();
        } else {
            tts = null;
            initDone = false;
            LOGGER.e(TAG, "Fallo al inicializar TTS");
        }
    }
    /* ON START COMAMND */
    /* **************** */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        textToSpeak = "";                               // Asumimos que no va a haber un texto
        // Acoording to documentation, intent may be null if service is restarted
        // so first we need to check if it is null before asking for extras
        if ((null != intent) && (intent.hasExtra(EXTRA_TTS_TEXT))){
            textToSpeak = intent.getStringExtra(EXTRA_TTS_TEXT);
        }
        // Si ya se inicializo y hay un texto para reproducir, hazlo!
        if (initDone) {
            speakOut();
        }
        // Debe ser stoppeado explicitamente
        return START_STICKY;
    }
    /* ON DESTROY */
    /* ********** */
    @Override
    public void onDestroy() {
        // Shutdown TTS before destroying the Service
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
    /* ON BIND */
    /* ******* */
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    /* SPEAK OUT */
    /* ********* */
    private void speakOut() {
        // First need to check if textToSpeak is not null
        if ((null != textToSpeak) && (!textToSpeak.equals(""))) {
            if (null != tts) {
                tts.setSpeechRate((float) 1);                    // Set speech rate a bit slower than normal
                tts.setLanguage(Locale.getDefault());               // Set deafualt Locale as Speech Languaje
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //   tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);   // Don't need and utteranceID to track
                //} else {
                Pattern p = Pattern.compile("alta: ");
                Matcher m = p.matcher(textToSpeak);
                boolean matchFound = m.find();
                if(matchFound) {
                    tts.stop();
                    mediaPlayer.stop();
                    textToSpeak = textToSpeak.substring(6,textToSpeak.length());
                    if(textToSpeak.equals("billetera")){
                        LOGGER.d("Entro"+textToSpeak);
                        mediaPlayer = MediaPlayer.create(this, R.raw.b_e);
                    } else if(textToSpeak.equals("llave")){
                        mediaPlayer = MediaPlayer.create(this, R.raw.ll_e);

                    } else if(textToSpeak.equals("lente")){
                        mediaPlayer = MediaPlayer.create(this, R.raw.l_e);
                    } else{
                        mediaPlayer = MediaPlayer.create(this, R.raw.vs);
                    }
                    mediaPlayer.start();
                    tts.speak("", TextToSpeech.QUEUE_FLUSH, null,null);                    //tts.speak(" " + textToSpeak, TextToSpeech.QUEUE_FLUSH, null,null);

                }
                else{
                    if(!mediaPlayer.isPlaying()){

                        LOGGER.d("TEXTO A EMITIR "+textToSpeak);

                        if(textToSpeak.equals("billetera")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b);
                            play = true;
                        }else if(textToSpeak.equals("llave")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll);
                            play = true;
                        }else if(textToSpeak.equals("lente")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l);
                            play = true;
                        }else if(textToSpeak.equals("billetera arriba")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_a);
                            play = true;

                        } else if(textToSpeak.equals("billetera abajo")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_ab);
                            play = true;

                        } else if(textToSpeak.equals("billetera derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_d);
                            play = true;

                        } else if(textToSpeak.equals("billetera izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_i);
                            play = true;

                        }else if(textToSpeak.equals("billetera arriba derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_a_d);
                            play = true;

                        }else if(textToSpeak.equals("billetera arriba izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_a_i);
                            play = true;

                        }else if(textToSpeak.equals("billetera abajo derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_ab_d);
                            play = true;

                        }else if(textToSpeak.equals("billetera abajo izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_ab_i);
                            play = true;

                        }else if(textToSpeak.equals("billetera centro")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.b_c);
                            play = true;

                        }else if(textToSpeak.equals("llave arriba")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_a);
                            play = true;

                        } else if(textToSpeak.equals("llave abajo")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_ab);
                            play = true;

                        } else if(textToSpeak.equals("llave derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_d);
                            play = true;

                        } else if(textToSpeak.equals("llave izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_i);
                            play = true;

                        }else if(textToSpeak.equals("llave arriba derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_a_d);
                            play = true;

                        }else if(textToSpeak.equals("llave arriba izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_a_i);
                            play = true;

                        }else if(textToSpeak.equals("llave abajo derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_ab_d);
                            play = true;

                        }else if(textToSpeak.equals("llave abajo izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_ab_i);
                            play = true;

                        }else if(textToSpeak.equals("llave centro")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.ll_c);
                            play = true;

                        }else if(textToSpeak.equals("lente arriba")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_a);
                            play = true;

                        } else if(textToSpeak.equals("lente abajo")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_ab);
                            play = true;

                        } else if(textToSpeak.equals("lente derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_d);
                            play = true;

                        } else if(textToSpeak.equals("lente izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_i);
                            play = true;

                        }else if(textToSpeak.equals("lente arriba derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_a_d);
                            play = true;

                        }else if(textToSpeak.equals("lente arriba izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_a_i);
                            play = true;
                        }else if(textToSpeak.equals("lente abajo derecha")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_ab_d);
                            play = true;
                        }else if(textToSpeak.equals("lente abajo izquierda")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_ab_i);
                            play = true;
                        } else if(textToSpeak.equals("lente centro")){
                            mediaPlayer = MediaPlayer.create(this, R.raw.l_c);
                            play = true;

                        }else{
                            play = false;
                        }
                        LOGGER.d("TEXTP "+play);

                        if(play){
                            LOGGER.d("Entro  "+play);
                            mediaPlayer.start();
                        }
                    }
                        tts.speak("", TextToSpeech.QUEUE_FLUSH, null,null);

                }

                //}
            } else {
                Log.e(TAG, "No se puede hablar porque no existe TTS");
            }
        }else{
            Log.w(TAG, "No hay texto para reproducir. Si se trata de la primer inicializacion, no pasa nada");
        }
    }
}
