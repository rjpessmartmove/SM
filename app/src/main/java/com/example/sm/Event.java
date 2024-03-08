package com.example.sm;

import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Hashtable;


public class Event implements KvmSerializable
{
    public String Codigo;
    public String Nome;
    public String Data;

    public Event(){}


    public Event(String codigo, String nome, String data)
    {
        Codigo = codigo;
        Nome = nome;
        Data = data;
    }


    public Object getProperty(int arg0)
    {
        switch(arg0)
        {
            case 0:
                return Codigo;
            case 1:
                return Nome;
            case 2:
                return Data;
        }

        return null;
    }

    public int getPropertyCount()
    {
        return 3;
    }

    public void getPropertyInfo(int index, Hashtable arg1, PropertyInfo info)
    {
        switch(index)
        {
            case 0:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Codigo";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Nome";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "Data";
                break;
            default:break;
        }
    }

    public void setProperty(int index, Object value)
    {
        switch(index)
        {
            case 0:
                Codigo = value.toString();
                break;
            case 1:
                Nome = value.toString();
                break;
            case 2:
                Data = value.toString();
                break;
            default:
                break;
        }
    }
}
