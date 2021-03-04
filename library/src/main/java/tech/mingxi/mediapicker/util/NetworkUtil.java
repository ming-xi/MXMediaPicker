package tech.mingxi.mediapicker.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public class NetworkUtil {
	private static final String TAG = NetworkUtil.class.getSimpleName();

	public enum NetworkType {
		NONE, CELLULAR, WIFI
	}

	private static Context appContext;


	static class Singleton {
		static NetworkUtil INSTANCE;

		static void init(Context context) {
			INSTANCE = new NetworkUtil(context);
		}
	}

	public static NetworkUtil getInstance() {
		return Singleton.INSTANCE;
	}

	static void init(Context context) {
		appContext = context;
		Singleton.init(appContext);
	}


	private NetworkUtil(Context context) {

	}

	public static boolean isNetworkAvailable() {
		return getNetworkType() != NetworkType.NONE;
	}

	public static NetworkType getNetworkType() {
		NetworkType[] types = new NetworkType[]{NetworkType.WIFI, NetworkType.CELLULAR};
		for (NetworkType type : types) {
			if (isConnectedTo(type)) {
				return type;
			}
		}
		return NetworkType.NONE;
	}

	private static boolean isConnectedTo(NetworkType networkType) {
		if (networkType == NetworkType.NONE) {
			throw new IllegalArgumentException();
		}
		ConnectivityManager connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			Network network = connectivityManager.getActiveNetwork();
			NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
			if (capabilities == null) {
				return false;
			}
			int type = -1;
			switch (networkType) {
				case CELLULAR:
					type = NetworkCapabilities.TRANSPORT_CELLULAR;
					break;
				case WIFI:
					type = NetworkCapabilities.TRANSPORT_WIFI;
					break;
			}
			if (type == -1) {
				throw new IllegalArgumentException();
			}
			return capabilities.hasTransport(type);
		} else {
			int type = -1;
			switch (networkType) {
				case CELLULAR:
					type = ConnectivityManager.TYPE_MOBILE;
					break;
				case WIFI:
					type = ConnectivityManager.TYPE_WIFI;
					break;
			}
			NetworkInfo networkInfo = connectivityManager.getNetworkInfo(type);
			if (networkInfo == null) {
				return false;
			}
			return networkInfo.isConnected();
		}
	}
}

