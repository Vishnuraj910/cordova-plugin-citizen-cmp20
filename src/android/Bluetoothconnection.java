
package org.apache.cordova.bluetooth;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.app.ProgressDialog;

import android.R;
import com.citizen.port.android.BluetoothPort;
import com.citizen.request.android.RequestHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.UUID;
import android.content.res.AssetManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.util.Xml.Encoding;
import android.util.Base64;
import java.util.ArrayList;
import java.util.List;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import android.graphics.Typeface;
import com.citizen.jpos.command.CPCLConst;
import com.citizen.jpos.printer.CPCLPrinter;
import com.citizen.jpos.command.ESCPOSConst;
import com.citizen.jpos.printer.ESCPOSPrinter;

public class Bluetoothconnection extends CordovaPlugin {

	private static final String LIST = "list";
	private static final String CONNECT = "connect";
	private static final int REQUEST_ENABLE_BT = 2;
	private static final String LOG_TAG = "BluetoothPrinter";
	private static final String TAG = "Bluetoothconnection";
	public static final int LENGTH_SHORT = 0;

	private BluetoothAdapter mBluetoothAdapter;
	private Vector<BluetoothDevice> remoteDevices;
	private BroadcastReceiver searchFinish;
	private BroadcastReceiver searchStart;
	private BroadcastReceiver discoveryResult;
	private Thread hThread;
	private Context context;
	private connTask connectionTask;
	private BluetoothPort bluetoothPort;
	private String lastConnAddr;
	byte FONT_TYPE;
	private static BluetoothSocket btsocket;
	private static OutputStream btoutputstream;
	private  BluetoothDevice mmDevice;
	private ESCPOSPrinter posPtr = new ESCPOSPrinter("Shift_JIS");

	Bitmap bitmap;

	//	private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private ConnectThread mConnectThread;
	private CPCLPrinter cpclPrinter;

	private  BluetoothSocket mmSocket;
	String macAddress;
	String devicename;
	public Bluetoothconnection()
	{
		cpclPrinter = new CPCLPrinter();
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {


		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bluetoothSetup(callbackContext);
		}

		if (action.equals("printWithText")) {
			String errMsg = null;
			boolean secure = true;
			if(listBondedDevices(callbackContext)) //getting paired device
			{
				try
				{
					if(connect(callbackContext)) //connecting to the paired device
					{
						try
						{
							if(mmSocket != null) {
								Log.e(LOG_TAG,"Getting to printing function");
								printText(args, callbackContext); //Taking the prin of the text
							}

						}
						catch(Exception e)
						{
							// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
							errMsg = e.getMessage();
							Log.e(LOG_TAG, errMsg);
							e.printStackTrace();
							callbackContext.error("Error message" + errMsg);
						}

					}
					else
					{
						callbackContext.error("Could not connect to " + devicename);
						return true;
					}
				}
				catch (Exception e) {
					errMsg = e.getMessage();
					Log.e(LOG_TAG, errMsg);
					e.printStackTrace();
					callbackContext.error(errMsg);
				}
			}
			else
			{
				callbackContext.error("No Bluetooth Device Found");
				return true;
			}
			return true;
		}
		else if (action.equals("printWithLogo")) {
			String errMsg = null;
			boolean secure = true;
			if(listBondedDevices(callbackContext)) //getting paired device
			{
				try
				{
					if(connect(callbackContext)) //connecting to the paired device
					{
						try
						{
							if(mmSocket != null) {
								Log.e(LOG_TAG,"Getting to printing function");
								printWithLogo(args, callbackContext); //Taking the prin of the text
							}

						}
						catch(Exception e)
						{
							// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
							errMsg = e.getMessage();
							Log.e(LOG_TAG, errMsg);
							e.printStackTrace();
							callbackContext.error("Error message" + errMsg);
						}

					}
					else
					{
						callbackContext.error("Could not connect to " + devicename);
						return true;
					}
				}
				catch (Exception e) {
					errMsg = e.getMessage();
					Log.e(LOG_TAG, errMsg);
					e.printStackTrace();
					callbackContext.error(errMsg);
				}
			}
			else
			{
				callbackContext.error("No Bluetooth Device Found");
				return true;
			}
			return true;

		}
		return false;
	}

