package co.telnet.a2dpnostandby;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectionStateReceiver extends BroadcastReceiver {
	private static final String TAG = "A2DP No Standby";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		int newState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);

		if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				Log.v(TAG, "Bluetooth connected");
				if (context.startService(new Intent(context, A2dpPlayerService.class)) == null)
					Log.e(TAG, "Couldn't start service");
			} else {
				Log.v(TAG, "Bluetooth not connected");
				context.stopService(new Intent(context, A2dpPlayerService.class));
			}
		}
	}

}
