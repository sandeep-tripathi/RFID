package com.example.uhf.RFIDcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RfidInfo {
    private String tagId;
    private Date nextInspectionDate;
    private String labelling;
    private String equipment_status;
    private String remarks;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public RfidInfo() {}

    public RfidInfo(JSONObject json) throws JSONException, ParseException {
        // Getting everything necessary out from the json-object
        this.tagId = json.getString("tagid");
        this.nextInspectionDate = dateFormat.parse(json.getString("nextinspdate").replace("T", " "));
        this.labelling = json.getString("labelling");
        this.remarks = json.getString("remarks");
        this.equipment_status = json.getString("equipment_status");
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Date getNextInspectionDate() {
        return nextInspectionDate;
    }

    public void setNextInspectionDate(Date nextInspectionDate) {
        this.nextInspectionDate = nextInspectionDate;
    }

    public String getLabelling() {
        return labelling;
    }

    public void setLabelling(String labelling) {
        this.labelling = labelling;
    }

    public String getEquipment_status() {
        return equipment_status;
    }

    public void setEquipment_status(String equipment_status) {
        this.equipment_status = equipment_status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}