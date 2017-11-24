package com.pf.androidspeaker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

public class AndroidSpeakerActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final String TAG = "MainActivity";

    private TextToSpeech mTTS;
    private boolean mStarted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        mTTS = new TextToSpeech(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTTS.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                doSpeech((float)0.5);
                //doSpeech((float)0.75);
                //doSpeech((float)1.0);
                //doSpeech((float)1.25);
                //doSpeech((float)1.5);
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    public void doSpeech(float pitch) {

        String pitchStr = String.valueOf(pitch);
        mTTS.setPitch(pitch);

        speakOut("that's great", "17_" + pitchStr + ".mp3");
        sleep(5000);

        /*
        speakOut("That’s beautiful, you should always keep your eyes open.", "Pitch_" + pitchStr + "_1_2.mp3");
        sleep(10000);
        speakOut("Now wipe that grumpy grin off your face and smile instead.", "Pitch_" + pitchStr + "_1_3.mp3");
        sleep(10000);
        speakOut("Lovely. Smiling is good for you, you should always smile.", "Pitch_" + pitchStr + "_1_4.mp3");
        sleep(10000);
*?

/*
        speakOut("Check your wearable.", "Pitch_" + pitchStr + "_CheckYourWearable");
        sleep(10000);
        speakOut("It now has maps", "Pitch_" + pitchStr + "_ItNowHasMaps");
        sleep(10000);
        speakOut("It now has maps", "Pitch_" + pitchStr + "_ItNowHasMaps");
        sleep(10000);
        speakOut("You tell me. You just came here and started speaking", "Pitch_" + pitchStr + "_YouJustCameHere");
        sleep(10000);
        speakOut("Recording dev byte. Get to work", "Pitch_" + pitchStr + "_RecordingDevByte");
        sleep(10000);
*/
/*
 *         speakOut("Walking and Running", "Pitch_" + pitchStr + "_A_01_WalkingAndRunning");

        sleep(10000);
        speakOut("Walking. And. Running", "Pitch_" + pitchStr + "_A_02_Waking_And_Running");
        sleep(10000);
        speakOut("Ok", "Pitch_" + pitchStr + "_Z_01_Ok");
        sleep(10000);
        speakOut("You are walking", "Pitch_" + pitchStr + "_B_01_YouAreWalking");
        sleep(10000);
        speakOut("You are riding a bike", "Pitch_" + pitchStr + "_B_02_YouAreRidingABike");
        sleep(10000);
        speakOut("You are running. Keep running it is good for you", "Pitch_" + pitchStr + "_B_03_YouAreRunning");
        sleep(10000);
        speakOut("Thank you", "Pitch_" + pitchStr + "_B_00_ThankYou");
        sleep(10000);
        */

        //speakOut("Tell them. That Google play services rocks", "GMS_Rocks");
        //sleep(10000);
        //speakOut("Magnus is not here. He is traveling in time. Can I leave a message", "Magnus_Not_Here");
        //sleep(10000);

//        speakOut("Excelsior", "Pitch_" + pitchStr + "_B_05_Excelsior");
//        sleep(10000);
        //speakOut("You need to be more careful when you are climbing", "Pitch_" + pitchStr + "_C_01_YouNeedToBeCareful");
        //sleep(10000);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception exc) {
        }
    }

    private void speakOut(String text, String postfix) {
        // mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        Log.e("SPEECH", "creating " + Environment.getExternalStorageDirectory() + "/speech_" + postfix);
        mTTS.synthesizeToFile(text, new HashMap<String, String>(), Environment.getExternalStorageDirectory() + "/speech_" + postfix);
        Log.e("SPEECH", "That one was created");
    }

}




/*
package com.example.speaker;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

	private static final String TAG = "MainActivity";

	private TextToSpeech mTTS;
	private boolean mStarted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTTS = new TextToSpeech(this, this);
	}

    @Override
    protected void onResume() {
        super.onResume();
    }

	@Override
	public void onDestroy() {
		// Don't forget to shutdown tts!
		if (mTTS != null) {
			mTTS.stop();
			mTTS.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = mTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				sleep(2000);
				//doSpeech((float)0.5);
				//doSpeech((float)0.75);
				doSpeech((float)1.0);
				// doSpeech((float)1.3);
				//doSpeech((float)1.6);
				// doSpeech((float)2.0);
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}

	public void doSpeech(float pitch) {
		mTTS.setPitch(pitch);
		speakOut("Walking and Running", "10");
		sleep(10000);
		speakOut("Ok", "Ok");
		sleep(10000);
		speakOut("Walking. And. Running", "11");
		sleep(10000);
		speakOut("You are walking", "2");
		sleep(10000);
		speakOut("You are riding a bike", "3");
		sleep(10000);
		speakOut("You are running", "40");
		sleep(10000);
		speakOut("Keep running it is good for you", "41");
		sleep(10000);
		speakOut("Thank you", "ThankYou");
		sleep(10000);
		speakOut("No. Don't stop running. You are very close to Google plex", "5");
		sleep(10000);
		speakOut("Be more careful when you climb", "60");
		sleep(10000);
		speakOut("You need to be more careful when you are climbing", "61");
		sleep(10000);
	}

	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch(Exception exc) {
		}
	}

	private void speakOut(String text, String postfix) {
		// mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		mTTS.synthesizeToFile(text, null, "/sdcard/speech_" + postfix);
	}
}
*/
