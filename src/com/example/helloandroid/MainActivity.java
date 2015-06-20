package com.example.helloandroid;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText editTextAddress, editTextPort, editTextMsg;
	private Button buttonConnect, buttonSend;
	private ObjectOutputStream oos;
	private Socket client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editTextAddress = (EditText) findViewById(R.id.editTextIP);
		editTextPort = (EditText) findViewById(R.id.editTextPort);
		editTextMsg = (EditText) findViewById(R.id.editText2);
		buttonConnect = (Button) findViewById(R.id.button1);
		buttonSend = (Button) findViewById(R.id.button2);

		buttonConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (client != null)
					return;
				SocketCreateTask socketCreateTask = new SocketCreateTask(
						editTextAddress.getText().toString(), Integer
								.parseInt(editTextPort.getText().toString()), v);
				try {
					socketCreateTask.execute();
					client = socketCreateTask.get();
					if (client != null) {
						oos = new ObjectOutputStream(client.getOutputStream());
						Log.d("Debug", "取得串流");
						RecieveMsgTask rcvMsgTask = new RecieveMsgTask(client,
								v);
						rcvMsgTask
								.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						Log.d("Debug", "開啟監聽");
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					Toast.makeText(v.getContext(), (CharSequence) errors,
							Toast.LENGTH_SHORT).show();
					Log.d("Debug", errors.toString());
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					Toast.makeText(v.getContext(), (CharSequence) errors,
							Toast.LENGTH_SHORT).show();
					Log.d("Debug", errors.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					Toast.makeText(v.getContext(), (CharSequence) errors,
							Toast.LENGTH_SHORT).show();
					Log.d("Debug", errors.toString());
				}

			}
		});

		buttonSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (oos == null)
					return;
				Log.d("Debug", "buttonSend");
				SendMsgTask sendMsgTask = new SendMsgTask(oos, editTextMsg
						.getText().toString(), arg0);
				sendMsgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

class SendMsgTask extends AsyncTask<Void, Void, Void> {

	ObjectOutputStream oos;
	String msg;
	View v;
	Exception e;

	SendMsgTask(ObjectOutputStream oos, String msg, View v) {
		this.oos = oos;
		this.msg = msg;
		this.v = v;
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		try {
			Log.d("debug", msg);
			oos.writeObject(msg);
			Log.d("debug", "Send end");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("debug", "IOException");
			e.printStackTrace();
			this.e = e;
		}

		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		// StringWriter errors = new StringWriter();
		// e.printStackTrace(new PrintWriter(errors));
		// Toast.makeText(v.getContext(), (CharSequence) errors,
		// Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
	}
}

class SocketCreateTask extends AsyncTask<Void, String, Socket> {

	String address;
	int port;
	View v;

	public SocketCreateTask(String address, int port, View v) {
		this.address = address;
		this.port = port;
		this.v = v;
	}

	@Override
	protected Socket doInBackground(Void... params) {
		// TODO Auto-generated method stub
		Socket client = null;

		try {
			// client = new Socket());
			Log.d("debug", "Try to connect'");
			client = new Socket("140.116.208.43", 9527);
			Log.d("debug", "Connection sucess'");

			publishProgress("Connection sucess");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", "NumberFormatException'");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("debug", "UnknownHostException'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			publishProgress("No Network");
			return null;
		}
		return client;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		Toast.makeText(v.getContext(), (CharSequence) values[0],
				Toast.LENGTH_SHORT).show();
		super.onProgressUpdate(values);
	}

}

class RecieveMsgTask extends AsyncTask<Void, String, Void> {

	Socket client;
	View v;
	Exception e;

	RecieveMsgTask(Socket client, View v) {
		this.client = client;
		this.v = v;
	}

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub

		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(client.getInputStream());
		} catch (StreamCorruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (true) {
			try {
				publishProgress((String) ois.readObject());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		// TODO Auto-generated method stub
		Toast.makeText(v.getContext(), (CharSequence) values[0],
				Toast.LENGTH_SHORT).show();
		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Void result) {
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		Toast.makeText(v.getContext(), (CharSequence) errors,
				Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
	}
}
