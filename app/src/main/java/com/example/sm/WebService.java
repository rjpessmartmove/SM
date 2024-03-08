package com.example.sm;

import android.os.Bundle;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;


public class WebService
{
    private static final String ACTION_INIT = "octInitGc";
    private static final String ACTION_GETPORTATERMINAL = "octGetPortaTerminal";
    private static final String ACTION_GETPORTATERMINAL_V2 = "octGetPortaTerminal_V2";
    private static final String ACTION_GETACTIVEEVENT = "octGetActiveEvent";
    private static final String ACTION_BARCODEVERIFY = "octBarcodeVerifyTecnologia";
    private static final String ACTION_NUMACESSOSDIA = "octGetAccessEventDay";
    private static final String ACTION_GETACTIVEEVENTS_LIST = "octGetActiveEventList";
    private static final String ACTION_BARCODEVERIFY_EVENT = "octBarcodeVerifyTecnologiaEvento";
    private static final String ACTION_NUMACESSOSDIA_EVENT = "octGetAccessEventDay_Event";

    private static String WSDL_TARGET_NAMESPACE;
    private static String WSDL_SERVICE_NAME;
    private static String SOAP_ADDRESS;
    private static Integer WS_VERSION = 1;

    public WebService(String url, String nameSpace, String serviceName, Integer wsVersion)
    {
        SOAP_ADDRESS = url;
        WSDL_TARGET_NAMESPACE = nameSpace;
        WSDL_SERVICE_NAME = serviceName;
        WS_VERSION = wsVersion;
    }

    public static String InitService(Integer Terminal_ID)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        StringBuilder responseStr = new StringBuilder();
        boolean callResult = false;
        switch (WS_VERSION)
        {
            case 1:
                callResult = CallAction(responseStr, ACTION_INIT);
                break;
            case 2:
                callResult = CallAction(responseStr, ACTION_INIT, pi1);
                break;
        }

