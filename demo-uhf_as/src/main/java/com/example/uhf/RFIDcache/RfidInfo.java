package com.example.uhf.RFIDcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RfidInfo {
    private String tagId;
    private String equipment;
    private Date nextInspectionDate;
    private String equipmentType;
    private String labelling;


    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public RfidInfo() {}

    public RfidInfo(JSONObject json) throws JSONException, ParseException {
        // Getting everything necessary out from the json-object
        this.tagId = json.getString("tagid");
        this.equipment = json.getString("equipment");
        this.nextInspectionDate = dateFormat.parse(json.getString("nextinspdate").replace("T", " "));
        this.equipmentType = json.getString("equipment_type");
        this.labelling = json.getString("labelling");
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public Date getNextInspectionDate() {
        return nextInspectionDate;
    }

    public void setNextInspectionDate(Date nextInspectionDate) {
        this.nextInspectionDate = nextInspectionDate;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getLabelling() {
        return labelling;
    }

    public void setLabelling(String labelling) {
        this.labelling = labelling;
    }
}