package com.example.nttlman8.ssdp_prober;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Entry {
    private static final String TAG = "Entry";

    public String Name = "";
    public String Manufacturer = "";
    public String ManufacturerURL = "";
    public String ModelDescription = "";
    public String ModelName = "";
    public String ModelNumber = "";
    public String PresentationURL = "";

    public boolean setFields(Document doc) {

        NodeList nodeList = getNodeListFromXML(doc,"device");

        if (nodeList == null) {
            return false;
        }

        String tempStr;
        tempStr = getValueFromXml(nodeList, 0, "friendlyName");

        if (tempStr == null) {
            Log.d(TAG, "No value friendlyName");
            return false;
        }

        Name = "Name: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "manufacturer");

        if (tempStr == null) {
            Log.d(TAG,"No value manufacturer");
            return false;
        }

        Manufacturer = "Manufacturer: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "manufacturerURL");

        if (tempStr == null) {
            Log.d(TAG,"No value manufacturerURL");
            return false;
        }

        ManufacturerURL = "manufacturerURL: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "modelDescription");

        if (tempStr == null) {
            Log.d(TAG,"No value modelDescription");
            return false;
        }

        ModelDescription = "ModelDescription: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "modelName");

        if (tempStr == null) {
            Log.d(TAG,"No value modelName");
            return false;
        }

        ModelName = "ModelName: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "modelNumber");

        if (tempStr == null) {
            Log.d(TAG,"No value modelNumber");
            return false;
        }

        ModelNumber = "ModelNumber: " + tempStr;

        tempStr = getValueFromXml(nodeList, 0, "presentationURL");

        if (tempStr == null) {
            Log.d(TAG,"No value presentationURL");
            return false;
        }

        PresentationURL = "PresentationURL: " + tempStr;

        return true;
    }

    private NodeList getNodeListFromXML(Document doc, String sectName) {
        return doc.getElementsByTagName(sectName);
    }

    private String getValueFromXml(NodeList nl, int index,String val){
        Node node = nl.item(index);

        Element fstElmnt = (Element) node;

        NodeList itemList = fstElmnt.getElementsByTagName(val);

        if (itemList == null) {
            return null;
        }

        Element elem = (Element) itemList.item(index);
        if (elem == null) {
            return null;
        }

        itemList = elem.getChildNodes();

        return ((Node) itemList.item(index)).getNodeValue();
    }
}
