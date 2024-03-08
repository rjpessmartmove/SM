package com.example.sm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.device.ScanManager;
import android.device.scanner.configuration.Constants;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// https://www.javatpoint.com/android-toast-example
// https://pt.stackoverflow.com/questions/31502/acessando-webservice-pelo-android-ksoap2
// https://www.it-swarm.dev/pt/android/como-chamar-um-webservice-.net-do-android-usando-o-ksoap2/967214624/

// AppCompatActivity
public class MainActivity extends Activity
{
    private Boolean DEBUG = false;

    private static final String TAG = "SmartAccess";
    private Boolean ws_init = false;
    private Boolean doInitWs = false;

    private String Cliente = "";
    private String url = "";
    private String namespace = "";
    private String servicename = "";
    private int wsVersion = 1;
    private Integer TerminalID = 215;
    private String Barcode = "";

    private CountDownTimer semaforeTimer = null;

    private ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);

    private ArrayList<String> Recintos = new ArrayList<String>();
    private ArrayList<Integer> Terminais = new ArrayList<Integer>();

    private Integer Tecnologia = 1;
    private Integer Sentido;


    private String portaTerminal = "";
    private String eventoCodigo = "";
    private String eventoNome = "";
    private String eventoData = "";
    private ArrayList<String> Jogos_Ids = new ArrayList<String>();
    private ArrayList<String> Jogos_Nomes = new ArrayList<String>();
    private ArrayList<String> Jogos_Datas = new ArrayList<String>();


    private String ReturnMessage = "";
    private String ReturnMessage_Alt = "";
    private String Owner = "";

    private long nEntradas_OK = 0;
    private long nEntradas_NotOK = 0;

    private WebService ws_sma_acess = null;

    private boolean startAccess = false;

    private ScanManager mScanManager = null;
    private static boolean mScanEnable = true;
    private static final String ACTION_DECODE = ScanManager.ACTION_DECODE;
    private static final String ACTION_DECODE_IMAGE_REQUEST = "action.scanner_capture_image";
    private static final String ACTION_CAPTURE_IMAGE = "scanner_capture_image_result";
    private static final String BARCODE_STRING_TAG = ScanManager.BARCODE_STRING_TAG;
    private static final String BARCODE_TYPE_TAG = ScanManager.BARCODE_TYPE_TAG;
    private static final String BARCODE_LENGTH_TAG = ScanManager.BARCODE_LENGTH_TAG;
    private static final String DECODE_DATA_TAG = ScanManager.DECODE_DATA_TAG;
    private static final String DECODE_ENABLE = "decode_enable";
    private static final String DECODE_TRIGGER_MODE = "decode_trigger_mode";
    private static final String DECODE_TRIGGER_MODE_HOST = "HOST";
    private static final String DECODE_TRIGGER_MODE_CONTINUOUS = "CONTINUOUS";
    private static final String DECODE_TRIGGER_MODE_PAUSE = "PAUSE";
    private static String DECODE_TRIGGER_MODE_CURRENT = DECODE_TRIGGER_MODE_HOST;
    private static final int DECODE_OUTPUT_MODE_INTENT = 0;
    private static final int DECODE_OUTPUT_MODE_FOCUS = 1;
    private static int DECODE_OUTPUT_MODE_CURRENT = DECODE_OUTPUT_MODE_FOCUS;
    private static final String DECODE_OUTPUT_MODE = "decode_output_mode";
    private static final String DECODE_CAPTURE_IMAGE_KEY = "bitmapBytes";
    private static final String DECODE_CAPTURE_IMAGE_SHOW = "scan_capture_image";
    private static final int MSG_SHOW_SCAN_RESULT = 1;
    private static final int MSG_SHOW_SCAN_IMAGE = 2;
    private static boolean mScanCaptureImageShow = false;
    private ImageView mScanImage = null;
    private String scanResult = null;
    private static Map<String, BarcodeHolder> mBarcodeMap = new HashMap<String, BarcodeHolder>();
    private int scannerIndex = 0; // Keep the selected scanner
    private int defaultIndex = 0; // Keep the default scanner
    private int dataLength = 0;
    private String statusString = "";

    private boolean bSoftTriggerSelected = false;
    private boolean bDecoderSettingsChanged = true; //false;
    private boolean bExtScannerDisconnected = false;
    private final Object lock = new Object();

    private Spinner spinnerScannerDevices = null;

    private TextView textTitulo = null;
    private TextView textEvento = null;
    private TextView textData = null;
    private TextView textTerminal = null;
    private TextView textMsgAccess = null;
    private TextView textNumAcessos = null;

    private ImageView semaforoVerde = null;
    private ImageView semaforoVermelho = null;

    private ListView termList;
    private ListView eventList;
    private Button butNumAcessos;
    private static final int[] SCAN_KEYCODE = {520, 521, 522, 523};

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logD("onReceive , action:" + action);
            // Get Scan Image . Make sure to make a request before getting a scanned image
            if (ACTION_CAPTURE_IMAGE.equals(action)) {
                byte[] imageData = intent.getByteArrayExtra(DECODE_CAPTURE_IMAGE_KEY);
                if (imageData != null && imageData.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_IMAGE);
                    msg.obj = bitmap;
                    mHandler.sendMessage(msg);
                } else {
                    logD("onReceive , ignore imageData:" + imageData);
                }
            } else {
                // Get scan results, including string and byte data etc.
                byte[] barcode = intent.getByteArrayExtra(DECODE_DATA_TAG);
                int barcodeLen = intent.getIntExtra(BARCODE_LENGTH_TAG, 0);
                byte temp = intent.getByteExtra(BARCODE_TYPE_TAG, (byte) 0);
                String barcodeStr = intent.getStringExtra(BARCODE_STRING_TAG);
                if (mScanCaptureImageShow) {
                    // Request images of this scan
                    context.sendBroadcast(new Intent(ACTION_DECODE_IMAGE_REQUEST));
                }
                logD("barcode type:" + temp);
                String scanResult = new String(barcode, 0, barcodeLen);
                // print scan results.
                //scanResult = " length：" + barcodeLen + "\nbarcode：" + scanResult + "\nbytesToHexString：" + bytesToHexString(barcode) + "\nbarcodeStr:" + barcodeStr;
                scanResult = barcodeStr;
                Message msg = mHandler.obtainMessage(MSG_SHOW_SCAN_RESULT);
                msg.obj = scanResult;
                mHandler.sendMessage(msg);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_SHOW_SCAN_RESULT:
                    Barcode = (String) msg.obj;
                    if (startAccess)
                        BarcodeVerify();
                    break;
                case MSG_SHOW_SCAN_IMAGE:
                    if (mScanImage != null && mScanCaptureImageShow) {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        mScanImage.setImageBitmap(bitmap);
                        mScanImage.setVisibility(View.VISIBLE);
                    } else {
                        mScanCaptureImageShow = false;
                        mScanImage.setVisibility(View.INVISIBLE);
                        logD("handleMessage , MSG_SHOW_SCAN_IMAGE scan image:" + mScanImage);
                    }
                    break;
            }
        }
    };


    /**
     * @param register , ture register , false unregister
     */
    private void registerReceiver(boolean register) {
        if (register && mScanManager != null) {
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(ACTION_DECODE);
            }
            filter.addAction(ACTION_CAPTURE_IMAGE);

            registerReceiver(mReceiver, filter);
        } else if (mScanManager != null) {
            mScanManager.stopDecode();
            unregisterReceiver(mReceiver);
        }
    }
    /**
     * byte[] toHex String
     *
     * @param src
     * @return String
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Interface UI
        textTitulo = (TextView) findViewById(R.id.textTitle);
        textEvento = (TextView) findViewById(R.id.textEvent);
        textData = (TextView) findViewById(R.id.textDate);
        textTerminal = (TextView) findViewById(R.id.textTerminal);
        textMsgAccess = (TextView) findViewById(R.id.textMsgAccess);
        textNumAcessos= (TextView) findViewById(R.id.textNumAcessos);

        semaforoVerde = (ImageView) findViewById(R.id.imageGreen);
        semaforoVermelho = (ImageView) findViewById(R.id.imageRed);

        termList = (ListView) findViewById(R.id.termList);
        termList.setBackgroundColor(Color.WHITE);
        termList.setVisibility(View.INVISIBLE);

        eventList = (ListView) findViewById(R.id.termList);
        eventList.setBackgroundColor(Color.WHITE);
        eventList.setVisibility(View.INVISIBLE);

        butNumAcessos = (Button) findViewById(R.id.butNumAcessos);
        butNumAcessos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetAcessosDiaEvento();
            }
        });

        hideSemaforos();

        spinnerScannerDevices = (Spinner)findViewById(R.id.spinnerScannerDevices);

        nEntradas_OK = 0;
        nEntradas_NotOK = 0;

        // Obter valores do ficheiro de configuração
        OpenConfigFile();

        updateTitulo();
        updateTituloEscolha("ESCOLHA EVENTO/LOCAL");

        if (!Recintos.isEmpty())
        {
            ArrayAdapter arrTerm = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Recintos);
            termList.setVisibility(View.VISIBLE);
            termList.setAdapter(arrTerm);
            termList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TerminalID = Terminais.get(i);
                    termList.setVisibility(View.INVISIBLE);
                    StartControl();
                }
            });
        }
        else
            StartControl();

        mScanCaptureImageShow = getDecodeScanShared(DECODE_CAPTURE_IMAGE_SHOW);
    }

    private boolean getDecodeScanShared(String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enable = sharedPrefs.getBoolean(key, true);
        return enable;
    }
    /**
     * Obtém eventos activos e escolhe um.
     * @return true, se não ocorrer erro.
     */
    private Thread GetActiveEventList()
    {
        Thread eventSel = null;

        try
        {
            eventSel = new Thread()
            {
                public void run()
                {
                    try
                    {
                        String response = ws_sma_acess.InitService(TerminalID);
                        ws_init = Boolean.parseBoolean(response);

                        if (!ws_init) return;

                        switch (wsVersion) {
                            case 1:
                                return;
                        }

                        response = ws_sma_acess.GetActiveEventsList(TerminalID);
                        Element nJogo;
                        String id, nome, data;
                        Jogos_Ids.clear();;
                        Jogos_Nomes.clear();
                        Jogos_Datas.clear();

                        try
                        {
                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            InputSource is = new InputSource(new StringReader(response));
                            Document doc = dBuilder.parse(is);
                            Element element = doc.getDocumentElement();
                            element.normalize();
                            NodeList nListJogos = doc.getElementsByTagName("Jogo");
                            for (int j = 0; j < nListJogos.getLength(); j++) {
                                nJogo = (Element) nListJogos.item(j);
                                id = nJogo.getAttribute("Codigo");
                                nome = nJogo.getAttribute("Nome");
                                data = nJogo.getAttribute("Data");
                                Jogos_Ids.add(j, id);
                                Jogos_Nomes.add(j, nome);
                                Jogos_Datas.add(j, data);
                            }
                        }
                        catch (Exception ex)
                        {
                            Jogos_Ids.clear();;
                            Jogos_Nomes.clear();
                            Jogos_Datas.clear();
                        }
                    }
                    catch (Exception ex)
                    {
                        logD("ERROR (GetActiveEventList): " + ex.fillInStackTrace());
                    }
                }
            };
            eventSel.start();

            return eventSel;
        }
        catch (Exception ex)
        {
            Jogos_Ids.clear();;
            Jogos_Nomes.clear();
            Jogos_Datas.clear();
            return null;
        }

    }

    private void StartControl() {

        ws_sma_acess = new WebService(url, namespace, servicename, wsVersion);
        updateMsgAccess("Serviço em inicialização. Aguarde um instante ... ");

        eventoCodigo = "";
        eventoNome = "";
        eventoData = "";

        Thread evtSel = GetActiveEventList();
        if (evtSel != null) {
            try {
                evtSel.join();
            }
            catch (Exception ex){
            }

        }

        if (Jogos_Ids.size() == 0)
        {
            doInitWs = true;
            Init();
        }
        else if (Jogos_Ids.size() == 1)
        {
            eventoCodigo = Jogos_Ids.get(0);
            eventoNome = Jogos_Nomes.get(0);
            eventoData = Jogos_Datas.get(0);
            Init();
        }
        else
        {
            updateTituloEscolha("ESCOLHA EVENTO");
            ArrayAdapter arrTerm = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Jogos_Nomes);
            eventList.setVisibility(View.VISIBLE);
            eventList.setAdapter(arrTerm);
            eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    eventoCodigo = Jogos_Ids.get(i);
                    eventoNome = Jogos_Nomes.get(i);
                    eventoData = Jogos_Datas.get(i);
                    eventList.setVisibility(View.INVISIBLE);
                    Init();
                }
            });
        }

    }

    /**
     * Inicialização da aplicação.
     */
    private void Init()
    {
        try
        {
            new Thread(new Runnable()
            {
                public void run()
                {

                    if (doInitWs) {
                        String response = ws_sma_acess.InitService(TerminalID);
                        ws_init = Boolean.parseBoolean(response);
                    }
                    if (ws_init)
                    {
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                portaTerminal = "";
                                String response = ws_sma_acess.GetPortaTerminal(TerminalID);
                                portaTerminal = response;
                                updateTerminal(portaTerminal);

                                if (portaTerminal != null && portaTerminal != "") {
                                    GetActiveEvent();
                                    GetAcessosDiaEvento();
                                }

                            }
                        }).start();
                    }
                    else
                    {
                        new Thread(new Runnable()
                        {
                            public void run() {
                                updateMsgAccess("Serviço indisponivel.");
                            }
                        }).start();
                    }

                }
            }).start();

        }
        catch (Exception ex)
        {
            //textLog.append("ERROR Init: " + ex.getMessage() + "\n");
        }
    }

    /**
     * Obtém evento activo.
     * @return true, se não ocorrer erro.
     */
    private boolean GetActiveEvent()
    {
        try
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    if (eventoCodigo == "") {
                        String response = WebService.GetActiveEvent(TerminalID);

                        String[] arrayString = response.split(";");

                        eventoCodigo = arrayString[2];
                        eventoNome = arrayString[4];
                        eventoData = arrayString[6];
                    }
                    updateEvent(eventoNome);
                    updateDate(eventoData);

                    updateMsgAccess("Próximo acesso...");

                    startAccess = true;
                }
            }).start();

        }
        catch (Exception ex)
        {
            logD("ERROR (GetActiveEvent): " + ex.fillInStackTrace());
            return false;
        }

        return true;
    }

    private boolean GetAcessosDiaEvento()
    {
        try
        {
            new Thread(new Runnable()
            {
                public void run() {
                    try {
                        String response = WebService.GetAcessosDia(TerminalID, eventoCodigo);
                        String[] arrayString = response.split(";");
                        nEntradas_OK = Long.parseLong(arrayString[2]);
                        updateNumAcessos(nEntradas_OK);
                    } catch (Exception ex) {

                    }
                }
            }).start();

        }
        catch (Exception ex)
        {
            logD("ERROR (GetAcessosDiaEvento): " + ex.fillInStackTrace());
            return false;
        }

        return true;
    }

    /**
     *
     */
    private void BarcodeVerify()
    {
        Thread thread = new Thread()
        {
            String response = "";

            public void run()
            {
                try
                {
                    logD("Barcode string: "+Barcode);
                    response = ws_sma_acess.BarcodeVerify(TerminalID, Barcode, Tecnologia, Sentido, eventoCodigo);
                    Boolean result = Boolean.FALSE;
                    String[] arrayString = response.split(";");

                    if (arrayString[0].equals("ERROR"))
                    {
                        ReturnMessage = "Tente de novo p.f.";
                        Owner = "";
                    }
                    else {
                        result = Boolean.parseBoolean(arrayString[2]);
                        ReturnMessage = arrayString[4];
                        Owner = arrayString[7];
                    }
                    if (result)
                    {
                        nEntradas_OK ++;
                        updateMsgAccess(ReturnMessage);// + "  " +  Owner + "  N.º OK: " + nEntradas_OK);
                        updateSemaforos(1);
                        updateNumAcessos(nEntradas_OK);
                    }
                    else
                    {
                        nEntradas_NotOK ++;
                        updateMsgAccess(ReturnMessage);// + "  N.º Not OK: " + nEntradas_NotOK);
                        updateSemaforos(0);
                    }

                    arrayString = null;
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    response = "ERROR" + ex.toString();

                    logD("ERROR: " + response);
                }
            }
        };

        try
        {
            thread.join();
            thread.start();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initScan();
        registerReceiver(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        registerReceiver(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    public void softScan(View view)
    {
        bSoftTriggerSelected = true;
        cancelRead();
    }

    private void cancelRead()
    {
        if (mScanManager != null){
            mScanManager.stopDecode();
        }
    }


    private void initScan() {
        mScanManager = new ScanManager();
        boolean powerOn = mScanManager.getScannerState();
        if (!powerOn) {
            powerOn = mScanManager.openScanner();
            if (!powerOn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("O Scanner não pode ser ligado!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();
            }
        }
        initBarcodeParameters();
        mScanManager.switchOutputMode(0);
    }

    /**
     * ScanManager.getTriggerLockState
     *
     * @return
     */
    private boolean getlockTriggerState() {
        boolean state = mScanManager.getTriggerLockState();
        return state;
    }
    /**
     * ScanManager.startDecode
     */
    private void startDecode() {
        if (!mScanEnable) {
            logD("startDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        boolean lockState = getlockTriggerState();
        if (lockState) {
            logD("startDecode ignore, Scan lockTrigger state:" + lockState);
            return;
        }
        if (mScanManager != null) {
            mScanManager.startDecode();
        }
    }

    /**
     * ScanManager.stopDecode
     */
    private void stopDecode() {
        if (!mScanEnable) {
            logD("stopDecode ignore, Scan enable:" + mScanEnable);
            return;
        }
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
    }

    private void deInitScanner()
    {
        if (mScanManager!=null){
            mScanManager.closeScanner();
        }
        mScanManager = null;
    }

    // XML configuration file

    private void OpenConfigFile()
    {
        try
        {
            InputStream is = null;
            boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);

            //is = getAssets().open("AppConfig_GDC.xml");
            //is = getAssets().open("AppConfig_WS.xml");
            is = getAssets().open("AppConfig_DEBUG.xml");
            //is = getAssets().open("AppConfig_EMA.xml");
            //is = getAssets().open("AppConfig_GIL.xml");
            //is = getAssets().open("AppConfig_CDN.xml");
            //is = getAssets().open("AppConfig_BELENENSES.xml");
            //is = getAssets().open("AppConfig_CF.xml");
            //is = getAssets().open("AppConfig_FAM.xml");
            //is = getAssets().open("AppConfig_FPF_FARO.xml");
            //is = getAssets().open("AppConfig_WOW.xml");
            //is = getAssets().open("AppConfig_VIZELA.xml");
            //is = getAssets().open("AppConfig.xml");
            //is = getAssets().open("AppConfig_VSC.xml");
            //is = getAssets().open("AppConfig_VSC_PAV.xml");
            //is = getAssets().open("AppConfig_SCBRAGA.xml");
            //is = getAssets().open("AppConfig_ESTORIL.xml");
            //is = getAssets().open("AppConfig.xml");
            //is = getAssets().open("AppConfig_VARZIM.xml");
            //is = getAssets().open("AppConfig_FPF_JAMOR.xml");
            //is = getAssets().open("AppConfig_FPBASKET.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            is.close();

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("appSettings");

            for (int i=0; i<nList.getLength(); i++) {

                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element2 = (Element) node;

                    url = getValue("WebServerURL", element2);
                    namespace = getValue("WebServerNameSpace", element2);
                    servicename = getValue("WebServiceName", element2);
                    if (servicename.equals("-")) servicename = "";
                    wsVersion = Integer.parseInt(getValue("WebServerVersion", element2));
                    TerminalID = Integer.parseInt(getValue("TerminalID", element2));
                    String SentidoStr = getValue("EntradaSaida", element2);
                    Sentido = ((int) SentidoStr.charAt(0));

                    try { Cliente = getValue("Cliente", element2); } catch (Exception ex) { Cliente = "";}
                }
            }

            try
            {
                NodeList nListTerminais = doc.getElementsByTagName("Terminal");
                for (int j = 0; j < nListTerminais.getLength(); j++)
                {
                    Element nTerminal = (Element)nListTerminais.item(j);
                    Integer idTerm = Integer.parseInt(nTerminal.getAttribute("Id"));
                    String sRecinto = nTerminal.getAttribute("Evento");
                    Recintos.add(j, sRecinto);
                    Terminais.add(j, idTerm);
                }
            }
            catch (Exception ex)
            {
                Recintos.clear();
                Terminais.clear();
            }

        } catch (Exception ex) {
            //textLog.setText("ERROR: " + ex.getMessage() + "\n");
        }

    }

    private static String getValue(String tag, Element element)
    {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    private static boolean setValue(String tag, Element element, String newValue)
    {
        try {
            NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
            Node node = nodeList.item(0);
            node.setNodeValue(newValue);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }


    private class MyTask extends AsyncTask<Void, Void, Void>
    {
        String result;

        @Override
        protected Void doInBackground(Void... voids)
        {


            try
            {
                result = ws_sma_acess.InitService(TerminalID);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                result = "ERROR" + ex.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            //textLog.append(result + "\n");
            super.onPostExecute(aVoid);
        }
    }


    // Interface UI
    private void updateTitulo()
    {
        String tituloBase = "SmartAccess";
        if (Cliente != "") tituloBase = tituloBase + "-" + Cliente;
        final String titulo = tituloBase;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTitulo.setText(titulo.trim()); ;
            }
        });
    }


    private void updateEvent(final String evento)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textEvento.setText(evento.trim()); ;
            }
        });
    }

    private void updateTituloEscolha(final String titulo)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textEvento.setText(titulo.trim()); ;
            }
        });
    }

    private void updateDate(final String data)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textData.setText(data); ;
            }
        });
    }

    private void updateTerminal(final String terminal)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textTerminal.setText("Terminal: " + terminal); ;
            }
        });
    }

    private void updateMsgAccess(final String text)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textMsgAccess.setText(text);
            }
        });
    }

    private void updateNumAcessos(final long NumAcessos)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textNumAcessos.setText("Acessos: " + Long.toString(NumAcessos));
            }
        });
    }

    private void logD(final String result)
    {
        if (DEBUG) {
            android.util.Log.d(TAG, result);
        }

    }

    private void SemaforoTimer()
    {
        semaforeTimer = new CountDownTimer(2000, 1000)
        {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                hideSemaforos();
                updateMsgAccess("Próximo acesso...");
                semaforeTimer =null;
            }
        };
        semaforeTimer.start();
    }

    private void hideSemaforos()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                semaforoVerde.setVisibility(View.INVISIBLE);
                semaforoVermelho.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void updateSemaforos(final int status)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (semaforeTimer != null)
                {
                    semaforeTimer.cancel();
                }
                hideSemaforos();
                semaforeTimer = null;

                //ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                if (status == 1)
                {   // Com Acesso
                    semaforoVerde.setVisibility(View.VISIBLE);
                    semaforoVermelho.setVisibility(View.INVISIBLE);
                    toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,200);

                }
                else
                {   // Sem Acesso
                    semaforoVerde.setVisibility(View.INVISIBLE);
                    semaforoVermelho.setVisibility(View.VISIBLE);
                    for (int j = 1; j <=3; j++) {
                        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,200);
                        try { Thread.sleep(150);}
                        catch (InterruptedException e) {}
                    }

                }

                SemaforoTimer();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        logD("onKeyUp, keyCode:" + keyCode);
        if (keyCode >= SCAN_KEYCODE[0] && keyCode <= SCAN_KEYCODE[SCAN_KEYCODE.length - 1]) {
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        logD("onKeyDown, keyCode:" + keyCode);
        if (keyCode >= SCAN_KEYCODE[0] && keyCode <= SCAN_KEYCODE[SCAN_KEYCODE.length - 1]) {
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
            * mBarcodeMap helper
     */
    private void initBarcodeParameters() {
        mBarcodeMap.clear();
        BarcodeHolder holder = new BarcodeHolder();
        // Symbology.AZTEC
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.AZTEC_ENABLE};
        holder.mParaKeys = new String[]{"AZTEC_ENABLE"};
        mBarcodeMap.put(Symbology.AZTEC + "", holder);
        // Symbology.CHINESE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.C25_ENABLE};
        holder.mParaKeys = new String[]{"C25_ENABLE"};
        mBarcodeMap.put(Symbology.CHINESE25 + "", holder);
        // Symbology.CODABAR
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeNOTIS = new CheckBoxPreference(this);
        holder.mBarcodeCLSI = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODABAR_ENABLE, PropertyID.CODABAR_LENGTH1, PropertyID.CODABAR_LENGTH2, PropertyID.CODABAR_NOTIS, PropertyID.CODABAR_CLSI};
        holder.mParaKeys = new String[]{"CODABAR_ENABLE", "CODABAR_LENGTH1", "CODABAR_LENGTH2", "CODABAR_NOTIS", "CODABAR_CLSI"};
        mBarcodeMap.put(Symbology.CODABAR + "", holder);
        // Symbology.CODE11
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeCheckDigit = new ListPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE11_ENABLE, PropertyID.CODE11_LENGTH1, PropertyID.CODE11_LENGTH2, PropertyID.CODE11_SEND_CHECK};
        holder.mParaKeys = new String[]{"CODE11_ENABLE", "CODE11_LENGTH1", "CODE11_LENGTH2", "CODE11_SEND_CHECK"};
        mBarcodeMap.put(Symbology.CODE11 + "", holder);
        // Symbology.CODE32
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE32_ENABLE};
        holder.mParaKeys = new String[]{"CODE32_ENABLE"};
        mBarcodeMap.put(Symbology.CODE32 + "", holder);
        // Symbology.CODE39
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mBarcodeFullASCII = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE39_ENABLE, PropertyID.CODE39_LENGTH1, PropertyID.CODE39_LENGTH2, PropertyID.CODE39_ENABLE_CHECK, PropertyID.CODE39_SEND_CHECK, PropertyID.CODE39_FULL_ASCII};
        holder.mParaKeys = new String[]{"CODE39_ENABLE", "CODE39_LENGTH1", "CODE39_LENGTH2", "CODE39_ENABLE_CHECK", "CODE39_SEND_CHECK", "CODE39_FULL_ASCII"};
        mBarcodeMap.put(Symbology.CODE39 + "", holder);
        // Symbology.CODE93
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE93_ENABLE, PropertyID.CODE93_LENGTH1, PropertyID.CODE93_LENGTH2};
        holder.mParaKeys = new String[]{"CODE93_ENABLE", "CODE93_LENGTH1", "CODE93_LENGTH2"};
        mBarcodeMap.put(Symbology.CODE93 + "", holder);
        // Symbology.CODE128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeISBT = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE128_ENABLE, PropertyID.CODE128_LENGTH1, PropertyID.CODE128_LENGTH2, PropertyID.CODE128_CHECK_ISBT_TABLE};
        holder.mParaKeys = new String[]{"CODE128_ENABLE", "CODE128_LENGTH1", "CODE128_LENGTH2", "CODE128_CHECK_ISBT_TABLE"};
        mBarcodeMap.put(Symbology.CODE128 + "", holder);
        // Symbology.COMPOSITE_CC_AB
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.COMPOSITE_CC_AB_ENABLE};
        holder.mParaKeys = new String[]{"COMPOSITE_CC_AB_ENABLE"};
        mBarcodeMap.put(Symbology.COMPOSITE_CC_AB + "", holder);
        // Symbology.COMPOSITE_CC_C
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.COMPOSITE_CC_C_ENABLE};
        holder.mParaKeys = new String[]{"COMPOSITE_CC_C_ENABLE"};
        mBarcodeMap.put(Symbology.COMPOSITE_CC_C + "", holder);
        // Symbology.DATAMATRIX
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.DATAMATRIX_ENABLE};
        holder.mParaKeys = new String[]{"DATAMATRIX_ENABLE"};
        mBarcodeMap.put(Symbology.DATAMATRIX + "", holder);
        // Symbology.DISCRETE25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.D25_ENABLE};
        holder.mParaKeys = new String[]{"D25_ENABLE"};
        mBarcodeMap.put(Symbology.DISCRETE25 + "", holder);
        // Symbology.EAN8
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.EAN8_ENABLE};
        holder.mParaKeys = new String[]{"EAN8_ENABLE"};
        mBarcodeMap.put(Symbology.EAN8 + "", holder);
        // Symbology.EAN13
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeBookland = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.EAN13_ENABLE, PropertyID.EAN13_BOOKLANDEAN};
        holder.mParaKeys = new String[]{"EAN13_ENABLE", "EAN13_BOOKLANDEAN"};
        mBarcodeMap.put(Symbology.EAN13 + "", holder);
        // Symbology.GS1_14
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_14_ENABLE};
        holder.mParaKeys = new String[]{"GS1_14_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_14 + "", holder);
        // Symbology.GS1_128
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.CODE128_GS1_ENABLE};
        holder.mParaKeys = new String[]{"CODE128_GS1_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_128 + "", holder);
        // Symbology.GS1_EXP
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_EXP_ENABLE, PropertyID.GS1_EXP_LENGTH1, PropertyID.GS1_EXP_LENGTH2};
        holder.mParaKeys = new String[]{"GS1_EXP_ENABLE", "GS1_EXP_LENGTH1", "GS1_EXP_LENGTH2"};
        mBarcodeMap.put(Symbology.GS1_EXP + "", holder);
        // Symbology.GS1_LIMIT
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.GS1_LIMIT_ENABLE};
        holder.mParaKeys = new String[]{"GS1_LIMIT_ENABLE"};
        mBarcodeMap.put(Symbology.GS1_LIMIT + "", holder);
        // Symbology.INTERLEAVED25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.I25_ENABLE, PropertyID.I25_LENGTH1, PropertyID.I25_LENGTH2, PropertyID.I25_ENABLE_CHECK, PropertyID.I25_SEND_CHECK};
        holder.mParaKeys = new String[]{"I25_ENABLE", "I25_LENGTH1", "I25_LENGTH2", "I25_ENABLE_CHECK", "I25_SEND_CHECK"};
        mBarcodeMap.put(Symbology.INTERLEAVED25 + "", holder);
        // Symbology.MATRIX25
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.M25_ENABLE};
        holder.mParaKeys = new String[]{"M25_ENABLE"};
        mBarcodeMap.put(Symbology.MATRIX25 + "", holder);
        // Symbology.MAXICODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MAXICODE_ENABLE};
        holder.mParaKeys = new String[]{"MAXICODE_ENABLE"};
        mBarcodeMap.put(Symbology.MAXICODE + "", holder);
        // Symbology.MICROPDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MICROPDF417_ENABLE};
        holder.mParaKeys = new String[]{"MICROPDF417_ENABLE"};
        mBarcodeMap.put(Symbology.MICROPDF417 + "", holder);
        // Symbology.MSI
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeLength1 = new EditTextPreference(this);
        holder.mBarcodeLength2 = new EditTextPreference(this);
        holder.mBarcodeSecondChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSendCheck = new CheckBoxPreference(this);
        holder.mBarcodeSecondChecksumMode = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.MSI_ENABLE, PropertyID.MSI_LENGTH1, PropertyID.MSI_LENGTH2, PropertyID.MSI_REQUIRE_2_CHECK, PropertyID.MSI_SEND_CHECK, PropertyID.MSI_CHECK_2_MOD_11};
        holder.mParaKeys = new String[]{"MSI_ENABLE", "MSI_LENGTH1", "MSI_LENGTH2", "MSI_REQUIRE_2_CHECK", "MSI_SEND_CHECK", "MSI_CHECK_2_MOD_11"};
        mBarcodeMap.put(Symbology.MSI + "", holder);
        // Symbology.PDF417
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.PDF417_ENABLE};
        holder.mParaKeys = new String[]{"PDF417_ENABLE"};
        mBarcodeMap.put(Symbology.PDF417 + "", holder);
        // Symbology.QRCODE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.QRCODE_ENABLE};
        holder.mParaKeys = new String[]{"QRCODE_ENABLE"};
        mBarcodeMap.put(Symbology.QRCODE + "", holder);
        // Symbology.TRIOPTIC
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.TRIOPTIC_ENABLE};
        holder.mParaKeys = new String[]{"TRIOPTIC_ENABLE"};
        mBarcodeMap.put(Symbology.TRIOPTIC + "", holder);
        // Symbology.UPCA
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(this);
        holder.mBarcodeConvertEAN13 = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCA_ENABLE, PropertyID.UPCA_SEND_CHECK, PropertyID.UPCA_SEND_SYS, PropertyID.UPCA_TO_EAN13};
        holder.mParaKeys = new String[]{"UPCA_ENABLE", "UPCA_SEND_CHECK", "UPCA_SEND_SYS", "UPCA_TO_EAN13"};
        mBarcodeMap.put(Symbology.UPCA + "", holder);
        // Symbology.UPCE
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mBarcodeChecksum = new CheckBoxPreference(this);
        holder.mBarcodeSystemDigit = new CheckBoxPreference(this);
        holder.mBarcodeConvertUPCA = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCE_ENABLE, PropertyID.UPCE_SEND_CHECK, PropertyID.UPCE_SEND_SYS, PropertyID.UPCE_TO_UPCA};
        holder.mParaKeys = new String[]{"UPCE_ENABLE", "UPCE_SEND_CHECK", "UPCE_SEND_SYS", "UPCE_TO_UPCA"};
        mBarcodeMap.put(Symbology.UPCE + "", holder);
        // Symbology.UPCE1
        holder = new BarcodeHolder();
        holder.mBarcodeEnable = new CheckBoxPreference(this);
        holder.mParaIds = new int[]{PropertyID.UPCE1_ENABLE};
        holder.mParaKeys = new String[]{"UPCE1_ENABLE"};
        mBarcodeMap.put(Symbology.UPCE1 + "", holder);
    }
    /**
     * BarcodeHolder helper
     */
    static class BarcodeHolder {
        CheckBoxPreference mBarcodeEnable = null;
        EditTextPreference mBarcodeLength1 = null;
        EditTextPreference mBarcodeLength2 = null;

        CheckBoxPreference mBarcodeNOTIS = null;
        CheckBoxPreference mBarcodeCLSI = null;

        CheckBoxPreference mBarcodeISBT = null;
        CheckBoxPreference mBarcodeChecksum = null;
        CheckBoxPreference mBarcodeSendCheck = null;
        CheckBoxPreference mBarcodeFullASCII = null;
        ListPreference mBarcodeCheckDigit = null;
        CheckBoxPreference mBarcodeBookland = null;
        CheckBoxPreference mBarcodeSecondChecksum = null;
        CheckBoxPreference mBarcodeSecondChecksumMode = null;
        ListPreference mBarcodePostalCode = null;
        CheckBoxPreference mBarcodeSystemDigit = null;
        CheckBoxPreference mBarcodeConvertEAN13 = null;
        CheckBoxPreference mBarcodeConvertUPCA = null;
        CheckBoxPreference mBarcodeEanble25DigitExtensions = null;
        CheckBoxPreference mBarcodeDPM = null;
        int[] mParaIds = null;
        String[] mParaKeys = null;
    }

    /**
     * Use of android.device.scanner.configuration.Constants.Symbology Class
     */
    private int[] BARCODE_SYMBOLOGY = new int[]{
            Constants.Symbology.AZTEC,
            Constants.Symbology.CHINESE25,
            Constants.Symbology.CODABAR,
            Constants.Symbology.CODE11,
            Constants.Symbology.CODE32,
            Constants.Symbology.CODE39,
            Constants.Symbology.CODE93,
            Constants.Symbology.CODE128,
            Constants.Symbology.COMPOSITE_CC_AB,
            Constants.Symbology.COMPOSITE_CC_C,
            Constants.Symbology.COMPOSITE_TLC39,
            Constants.Symbology.DATAMATRIX,
            Constants.Symbology.DISCRETE25,
            Constants.Symbology.EAN8,
            Constants.Symbology.EAN13,
            Constants.Symbology.GS1_14,
            Constants.Symbology.GS1_128,
            Constants.Symbology.GS1_EXP,
            Constants.Symbology.GS1_LIMIT,
            Constants.Symbology.HANXIN,
            Constants.Symbology.INTERLEAVED25,
            Constants.Symbology.MATRIX25,
            Constants.Symbology.MAXICODE,
            Constants.Symbology.MICROPDF417,
            Constants.Symbology.MSI,
            Constants.Symbology.PDF417,
            Constants.Symbology.POSTAL_4STATE,
            Constants.Symbology.POSTAL_AUSTRALIAN,
            Constants.Symbology.POSTAL_JAPAN,
            Constants.Symbology.POSTAL_KIX,
            Constants.Symbology.POSTAL_PLANET,
            Constants.Symbology.POSTAL_POSTNET,
            Constants.Symbology.POSTAL_ROYALMAIL,
            Constants.Symbology.POSTAL_UPUFICS,
            Constants.Symbology.QRCODE,
            Constants.Symbology.TRIOPTIC,
            Constants.Symbology.UPCA,
            Constants.Symbology.UPCE,
            Constants.Symbology.UPCE1,
            Constants.Symbology.NONE,
            Constants.Symbology.RESERVED_6,
            Constants.Symbology.RESERVED_13,
            Constants.Symbology.RESERVED_15,
            Constants.Symbology.RESERVED_16,
            Constants.Symbology.RESERVED_20,
            Constants.Symbology.RESERVED_21,
            Constants.Symbology.RESERVED_27,
            Constants.Symbology.RESERVED_28,
            Constants.Symbology.RESERVED_30,
            Constants.Symbology.RESERVED_33
    };

    /**
     * Use of android.device.scanner.configuration.Symbology enums
     */
    private static Symbology[] BARCODE_SUPPORT_SYMBOLOGY = new Symbology[]{
            Symbology.AZTEC,
            Symbology.CHINESE25,
            Symbology.CODABAR,
            Symbology.CODE11,
            Symbology.CODE32,
            Symbology.CODE39,
            Symbology.CODE93,
            Symbology.CODE128,
            Symbology.COMPOSITE_CC_AB,
            Symbology.COMPOSITE_CC_C,
            Symbology.DATAMATRIX,
            Symbology.DISCRETE25,
            Symbology.EAN8,
            Symbology.EAN13,
            Symbology.GS1_14,
            Symbology.GS1_128,
            Symbology.GS1_EXP,
            Symbology.GS1_LIMIT,
            Symbology.INTERLEAVED25,
            Symbology.MATRIX25,
            Symbology.MAXICODE,
            Symbology.MICROPDF417,
            Symbology.MSI,
            Symbology.PDF417,
            Symbology.POSTAL_4STATE,
            Symbology.POSTAL_AUSTRALIAN,
            Symbology.POSTAL_JAPAN,
            Symbology.POSTAL_KIX,
            Symbology.POSTAL_PLANET,
            Symbology.POSTAL_POSTNET,
            Symbology.POSTAL_ROYALMAIL,
            Symbology.POSTAL_UPUFICS,
            Symbology.QRCODE,
            Symbology.TRIOPTIC,
            Symbology.UPCA,
            Symbology.UPCE,
            Symbology.UPCE1,
            Symbology.NONE
    };


}



