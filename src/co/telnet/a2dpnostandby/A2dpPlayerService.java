package co.telnet.a2dpnostandby;

import co.telnet.a2dpnostandby.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class A2dpPlayerService extends Service {
	private static final String TAG = "A2DP No Standby";

	private final IBinder mBinder = new LocalBinder();
	private final int NOTIFICATION = 1;
	private NotificationManager nm;
	private volatile Thread playerThread;

	private final int duration = 1; // seconds
	private final int sampleRate = 44100;
	private final int numSamples = duration * sampleRate;
	private final double sample[] = new double[numSamples];
	private final double freqOfTone = 440; // hz
	private final byte generatedSnd[] = new byte[2 * numSamples];
	private AudioTrack audioTrack = null;

	public class LocalBinder extends Binder {
		A2dpPlayerService getService() {
			return A2dpPlayerService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		showNotification();
		startSoundThread();
	}

	@Override
	public void onDestroy() {
		stopSoundThread();
		stopNotification();
	}

	private void showNotification() {
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(), 0);
		Notification notification = new Notification.Builder(this)
				.setContentTitle("A2DP Player").setContentText("Running")
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.ic_launcher).build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		nm.notify(NOTIFICATION, notification);
	}

	private void stopNotification() {
		nm.cancel(NOTIFICATION);
	}

	/* Tone generation example from http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
	 * Not really necessary in practice since the volume is turned down, but a tone is nice for testing. */
	void genTone() {
		// fill out the array
		for (int i = 0; i < numSamples; ++i) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
		}

		// convert to 16 bit pcm sound array
		// assumes the sample buffer is normalized.
		int idx = 0;
		for (final double dVal : sample) {
			// scale to maximum amplitude
			final short val = (short) ((dVal * 32767));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSnd[idx++] = (byte) (val & 0x00ff);
			generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

		}
	}

	private synchronized void startSoundThread(){
		if(playerThread == null){
			playerThread = new Thread(new Runnable() {
				public void run() {
					Log.v(TAG, "Player thread started.");
					initSound();
					while(Thread.currentThread() == playerThread) {
						playSound();
					}
					cleanupSound();
					Log.v(TAG, "Player thread stopped.");
				}
			});
			playerThread.start();
		}
	}

	private synchronized void stopSoundThread(){
		if(playerThread != null){
			Thread moribund = playerThread;
			playerThread = null;
			moribund.interrupt();
		}
	}

	private void initSound() {
		audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, sampleRate,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STREAM);
		audioTrack.setStereoVolume(0, 0);
		genTone();
	}

	private void playSound() {
		audioTrack.write(generatedSnd, 0, generatedSnd.length);
		audioTrack.play();
	}

	private void cleanupSound() {
		audioTrack.stop();
		audioTrack.release();
		audioTrack = null;
	}
}
