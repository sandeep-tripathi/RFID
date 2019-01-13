package com.example.uhf.RFIDcache;

import android.graphics.Bitmap;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RfidInfo {
    private String tagId;
    private Date nextInspectionDate;
    private String labelling;
    private String touch_test;
    private String remarks;
    private String xray_test;
    private String testremarks;
    private String test_status;
    private Date Test_Date;
    private String comments;
    private String File_name;
    private String File_path;
    private Uri currentImageUri = null;
    private Bitmap previousImage = null;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public RfidInfo() {}

    public RfidInfo(JSONObject json) throws JSONException, ParseException {
        // Getting everything necessary out from the json-object
        this.tagId = json.getString("tagid");
        this.nextInspectionDate = dateFormat.parse(json.getString("Next_Check"));
        this.labelling = json.getString("label");
        this.remarks = json.getString("testremarks");
        this.touch_test = json.getString("touch_test");
        this.xray_test = json.getString("xray_test");
        this.testremarks = json.getString("testremarks");
        this.test_status = json.getString(  "test_status");
        this.Test_Date = dateFormat.parse(json.getString("Test_Date"));
        this.comments = json.getString("comments");
        this.File_name = json.getString("File_name");
        this.File_path = json.getString("File_path");
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

    public String getTouch_test() {
        return touch_test;
    }

    public void setTouch_test(String touch_test) {
        this.touch_test = touch_test;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getXray_test() {
        return xray_test;
    }

    public void setXray_test(String xray_test) {
        this.xray_test = xray_test;
    }

    public String getTestremarks() {
        return testremarks;
    }

    public void setTestremarks(String testremarks) {
        this.testremarks = testremarks;
    }

    public String getTest_status() {
        return test_status;
    }

    public void setTest_status(String test_status) {
        this.test_status = test_status;
    }

    public Date getTest_Date() {
        return Test_Date;
    }

    public void setTest_Date(Date test_Date) {
        Test_Date = test_Date;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFile_name() {
        return File_name;
    }

    public void setFile_name(String file_name) {
        File_name = file_name;
    }

    public String getFile_path() {
        return File_path;
    }

    public void setFile_path(String file_path) {
        File_path = file_path;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static void setDateFormat(SimpleDateFormat dateFormat) {
        RfidInfo.dateFormat = dateFormat;
    }

    public Uri getCurrentImageUri() {
        return currentImageUri;
    }

    public void setCurrentImageUri(Uri currentImageUri) {
        this.currentImageUri = currentImageUri;
    }

    public Bitmap getPreviousImage() {
        return previousImage;
    }

    public void setPreviousImage(Bitmap previousImage) {
        this.previousImage = previousImage;
    }
}