        if (callResult)
        {
            try {
                return responseStr.toString();
            } catch (Exception e) {
                return "ERROR #1: " + e.getMessage();
            }
        }
        else
            return responseStr.toString();
    }

    public static String GetPortaTerminal(Integer Terminal_ID)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        StringBuilder responseStr = new StringBuilder();
        boolean callResult = false;
        switch (WS_VERSION)
        {
            case 1:
                //callResult = CallAction(responseStr, ACTION_GETPORTATERMINAL_V2, pi1);
                callResult = CallAction(responseStr, ACTION_GETPORTATERMINAL, pi1);
                break;
            case 2:
                callResult = CallAction(responseStr, ACTION_GETPORTATERMINAL, pi1);
                break;
        }

        if (callResult)
        {
            try
            {
                return responseStr.toString();
            } catch (Exception ex)
            {
                return "ERROR #1: " + ex.getMessage();
            }
        }
        else
            return responseStr.toString();
        //return "FAILED CALL ACTION ... ";
    }

    public static String GetActiveEvent(Integer Terminal_ID)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        StringBuilder responseStr = new StringBuilder();
        boolean callResult = false;
        switch (WS_VERSION)
        {
            case 1:
                callResult = CallAction(responseStr, ACTION_GETACTIVEEVENT);
                break;
            case 2:
                callResult = CallAction(responseStr, ACTION_GETACTIVEEVENT, pi1);
                break;
        }

        if (callResult)
        {
            try
            {
                return responseStr.toString();
            }
            catch (Exception ex)
            {
                return "ERROR #1: " + ex.getMessage();
            }
        }
        else
            return responseStr.toString();
    }

    public static String GetActiveEventsList(Integer Terminal_ID)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        StringBuilder responseStr = new StringBuilder();
        boolean callResult = false;
        switch (WS_VERSION)
        {
            case 1:
                callResult = CallAction(responseStr, ACTION_GETACTIVEEVENT);
                break;
            case 2:
                callResult = CallAction(responseStr, ACTION_GETACTIVEEVENTS_LIST, pi1);
                break;
        }

        if (callResult)
        {
            try
            {
                return responseStr.toString();
            }
            catch (Exception ex)
            {
                return "ERROR #1: " + ex.getMessage();
            }
        }
        else
            return responseStr.toString();
    }

    public static String GetAcessosDia(Integer Terminal_ID, String CodigoEvento)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        PropertyInfo piEvento = null;
        if (CodigoEvento != null) {
            piEvento = new PropertyInfo();
            piEvento.setName("CodigoJogo");
            piEvento.setValue(Integer.parseInt(CodigoEvento));
            piEvento.setType(Integer.class);
        }


        StringBuilder responseStr = new StringBuilder();
        boolean callResult = false;
        switch (WS_VERSION)
        {
            case 1:
                break;
            case 2:
                if (piEvento == null)
                    callResult = CallAction(responseStr, ACTION_NUMACESSOSDIA, pi1);
                else
                    callResult = CallAction(responseStr, ACTION_NUMACESSOSDIA_EVENT, pi1, piEvento);
                break;
        }

        if (callResult)
        {
            try
            {
                return responseStr.toString();
            }
            catch (Exception ex)
            {
                return "ERROR #1: " + ex.getMessage();
            }
        }
        else
            return responseStr.toString();
    }

    public static String BarcodeVerify(Integer Terminal_ID, String Barcode, Integer Tecnologia, Integer Sentido, String CodigoEvento)
    {
        PropertyInfo pi1=new PropertyInfo();
        pi1.setName("EnderecoUBA");
        pi1.setValue(Terminal_ID);
        pi1.setType(Integer.class);

        PropertyInfo pi2=new PropertyInfo();
        pi2.setName("Barcode");
        pi2.setValue(Barcode);
        pi2.setType(String.class);

        PropertyInfo pi3=new PropertyInfo();
        pi3.setName("Tecnologia");
        pi3.setValue(Tecnologia);
        pi3.setType(Integer.class);

        PropertyInfo pi4=new PropertyInfo();
        pi4.setName("Sentido");
        pi4.setValue(Sentido);
        pi4.setType(Character.class);

        PropertyInfo piEvento = null;
        if (CodigoEvento != null) {
            piEvento = new PropertyInfo();
            piEvento.setName("CodigoJogo");
            piEvento.setValue(Integer.parseInt(CodigoEvento));
            piEvento.setType(Integer.class);
        }

        StringBuilder responseStr = new StringBuilder();

        boolean result = false;
        switch (WS_VERSION)
        {
            case 1:
                result = CallAction(responseStr, ACTION_BARCODEVERIFY, pi1, pi2, pi3, pi4);
                break;
            case 2:
                result = CallAction(responseStr, ACTION_BARCODEVERIFY_EVENT, pi1, pi2, pi3, pi4, piEvento);
                break;
        }
        if (result)
        {
            try
            {
                return responseStr.toString();
            }
            catch (Exception ex)
            {
                return "ERROR;" + ex.getMessage();
            }
        }
        else
            return "ERROR;" + responseStr.toString();
    }


    public static String Test()
    {
        StringBuilder responseStr = new StringBuilder();

        if (CallAction(responseStr, ACTION_BARCODEVERIFY))
        {
            try
            {
                return responseStr.toString();
            }
            catch (Exception ex)
            {
                return "ERROR TEST WS: " + ex.getMessage();
            }
        }
        else
            return responseStr.toString();
    }


    private static boolean CallAction(StringBuilder responseStr, String action, PropertyInfo... properties)
    {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, action);
        String soapActionFull = "";

        for (PropertyInfo property : properties)
        {
            request.addProperty(property.name, property.getValue());
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.skipNullProperties=true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
        httpTransport.setXmlVersionTag("<!--?xml version=\"1.0\" encoding= \"UTF-8\" ?-->");

        Object response=null;
        try
        {
            soapActionFull = WSDL_TARGET_NAMESPACE;
            if (WSDL_SERVICE_NAME != "")  soapActionFull = soapActionFull +  WSDL_SERVICE_NAME + "/";
            soapActionFull = soapActionFull + action;
            httpTransport.call(soapActionFull, envelope);

            if (action == ACTION_GETACTIVEEVENT)
            {
                    SoapObject resultRequestSOAP1 = (SoapObject) envelope.getResponse();
                SoapObject rootReturn = (SoapObject)resultRequestSOAP1.getProperty(0);
                SoapObject jogoReturn = (SoapObject)rootReturn.getProperty(0);

                String code = jogoReturn.getAttribute(0).toString();
                String name = jogoReturn.getAttribute(1).toString();
                String data = jogoReturn.getAttribute(2).toString();

                responseStr.append("[octGetActiveEvent];Codigo;" + code+";Nome;"+name+";Data;"+data);
            }
            else if (action == ACTION_GETACTIVEEVENTS_LIST)
            {
                SoapObject resultRequestSOAP1 = (SoapObject) envelope.getResponse();
                SoapObject rootReturn = (SoapObject)resultRequestSOAP1.getProperty(0);
                SoapObject jogoReturn;
                String code, name, data;

                int numJogos = rootReturn.getPropertyCount();
                responseStr.append("<ListaJogos>");
                for (int j = 0; j < numJogos; j++)
                {
                    jogoReturn = (SoapObject)rootReturn.getProperty(j);
                    code = jogoReturn.getAttribute(0).toString();
                    name = jogoReturn.getAttribute(1).toString();
                    data =jogoReturn.getAttribute(2).toString();
                    responseStr.append("<Jogo Codigo='" + code + "' Nome='" + name + "' Data='" + data + "'/>");
                }
                responseStr.append("</ListaJogos>");

            }

            else if (action == ACTION_BARCODEVERIFY || action == ACTION_BARCODEVERIFY_EVENT)
            {
                SoapObject resultRequestSOAP1 = (SoapObject) envelope.getResponse();
                SoapObject rootReturn = (SoapObject)resultRequestSOAP1.getProperty(0);
                SoapObject acessoReturn = (SoapObject)rootReturn.getProperty(0);

                String Result = acessoReturn.getAttribute(0).toString();
                String Message = acessoReturn.getAttribute(1).toString();
                String MessageAlt = acessoReturn.getAttribute(2).toString();
                String Owner = acessoReturn.getAttribute(3).toString();

                responseStr.append("[octBarcodeVerifyTecnologia];Result;" + Result + ";Message;" + Message + ";MessageAlt;" + MessageAlt + ";Owner;" + Owner);
            }
            else if (action == ACTION_NUMACESSOSDIA || action == ACTION_NUMACESSOSDIA_EVENT)
            {
                SoapObject resultRequestSOAP1 = (SoapObject) envelope.getResponse();
                SoapObject rootReturn = (SoapObject)resultRequestSOAP1.getProperty(0);
                SoapObject entradasReturn = (SoapObject)rootReturn.getProperty(0);

                String entradas = entradasReturn.getAttribute(0).toString();

                responseStr.append("[octGetAccessEventDay];Valor;" + entradas);
            }
            else if (action == ACTION_GETPORTATERMINAL_V2) {
                SoapObject resultRequestSOAP1 = (SoapObject) envelope.getResponse();
                SoapObject rootReturn = (SoapObject)resultRequestSOAP1.getProperty(0);
                SoapObject terminalIdReturn = (SoapObject)rootReturn.getProperty(0);

                String terminalName = terminalIdReturn.getAttribute(0).toString();

                responseStr.append(terminalName);
            }
            else
            {
                response = envelope.getResponse();
                responseStr.append(response.toString());
            }

        }
        catch (Exception exception)
        {
            responseStr.append("ERROR CALL #1: " + exception.toString());
            return false;
        }

        return true;
    }


    public static Bundle getElementsFromSOAP(SoapObject so)
    {
        Bundle resultBundle = new Bundle();
        String Key = null;
        String Value = null;
        int elementCount = so.getPropertyCount();

        for(int i = 0;i<elementCount;i++){
            PropertyInfo pi = new PropertyInfo();
            SoapObject nestedSO = (SoapObject)so.getProperty(i);

            int nestedElementCount = nestedSO.getPropertyCount();
            //Log.i(tag, Integer.toString(nestedElementCount));

            for(int ii = 0;ii<nestedElementCount;ii++){
                nestedSO.getPropertyInfo(ii, pi);
                resultBundle.putString(pi.name, pi.getValue().toString());
                //Log.i(tag,pi.getName() + " " + pii.getProperty(ii).toString());
                //Log.i(tag,pi.getName() + ": " + pi.getValue());

            }
        }

        return resultBundle;

    }

}


