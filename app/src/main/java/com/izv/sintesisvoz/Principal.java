package com.izv.sintesisvoz;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;
import chatterapi.ChatterBot;
import chatterapi.ChatterBotFactory;
import chatterapi.ChatterBotSession;
import chatterapi.ChatterBotType;

public class Principal  extends Activity implements View.OnClickListener,  TextToSpeech.OnInitListener {

    private ChatterBotSession bot1session;
    private boolean reproductor = false;
    static final int CTE_LEE = 1;
    static final int CTE_RECONOCE = 2;
    private TextToSpeech tts;
    private EditText tvTexto;
    private hHablar hb;
    private Button bEs, bFr, bUs, bHablar;
    private Locale espanol, frances;
    private String reconoceIdioma = "es-ES";


    /***************************************************************/
    /*                                                             */
    /*                          Metodos ON                         */
    /*                                                             */
    /***************************************************************/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CTE_LEE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);//contexto y listener
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == CTE_RECONOCE && resultCode == RESULT_OK) {
            ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            tvTexto.append("Yo: " + textos.get(0) + "\n");
            String s = textos.get(0);
            hb = new hHablar();
            hb.execute(s);

            bHablar.setClickable(false);
            for (int i = 0; i < textos.size(); i++) {
                Log.v("textos:", textos.get(i));
            }
        }
    }


    @Override
    public void onClick(View v) {

        if (bEs == v) {
            reconoceIdioma = "es-ES";
            tts.setLanguage(espanol);
            tostada(getString(R.string.btEs));
        } else if (bFr == v) {
            reconoceIdioma = "fr-FR";
            tts.setLanguage(frances);
            tostada(getString(R.string.btFr));
        } else if (bUs == v) {
            reconoceIdioma = "en-US";
            tts.setLanguage(Locale.US);
            tostada(getString(R.string.btUs));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        initComponents();
        ChatterBotFactory factory = new ChatterBotFactory();
        ChatterBot bot1 = null;
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bot1session=bot1.createSession();

        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, CTE_LEE);
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            reproductor = true;

            tts.isSpeaking();// Para saber si esta hablando, mientras esta hablando no no hacer nada.
        } else {
            //no se puede reproducir
        }
        Log.v("¿Funciona?", reproductor + "");
    }

    @Override
    protected void onStop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    /***************************************************************/
    /*                                                             */
    /*                             Hilo                            */
    /*                                                             */
    /***************************************************************/

    class hHablar extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String texto = params[0];
            String aux = "";
            try {
                aux = bot1session.think(texto);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return aux;
        }

        @Override
        protected void onPostExecute(String texto) {
            super.onPostExecute(texto);
            if (reproductor) {
                tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null);
                tvTexto.append("Bot: " + texto + "\n");
                bHablar.setClickable(true);
            }
            hb = null;

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    /***************************************************************/
    /*                                                             */
    /*                           AUXILIARES                        */
    /*                                                             */
    /***************************************************************/

    public void hablar(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, reconoceIdioma);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.habla));
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
        startActivityForResult(i, CTE_RECONOCE);
    }

    public void initComponents() {
        espanol = new Locale("ES");
        frances = new Locale("fr");
        bEs = (Button) findViewById(R.id.bEspanol);
        bFr = (Button) findViewById(R.id.bFr);
        bUs = (Button) findViewById(R.id.bIngles);
        bHablar = (Button) findViewById(R.id.bHablar);
        tvTexto = (EditText) findViewById(R.id.tv);
    }

    //Sacamos mensajes de información
    private void tostada(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}

