package abaqueda.cl.cim_nxt;import android.app.Activity;import android.bluetooth.BluetoothAdapter;import android.bluetooth.BluetoothDevice;import android.content.Intent;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.view.MotionEvent;import android.view.View;import android.widget.Button;import android.widget.LinearLayout;import android.widget.RelativeLayout;import android.widget.SeekBar;import android.widget.TextView;import android.widget.Toast;import android.widget.ToggleButton;public class MainActivity extends Activity  {    static NXTTalker mNXTTalker;    private static final int REQUEST_ENABLE_BT = 1;//Variable de solicitud de habilitar bluetooth    private static final int REQUEST_CONNECT_DEVICE = 2; //Variable para conectar el dispositivo    private static final int REQUEST_SETTINGS = 3; //Variable de ajustes    public static final int MESSAGE_TOAST = 1;    public static final int MESSAGE_STATE_CHANGE = 2;    public static final String TOAST = "toast"; // Variable que se ocupa para la notificación al usuario sin que se pare el proceso    private int mState = NXTTalker.STATE_NONE; //Variable para indicar el estado del robot    private int MotorSeleccionado=1;    private byte Power=50; //Variable para la intensidad del motor del robot    private int mSavedState = NXTTalker.STATE_NONE; //Variable inicializada en estado ninguno    private boolean mNewLaunch = true; //Variable para nuevo lanzamiento    private String mDeviceAddress = null; //Variable de la dirección del dispositivo inicializado en nulo    //Variable que contiene el daptador por defecto del dispositivo    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    //Variable booleana que contiene al adaptador bluetooth si no es nulo(true)    boolean hasBluetooth = !(mBluetoothAdapter == null);    private byte[] sensores = new byte[1024]; //Variable de tipo byte que almacenaran los sensores    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.main);        //  Variable que contiene el menu y se castea con linearlayout, el cual alinea las vistas        final LinearLayout MenuLayout = (LinearLayout) findViewById(R.id.menuLayout); //Menu superior        // Variables de tipo final que se castean con relativelayout el cual es el por defecto en android        final RelativeLayout LinkLayout = (RelativeLayout) findViewById(R.id.linkLayout); //Panel link para iniciar la conexión        final RelativeLayout MotorLayout = (RelativeLayout) findViewById(R.id.motorLayout); //Panel motores        final RelativeLayout SensorLayout = (RelativeLayout) findViewById(R.id.sensorLayout); //Panel sensores        //Indica que menu y link se vean de forma predeterminada al entrar a la aplicación con VISIBLE y motor y sensor no ocurre eso        MenuLayout.setVisibility(View.VISIBLE);        LinkLayout.setVisibility(View.VISIBLE);        MotorLayout.setVisibility(View.INVISIBLE);        SensorLayout.setVisibility(View.INVISIBLE);        //Panel link (forma grafica)        final ToggleButton Bluetooth = (ToggleButton) findViewById(R.id.bluetooth);        Bluetooth.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                //Si hasbluetooth es falso, corresponde que no pudo encontrar el bluetooth por defecto                if (!hasBluetooth)                    Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();                if (Bluetooth.isChecked()) {                    // Si el bluetooth existe pero el adaptador no esta habilitado, se le pide al usuario que lo habilite                    if (hasBluetooth && !mBluetoothAdapter.isEnabled()) {                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);                        Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();                    } else {                        if (hasBluetooth && mBluetoothAdapter.isEnabled()) {                            if (mSavedState == NXTTalker.STATE_CONNECTED) {                                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);                                mNXTTalker.connect(device);                            } else {                                if (mNewLaunch) {                                    mNewLaunch = false;                                    startConnection();                                }                            }                            Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();                        }                    }                }                mNXTTalker.stop();            }        });        //Panel  de los motores        final Button MotorR = (Button) findViewById(R.id.motorR); //Avanzar motor seleccionado R        final Button MotorL = (Button) findViewById(R.id.motorL); //Retroceder motor seleccinado L        final Button Motor1c = (Button) findViewById(R.id.motor1); //Cambiar al motor 1        final Button Motor2c = (Button) findViewById(R.id.motor2); //Cambiar al motor 2        final Button Motor3c = (Button) findViewById(R.id.motor3); //Cambiar al motor 3        Motor1c.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MotorSeleccionado = 1;            }        });        Motor2c.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MotorSeleccionado = 2;            }        });        Motor3c.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MotorSeleccionado = 3;            }        });        MotorR.setOnTouchListener(new View.OnTouchListener() {            @Override            public boolean onTouch(View v, MotionEvent event) {                switch (event.getAction()) {                    case MotionEvent.ACTION_DOWN:                        switch (MotorSeleccionado) {                            case 1:                                moverMotores((byte) 1, (byte) 0, (byte) 0);                                break;                            case 2:                                moverMotores((byte) 0, (byte) 1, (byte) 0);                                break;                            case 3:                                moverMotores((byte) 0, (byte) 0, (byte) 1);                                break;                        }                        return true;                    case MotionEvent.ACTION_UP:                        moverMotores((byte) 0, (byte) 0, (byte) 0);                        return true;                }                return false;            }        });        MotorL.setOnTouchListener(new View.OnTouchListener() {            @Override            public boolean onTouch(View v, MotionEvent event) {                switch (event.getAction()) {                    case MotionEvent.ACTION_DOWN:                        switch (MotorSeleccionado) {                            case 1:                                moverMotores((byte) -1, (byte) 0, (byte) 0);                                break;                            case 2:                                moverMotores((byte) 0, (byte) -1, (byte) 0);                                break;                            case 3:                                moverMotores((byte) 0, (byte) 0, (byte) -1);                                break;                        }                        return true;                    case MotionEvent.ACTION_UP:                        moverMotores((byte) 0, (byte) 0, (byte) 0);                        return true;                }                return false;            }        });        //Barra de intensidad del motor del robot        SeekBar powerSeekBar = (SeekBar) findViewById(R.id.power);        powerSeekBar.setProgress(Power);        powerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {            @Override            public void onProgressChanged(SeekBar seekBar, int progress,                                          boolean fromUser) {                Power = (byte) progress;            }            @Override            public void onStartTrackingTouch(SeekBar seekBar) {            }            @Override            public void onStopTrackingTouch(SeekBar seekBar) {            }        });        //Panel Sensores        final TextView datosSensor1 = (TextView) findViewById(R.id.sensor1); //Datos del sensor 1: color        final TextView datosSensor2 = (TextView) findViewById(R.id.sensor2); //Datos del sensor 2: tacto        final TextView datosSensor3 = (TextView) findViewById(R.id.sensor3); //Datos del sensor 3: ultrasonido        final TextView datosSensor4 = (TextView) findViewById(R.id.sensor4); //Datos del sensor 4: luz        final TextView instrucciones = (TextView) findViewById(R.id.instrucciones); //Sugiere abrir el programa del robot        if (savedInstanceState != null) {            mNewLaunch = false;            mDeviceAddress = savedInstanceState.getString("device_address");            if (mDeviceAddress != null) {                mSavedState = NXTTalker.STATE_CONNECTED;            }        }        //Menu superior        final Button Link = (Button) findViewById(R.id.sLink);        final Button Motores = (Button) findViewById(R.id.sMotores);        final Button Sensores = (Button) findViewById(R.id.sSensores);        Link.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MenuLayout.setVisibility(View.VISIBLE);                LinkLayout.setVisibility(View.VISIBLE);                MotorLayout.setVisibility(View.INVISIBLE);                SensorLayout.setVisibility(View.INVISIBLE);            }        });        Motores.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MenuLayout.setVisibility(View.VISIBLE);                LinkLayout.setVisibility(View.INVISIBLE);                MotorLayout.setVisibility(View.VISIBLE);                SensorLayout.setVisibility(View.INVISIBLE);            }        });        Sensores.setOnClickListener(new View.OnClickListener() {            public void onClick(View v) {                MenuLayout.setVisibility(View.VISIBLE);                LinkLayout.setVisibility(View.INVISIBLE);                MotorLayout.setVisibility(View.INVISIBLE);                SensorLayout.setVisibility(View.VISIBLE);                datosSensor1.setTextColor(0xff0000cc);                datosSensor2.setTextColor(0xff0000cc);                datosSensor3.setTextColor(0xff0000cc);                datosSensor4.setTextColor(0xff0000cc);                sensores = mNXTTalker.buffer;                if (sensores[0]!=0){                    instrucciones.setVisibility(View.INVISIBLE);                }else{                    instrucciones.setVisibility(View.VISIBLE);                }                if ((sensores[6]) == -1) {                    datosSensor2.setTextColor(0xffff0000);                    datosSensor2.setText("Dato incorreto");                } else {                    datosSensor1.setText(Byte.toString(sensores[6]));                }                if ((sensores[7]) == -1) {                    datosSensor2.setTextColor(0xffff0000);                    datosSensor2.setText("Dato incorreto");                } else {                    datosSensor2.setText(Byte.toString(sensores[7]) + " cm");                }                datosSensor3.setText("R: " + Byte.toString(sensores[8]) + " |   G: " + Byte.toString(sensores[9]) + " |   B: " + Byte.toString(sensores[10]));                datosSensor4.setText(Byte.toString(sensores[11]) + " lux");            }        });        mNXTTalker = new NXTTalker(mHandler);    }    private final Handler mHandler = new Handler() {        @Override        public void handleMessage(Message msg) {            switch (msg.what) {                case MESSAGE_TOAST:                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();                    break;                case MESSAGE_STATE_CHANGE:                    mState = msg.arg1;                    break;            }        }    };    //Método que intenta la conexion con un dispositivo    private void startConnection(){        Intent intent = new Intent(this, SearchDeviceActivity.class);        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);    }    //Metodo para mover los motores dependiendo de la intensidad("power") que tenga    public void moverMotores(byte a, byte b, byte c){        a = (byte) (Power*a);        b = (byte) (Power*b);        c = (byte) (Power*c);        mNXTTalker.motors3(a, b, c, false, false);    }    @Override    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        switch (requestCode) {            case REQUEST_ENABLE_BT:                if (resultCode == Activity.RESULT_OK) {                    startConnection();                } else {                    Toast.makeText(this, "Bluetooth not enabled, exiting.", Toast.LENGTH_LONG).show();                    finish();                }                break;            case REQUEST_CONNECT_DEVICE:                if (resultCode == Activity.RESULT_OK) {                    String address = data.getExtras().getString(SearchDeviceActivity.EXTRA_DEVICE_ADDRESS);                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);                    mDeviceAddress = address;                    mNXTTalker.connect(device);                }                break;            case REQUEST_SETTINGS:                //XXX?                break;        }    }    @Override    protected void onSaveInstanceState(Bundle outState) {        super.onSaveInstanceState(outState);        if (mState == NXTTalker.STATE_CONNECTED) {            outState.putString("device_address", mDeviceAddress);        }    }}