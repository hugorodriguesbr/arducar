package br.com.arducar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ActivityPrincipal extends Activity {

	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	volatile boolean stopWorker;

	TextView myLabel;
	EditText MyDevicename;
	boolean conexao = false;

	TimerTask mTimerTask;
	final Handler handler = new Handler();
	Timer t = new Timer();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_principal); 

		final Button BTN_start = (Button)findViewById(R.id.btn_start);
		Button BTN_frente = (Button)findViewById(R.id.btn_frente);
		Button BTN_traz = (Button)findViewById(R.id.btn_traz);
		Button BTN_esq = (Button)findViewById(R.id.btn_esquerda);
		Button BTN_dir = (Button)findViewById(R.id.btn_direita);
		Button BTN_buz = (Button)findViewById(R.id.btn_buzina); 
		MyDevicename = (EditText)findViewById(R.id.mydevicename); 
		myLabel = (TextView)findViewById(R.id.mytxt); 

		//Open Button
		BTN_start.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				try 
				{    				
					if(!conexao){
						if(MyDevicename.getText().toString() != "" ){
							findBT();
							openBT();

							conexao = true;
							BTN_start.setText("Conectado");
						}
					}else{
						closeBT();
						conexao = false;
						BTN_start.setText("Iniciar Comunicação");
					}
				}
				catch (IOException ex) { }
			}
		});

		BTN_frente.setOnTouchListener(new View.OnTouchListener() {  

			@Override  	
			public boolean onTouch(View v, MotionEvent event) {  

				if(event.getAction() == MotionEvent.ACTION_DOWN){  
					//txt_teste.setText(String.valueOf("Apertou"));
					doTimerTask("f");
					myLabel.setText("Enviando Dados..");
				}
				if(event.getAction() == MotionEvent.ACTION_UP){ 
					//txt_teste.setText(String.valueOf("Solto"));
					try {
						sendData("*");
						stopTask();
						myLabel.setText("Esperando Comando.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}  
		});
		BTN_traz.setOnTouchListener(new View.OnTouchListener() {  

			@Override  	
			public boolean onTouch(View v, MotionEvent event) {  

				if(event.getAction() == MotionEvent.ACTION_DOWN){  
					//txt_teste.setText(String.valueOf("Apertou"));
					doTimerTask("t");
					myLabel.setText("Enviando Dados..");
				}
				if(event.getAction() == MotionEvent.ACTION_UP){ 
					//txt_teste.setText(String.valueOf("Solto"));
					try {
						sendData("*");
						stopTask();
						myLabel.setText("Esperando Comando.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}  
		});
		BTN_esq.setOnTouchListener(new View.OnTouchListener() {  

			@Override  	
			public boolean onTouch(View v, MotionEvent event) {  

				if(event.getAction() == MotionEvent.ACTION_DOWN){  
					//txt_teste.setText(String.valueOf("Apertou"));
					doTimerTask("e");
					myLabel.setText("Enviando Dados..");
				}
				if(event.getAction() == MotionEvent.ACTION_UP){ 
					//txt_teste.setText(String.valueOf("Solto"));
					try {
						sendData("*");
						stopTask();
						myLabel.setText("Esperando Comando.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}  
		});
		BTN_dir.setOnTouchListener(new View.OnTouchListener() {  

			@Override  	
			public boolean onTouch(View v, MotionEvent event) {  

				if(event.getAction() == MotionEvent.ACTION_DOWN){  
					//txt_teste.setText(String.valueOf("Apertou"));
					doTimerTask("d");
					myLabel.setText("Enviando Dados..");
				}
				if(event.getAction() == MotionEvent.ACTION_UP){ 
					//txt_teste.setText(String.valueOf("Solto"));
					try {
						sendData("*");
						stopTask();
						myLabel.setText("Esperando Comando.");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}  
		});
		BTN_buz.setOnTouchListener(new View.OnTouchListener() {  

			@Override  	
			public boolean onTouch(View v, MotionEvent event) {  

				if(event.getAction() == MotionEvent.ACTION_DOWN){  

				}
				if(event.getAction() == MotionEvent.ACTION_UP){ 

				}
				return false;
			}  
		});

	}   

	public void doTimerTask(final String msg){

		mTimerTask = new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							sendData(msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}};

			// public void schedule (TimerTask task, long delay, long period) 
			t.schedule(mTimerTask, 0, 100);  // 
	}

	public void stopTask(){

		if(mTimerTask!=null){
			mTimerTask.cancel();
		}

	}  

	void findBT()
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
		{
			myLabel.setText("No bluetooth adapter available");
		}

		if(!mBluetoothAdapter.isEnabled())
		{
			Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, 0);
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0)
		{
			for(BluetoothDevice device : pairedDevices)
			{
				if(device.getName().equals(MyDevicename.getText().toString())) 
				{
					mmDevice = device;
					break;
				}
			}
		}
		myLabel.setText("Bluetooth Device Found");
	}

	void openBT() throws IOException
	{
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
		mmSocket.connect();
		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();

		beginListenForData();

		myLabel.setText("Bluetooth Opened");
	}

	void beginListenForData()
	{
		final Handler handler = new Handler(); 
		final byte delimiter = 10; //This is the ASCII code for a newline character

		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable()
		{
			public void run()
			{                
				while(!Thread.currentThread().isInterrupted() && !stopWorker)
				{
					try 
					{
						int bytesAvailable = mmInputStream.available();                        
						if(bytesAvailable > 0)
						{
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for(int i=0;i<bytesAvailable;i++)
							{
								byte b = packetBytes[i];
								if(b == delimiter)
								{
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
									final String data = new String(encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable()
									{
										public void run()
										{
											myLabel.setText(data);
										}
									});
								}
								else
								{
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} 
					catch (IOException ex) 
					{
						stopWorker = true;
					}
				}
			}
		});

		workerThread.start();
	}

	public void sendData(String msg) throws IOException
	{
		mmOutputStream.write(msg.getBytes());
	}

	public void closeBT() throws IOException
	{
		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		myLabel.setText("Conexão Fechada.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_principal, menu);
		return true;
	}
}