	public void bluetoothSetup(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			bluetoothPort = BluetoothPort.getInstance();
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null)
			{
				errMsg = "No bluetooth adapter available";
				Log.e(LOG_TAG, errMsg);
				callbackContext.error(errMsg);
				return;
			}
			if (!mBluetoothAdapter.isEnabled())
			{
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

			}
		}
		catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
	}
	boolean listBondedDevices(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				JSONArray json = new JSONArray();
				for (BluetoothDevice device : pairedDevices) {
					json.put(device.getName() +","+device.getAddress()+" ,[Paired]");
					macAddress = device.getAddress();
					devicename = device.getName();
				}
				Log.e(LOG_TAG, "Address is " + macAddress);
				return true;
			}
		}
		catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}
	boolean connect(CallbackContext callbackContext)
	{
		String errMsg = null;
		try
		{
			macAddress=macAddress.replace(" ", "");
			Log.e(LOG_TAG,"macaddress in connect" + macAddress );
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);

			if (device != null) {

				if((connectionTask != null) && (connectionTask.getStatus() == AsyncTask.Status.RUNNING))
				{
					connectionTask.cancel(true);
					if(!connectionTask.isCancelled())
						connectionTask.cancel(true);
					connectionTask = null;
					return false;
				}

				mConnectThread = new ConnectThread(device);
				mConnectThread.start();

				Log.e(TAG, "connect to: " + device);
				return true;
				/*Toast.makeText(this.cordova.getActivity(), "Bluetooth Connected to" + device.getName(), Toast.LENGTH_LONG).show();
				callbackContext.success("Your device is connect to " + device.getName());
*/
			}
		}
		catch(Exception e)
		{
			// Bluetooth Address Format [OO:OO:OO:OO:OO:OO]
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error("Error message" + errMsg);
		}

		return false;
	}

	private static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		if(width>w)
		{
			float scaleWidth = ((float) w) / width;
			float scaleHeight = ((float) h) / height+24;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleWidth);
			Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
					height, matrix, true);
			return resizedBitmap;
		}else{
			Bitmap resizedBitmap = Bitmap.createBitmap(w, height+24, Config.RGB_565);
			Canvas canvas = new Canvas(resizedBitmap);
			Paint paint = new Paint();
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(bitmap, (w-width)/2, 0, paint);
			return resizedBitmap;
		}
	}
	public static byte[] getBitmapData(Bitmap bitmap) {
		byte temp = 0;
		int j = 7;
		int start = 0;
		if (bitmap != null) {
			int mWidth = bitmap.getWidth();
			int mHeight = bitmap.getHeight();

			int[] mIntArray = new int[mWidth * mHeight];
			bitmap.getPixels(mIntArray, 0, mWidth, 0, 0, mWidth, mHeight);
			bitmap.recycle();
			byte []data=encodeYUV420SP(mIntArray, mWidth, mHeight);
			byte[] result = new byte[mWidth * mHeight / 8];
			for (int i = 0; i < mWidth * mHeight; i++) {
				temp = (byte) ((byte) (data[i] << j) + temp);
				j--;
				if (j < 0) {
					j = 7;
				}
				if (i % 8 == 7) {
					result[start++] = temp;
					temp = 0;
				}
			}
			if (j != 7) {
				result[start++] = temp;
			}

			int aHeight = 24 - mHeight % 24;
			int perline = mWidth / 8;
			byte[] add = new byte[aHeight * perline];
			byte[] nresult = new byte[mWidth * mHeight / 8 + aHeight * perline];
			System.arraycopy(result, 0, nresult, 0, result.length);
			System.arraycopy(add, 0, nresult, result.length, add.length);

			byte[] byteContent = new byte[(mWidth / 8 + 4)
					* (mHeight + aHeight)];//
			byte[] bytehead = new byte[4];//
			bytehead[0] = (byte) 0x1f;
			bytehead[1] = (byte) 0x10;
			bytehead[2] = (byte) (mWidth / 8);
			bytehead[3] = (byte) 0x00;
			for (int index = 0; index < mHeight + aHeight; index++) {
				System.arraycopy(bytehead, 0, byteContent, index
						* (perline + 4), 4);
				System.arraycopy(nresult, index * perline, byteContent, index
						* (perline + 4) + 4, perline);
			}
			return byteContent;
		}
		return null;

	}
	private static String hexStr = "0123456789ABCDEF";
	private static String[] binaryArray = {"0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111"};

	public static byte[] encodeYUV420SP(int[] rgba, int width, int height) {
		final int frameSize = width * height;
		byte[] yuv420sp=new byte[frameSize];
		int[] U, V;
		U = new int[frameSize];
		V = new int[frameSize];
		final int uvwidth = width / 2;
		int r, g, b, y, u, v;
		int bits = 8;
		int index = 0;
		int f = 0;
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				r = (rgba[index] & 0xff000000) >> 24;
				g = (rgba[index] & 0xff0000) >> 16;
				b = (rgba[index] & 0xff00) >> 8;
				// rgb to yuv
				y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
				u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
				v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
				// clip y
				// yuv420sp[index++] = (byte) ((y < 0) ? 0 : ((y > 255) ? 255 :
				// y));
				byte temp = (byte) ((y < 0) ? 0 : ((y > 255) ? 255 : y));
				yuv420sp[index++] = temp > 0 ? (byte) 1 : (byte) 0;

				// {
				// if (f == 0) {
				// yuv420sp[index++] = 0;
				// f = 1;
				// } else {
				// yuv420sp[index++] = 1;
				// f = 0;
				// }

				// }

			}

		}
		f = 0;
		return yuv420sp;
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	public static byte[] hexList2Byte(List<String> list) {
		List<byte[]> commandList = new ArrayList<byte[]>();

		for (String hexStr : list) {
			commandList.add(hexStringToBytes(hexStr));
		}
		byte[] bytes = sysCopy(commandList);
		return bytes;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static byte[] sysCopy(List<byte[]> srcArrays) {
		int len = 0;
		for (byte[] srcArray : srcArrays) {
			len += srcArray.length;
		}
		byte[] destArray = new byte[len];
		int destLen = 0;
		for (byte[] srcArray : srcArrays) {
			System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
			destLen += srcArray.length;
		}
		return destArray;
	}


	public static byte[] decodeBitmap(Bitmap bmp) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		List<String> list = new ArrayList<String>(); //binaryString list
		StringBuffer sb;
		int bitLen = bmpWidth / 8;
		int zeroCount = bmpWidth % 8;
		String zeroStr = "";
		if (zeroCount > 0) {
			bitLen = bmpWidth / 8 + 1;
			for (int i = 0; i < (8 - zeroCount); i++) {
				zeroStr = zeroStr + "0";
			}
		}

		for (int i = 0; i < bmpHeight; i++) {
			sb = new StringBuffer();
			for (int j = 0; j < bmpWidth; j++) {
				int color = bmp.getPixel(j, i);

				int r = (color >> 16) & 0xff;
				int g = (color >> 8) & 0xff;
				int b = color & 0xff;
				// if color close to white，bit='0', else bit='1'
				if (r > 160 && g > 160 && b > 160) {
					sb.append("0");
				} else {
					sb.append("1");
				}
			}
			if (zeroCount > 0) {
				sb.append(zeroStr);
			}
			list.add(sb.toString());
		}

		List<String> bmpHexList = binaryListToHexStringList(list);
		String commandHexString = "1D763000";
		String widthHexString = Integer.toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8 : (bmpWidth / 8 + 1));
		if (widthHexString.length() > 2) {
			Log.d(LOG_TAG, "DECODEBITMAP ERROR : width is too large");
			return null;
		} else if (widthHexString.length() == 1) {
			widthHexString = "0" + widthHexString;
		}
		widthHexString = widthHexString + "00";

		String heightHexString = Integer.toHexString(bmpHeight);
		if (heightHexString.length() > 2) {
			Log.d(LOG_TAG, "DECODEBITMAP ERROR : height is too large");
			return null;
		} else if (heightHexString.length() == 1) {
			heightHexString = "0" + heightHexString;
		}
		heightHexString = heightHexString + "00";

		List<String> commandList = new ArrayList<String>();
		commandList.add(commandHexString + widthHexString + heightHexString);
		commandList.addAll(bmpHexList);

		return hexList2Byte(commandList);
	}
	public static List<String> binaryListToHexStringList(List<String> list) {
		List<String> hexList = new ArrayList<String>();
		for (String binaryStr : list) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < binaryStr.length(); i += 8) {
				String str = binaryStr.substring(i, i + 8);

				String hexString = myBinaryStrToHexString(str);
				sb.append(hexString);
			}
			hexList.add(sb.toString());
		}
		return hexList;

	}
	public static String myBinaryStrToHexString(String binaryStr) {
		String hex = "";
		String f4 = binaryStr.substring(0, 4);
		String b4 = binaryStr.substring(4, 8);
		for (int i = 0; i < binaryArray.length; i++) {
			if (f4.equals(binaryArray[i])) {
				hex += hexStr.substring(i, i + 1);
			}
		}
		for (int i = 0; i < binaryArray.length; i++) {
			if (b4.equals(binaryArray[i])) {
				hex += hexStr.substring(i, i + 1);
			}
		}

		return hex;
	}

	boolean printText(JSONArray  args, CallbackContext callbackContext) {
		try {

			// TODO Auto-generated method stub


			btoutputstream = mmSocket.getOutputStream();
			String str = args.getString(0);
			String newline = "\n";

			String msg = str.toString();

			msg += "\n";

			btoutputstream.write(msg.getBytes());

			Log.e(LOG_TAG,"Printing success");
			mmSocket.close();
			Toast.makeText(this.cordova.getActivity(), "Successfully printed", Toast.LENGTH_LONG).show();
			callbackContext.success("Printed Successfuly : ");

			return true;


		} catch (Exception e) {
			Log.e(LOG_TAG,"Printing error" + e.getMessage());
			e.printStackTrace();
			callbackContext.error("Some error occured new " + e.getMessage());
		}

		return false;
	}

	boolean printWithLogo(JSONArray  args, CallbackContext callbackContext) {
		try {

			// TODO Auto-generated method stub


            btoutputstream = mmSocket.getOutputStream();
//			String img =  "img1";
//			cpclPrinter.printCPCLImage(img,0,0);
            String str = args.getString(0);

			final String encodedString = "data:image/jpeg;base64,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";
			final String pureBase64Encoded = encodedString.substring(encodedString.indexOf(",") + 1);
			final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);

			Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

			bitmap = decodedBitmap;
			int mWidth = bitmap.getWidth();
			int mHeight = bitmap.getHeight();

			bitmap = resizeImage(bitmap, 48 * 8, mHeight);

			byte[] bt = decodeBitmap(bitmap);


			btoutputstream.write(bt);

			String newline = "\n";

			String msg = str.toString();

			msg += "\n";

			btoutputstream.write(msg.getBytes());

			Log.e(LOG_TAG,"Printing success");
            mmSocket.close();
            Toast.makeText(this.cordova.getActivity(), "Successfully printed", Toast.LENGTH_LONG).show();
            callbackContext.success("Printed Successfuly : ");

            return true;


        } catch (Exception e) {
			Log.e(LOG_TAG,"Printing error" + e.getMessage());
			e.printStackTrace();
			callbackContext.error("Some error occured new " + e.getMessage());
		}

		return false;
	}


	// Bluetooth Connection Task.
	class connTask extends AsyncTask<BluetoothDevice, Void, Integer>
	{
		private final ProgressDialog dialog = new ProgressDialog( cordova.getActivity().getApplicationContext());

		@Override
		protected void onPreExecute()
		{
			dialog.setTitle("Bluetooth");
			dialog.setMessage("Connecting");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(BluetoothDevice... params)
		{
			Integer retVal = null;
			try
			{
				bluetoothPort.connect(params[0]);
				lastConnAddr = params[0].getAddress();
				retVal = Integer.valueOf(0);
			}
			catch (IOException e)
			{
				retVal = Integer.valueOf(-1);
			}
			return retVal;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			if(result.intValue() == 0)	// Connection success.
			{
				RequestHandler rh = new RequestHandler();
				hThread = new Thread(rh);
				hThread.start();
			}
			else	// Connection failed.
			{
				if(dialog.isShowing())
					dialog.dismiss();

			}
			super.onPostExecute(result);
		}
	}

	private class ConnectThread extends Thread {

		//private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			// MY_UUID is the app's UUID string, also used by the server code
			UUID uuid = device.getUuids()[0]
					.getUuid();
			try {
				Method m = mmDevice.getClass().getMethod("createRfcommSocket",
						new Class[] { int.class });
				mmSocket = (BluetoothSocket) m.invoke(mmDevice, Integer.valueOf(1));

				Thread.sleep(2000);
				mmSocket.connect();

			} catch (NoSuchMethodException e) {
			} catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (Exception e) {}


//			mmSocket = device
//					.createRfcommSocketToServiceRecord(uuid);

		}
		@Override
		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {



				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception


				try {



					Thread.sleep(2000);
					mmSocket.connect();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				/*try {
					mmSocket.close();
				} catch (IOException closeException) { }*/
				return;
			}

			// Do work to manage the connection (in a separate thread)
//        manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}




	}




}




