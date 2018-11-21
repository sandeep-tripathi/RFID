package com.example.uhf.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.uhf.R;
import com.example.uhf.RFIDcache.RfidInfo;
import com.example.uhf.RFIDcache.RfidInfoContainer;
import com.example.uhf.activity.UHFMainActivity;
import com.example.uhf.tools.StringUtils;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rscja.deviceapi.RFIDWithUHF;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class UHFReadTagFragment extends KeyDwonFragment {

    private boolean loopFlag = false;
    private int inventoryFlag = 1;
    Handler handler;
    private ArrayList<HashMap<String, String>> tagList;
    SimpleAdapter adapter;
    Button BtClear;
    TextView tv_count;
    RadioGroup RgInventory;
    RadioButton RbInventorySingle;
    RadioButton RbInventoryLoop;
    Button Btimport;
    Button BtInventory;
    ListView LvTags;
    private Button btnFilter;//过滤
    private LinearLayout llContinuous;
    private UHFMainActivity mContext;
    private HashMap<String, String> map;
    PopupWindow popFilter;

    // Our additions to existing attributes
    String username;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onCreateView");

        return inflater
                .inflate(R.layout.uhf_readtag_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i("MY", "UHFReadTagFragment.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mContext = (UHFMainActivity) getActivity();
        tagList = new ArrayList<HashMap<String, String>>();
        BtClear = (Button) getView().findViewById(R.id.BtClear);
        Btimport = (Button) getView().findViewById(R.id.BtImport);
        tv_count = (TextView) getView().findViewById(R.id.tv_count);
        RgInventory = (RadioGroup) getView().findViewById(R.id.RgInventory);
        String tr = "";
        RbInventorySingle = (RadioButton) getView()
                .findViewById(R.id.RbInventorySingle);
        RbInventoryLoop = (RadioButton) getView()
                .findViewById(R.id.RbInventoryLoop);

        BtInventory = (Button) getView().findViewById(R.id.BtInventory);
        LvTags = (ListView) getView().findViewById(R.id.LvTags);

        llContinuous = (LinearLayout) getView().findViewById(R.id.llContinuous);

        adapter = new SimpleAdapter(mContext, tagList, R.layout.listtag_items,
                new String[]{"tagUii", "tagLen", "tagCount", "tagRssi"},
                new int[]{R.id.TvTagUii, R.id.TvTagLen, R.id.TvTagCount,
                        R.id.TvTagRssi});

        BtClear.setOnClickListener(new BtClearClickListener());
        Btimport.setOnClickListener(new BtImportClickListener());
        RgInventory.setOnCheckedChangeListener(new RgInventoryCheckedListener());
        BtInventory.setOnClickListener(new BtInventoryClickListener());
        btnFilter = (Button) getView().findViewById(R.id.btnFilter);


        btnFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popFilter == null) {
                    View viewPop = LayoutInflater.from(mContext).inflate(R.layout.popwindow_filter, null);

                    popFilter = new PopupWindow(viewPop, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);

                    popFilter.setTouchable(true);
                    popFilter.setOutsideTouchable(true);
                    popFilter.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    popFilter.setBackgroundDrawable(new BitmapDrawable());

                    final EditText etLen = (EditText) viewPop.findViewById(R.id.etLen);
                    final EditText etPtr = (EditText) viewPop.findViewById(R.id.etPtr);
                    final EditText etData = (EditText) viewPop.findViewById(R.id.etData);
                    final RadioButton rbEPC = (RadioButton) viewPop.findViewById(R.id.rbEPC);
                    final RadioButton rbTID = (RadioButton) viewPop.findViewById(R.id.rbTID);
                    final RadioButton rbUser = (RadioButton) viewPop.findViewById(R.id.rbUser);
                    final Button btSet = (Button) viewPop.findViewById(R.id.btSet);


                    btSet.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String filterBank = "UII";
                            if (rbEPC.isChecked()) {
                                filterBank = "UII";
                            } else if (rbTID.isChecked()) {
                                filterBank = "TID";
                            } else if (rbUser.isChecked()) {
                                filterBank = "USER";
                            }
                            if (etLen.getText().toString() == null || etLen.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "数据长度不能为空");
                                return;
                            }
                            if (etPtr.getText().toString() == null || etPtr.getText().toString().isEmpty()) {
                                UIHelper.ToastMessage(mContext, "起始地址不能为空");
                                return;
                            }
                            int ptr = StringUtils.toInt(etPtr.getText().toString(), 0);
                            int len = StringUtils.toInt(etLen.getText().toString(), 0);
                            String data = etData.getText().toString().trim();
                            if (len > 0) {
                                String rex = "[\\da-fA-F]*"; //匹配正则表达式，数据为十六进制格式
                                if (data == null || data.isEmpty() || !data.matches(rex)) {
                                    UIHelper.ToastMessage(mContext, "过滤的数据必须是十六进制数据");
//									mContext.playSound(2);
                                    return;
                                }

                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf(filterBank), ptr, len, data, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.uhf_msg_set_filter_fail);
//									mContext.playSound(2);
                                }
                            } else {
                                //禁用过滤
                                String dataStr = "";
                                if (mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("UII"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("TID"), 0, 0, dataStr, false)
                                        && mContext.mReader.setFilter(RFIDWithUHF.BankEnum.valueOf("USER"), 0, 0, dataStr, false)) {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_succ);
                                } else {
                                    UIHelper.ToastMessage(mContext, R.string.msg_disable_fail);
                                }
                            }


                        }
                    });
                    CheckBox cb_filter = (CheckBox) viewPop.findViewById(R.id.cb_filter);
                    rbEPC.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbEPC.isChecked()) {
                                etPtr.setText("32");
                            }
                        }
                    });
                    rbTID.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbTID.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });
                    rbUser.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (rbUser.isChecked()) {
                                etPtr.setText("0");
                            }
                        }
                    });

                    cb_filter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) { //启用过滤

                            } else { //禁用过滤

                            }
                            popFilter.dismiss();
                        }
                    });
                }
                if (popFilter.isShowing()) {
                    popFilter.dismiss();
                    popFilter = null;
                } else {
                    popFilter.showAsDropDown(view);
                }
            }
        });
        LvTags.setAdapter(adapter);
        clearData();
        Log.i("MY", "UHFReadTagFragment.EtCountOfTags=" + tv_count.getText());
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String result = msg.obj + "";
                String[] strs = result.split("@");
                addEPCToList(strs[0], strs[1]);
                mContext.playSound(1);
            }
        };

        LvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getActivity().getApplicationContext(), "Click ListItem Number " + position + "\n" + tagList.get(position).get("tagUii"), Toast.LENGTH_SHORT).show();
                RfidInfo infoOfSelectedTagID = RfidInfoContainer.findRfidInfo(tagList.get(position).get("tagUii").replace("EPC:", ""));
                if(infoOfSelectedTagID != null)
                    rfidPopup(infoOfSelectedTagID);
                else
                    UIHelper.ToastMessage(getActivity().getApplicationContext(), "No relevant information found! \nConsidering registering the tag.");

            }
        });
    }

    @Override
    public void onPause() {
        Log.i("MY", "UHFReadTagFragment.onPause");
        super.onPause();

        // 停止识别
        stopInventory();
    }

    /**
     * 添加EPC到列表中
     *
     * @param epc
     */
    private void addEPCToList(String epc, String rssi) {
        if (!TextUtils.isEmpty(epc)) {
            int index = checkIsExist(epc);

            map = new HashMap<String, String>();

            map.put("tagUii", epc);
            map.put("tagCount", String.valueOf(1));
            map.put("tagRssi", rssi);

            // mContext.getAppContext().uhfQueue.offer(epc + "\t 1");

            if (index == -1) {
                tagList.add(map);
                LvTags.setAdapter(adapter);
                tv_count.setText("" + adapter.getCount());
            } else {
                int tagcount = Integer.parseInt(
                        tagList.get(index).get("tagCount"), 10) + 1;

                map.put("tagCount", String.valueOf(tagcount));

                tagList.set(index, map);

            }

            adapter.notifyDataSetChanged();

        }
    }

    public class BtClearClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            RfidInfoContainer.flushRfidInfo(); // Flushing everything fetched from the backend so far.
            clearData();

        }
    }


    public class BtImportClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            if (BtInventory.getText().equals(
                    mContext.getString(R.string.btInventory))) {
                if(tagList.size()==0) {

                    UIHelper.ToastMessage(mContext, "无数据导出");
                    return;
                }
                boolean re = FileImport.daochu("", tagList);
                if (re) {
                    UIHelper.ToastMessage(mContext, "导出成功");

                    tv_count.setText("0");

                    tagList.clear();

                    Log.i("MY", "tagList.size " + tagList.size());

                    adapter.notifyDataSetChanged();
                }
            }
            else
            {
                UIHelper.ToastMessage(mContext, "请停止扫描后再导出");
            }
        }


    }

    private void clearData() {
        tv_count.setText("0");

        tagList.clear();

        Log.i("MY", "tagList.size " + tagList.size());

        adapter.notifyDataSetChanged();
    }

    public class RgInventoryCheckedListener implements OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            llContinuous.setVisibility(View.GONE);
            if (checkedId == RbInventorySingle.getId()) {
                // 单步识别
                inventoryFlag = 0;
            } else if (checkedId == RbInventoryLoop.getId()) {
                // 单标签循环识别
                inventoryFlag = 1;
                llContinuous.setVisibility(View.VISIBLE);
            }
        }
    }


    public class BtInventoryClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            readTag();
        }
    }

    private void readTag() {
        if (BtInventory.getText().equals(
                mContext.getString(R.string.btInventory)))// 识别标签
        {
            switch (inventoryFlag) {
                case 0:// 单步
                {
                    String strUII = mContext.mReader.inventorySingleTag();
                    if (!TextUtils.isEmpty(strUII)) {
                        String strEPC = mContext.mReader.convertUiiToEPC(strUII);
                        addEPCToList(strEPC, "N/A");
                        tv_count.setText("" + adapter.getCount());





                    } else {
                        UIHelper.ToastMessage(mContext, R.string.uhf_msg_inventory_fail);
//					mContext.playSound(2);
                    }
                }
                break;
                case 1:// 单标签循环  .startInventoryTag((byte) 0, (byte) 0))
                {
                  //  mContext.mReader.setEPCTIDMode(true);
                    if (mContext.mReader.startInventoryTag(0,0)) {
                        BtInventory.setText(mContext
                                .getString(R.string.title_stop_Inventory));
                        loopFlag = true;
                        setViewEnabled(false);
                        new TagThread().start();
                    } else {
                        mContext.mReader.stopInventory();
                        UIHelper.ToastMessage(mContext,
                                R.string.uhf_msg_inventory_open_fail);
//					mContext.playSound(2);
                    }
                }
                break;
                default:
                    break;
            }
        } else {// 停止识别
            stopInventory();
        }
    }

    private void setViewEnabled(boolean enabled) {
        RbInventorySingle.setEnabled(enabled);
        RbInventoryLoop.setEnabled(enabled);
        btnFilter.setEnabled(enabled);
        BtClear.setEnabled(enabled);
    }

    /**
     * 停止识别
     */
    private void stopInventory() {
        if (loopFlag) {
            loopFlag = false;
            setViewEnabled(true);
            if (mContext.mReader.stopInventory()) {
                BtInventory.setText(mContext.getString(R.string.btInventory));
            } else {
                UIHelper.ToastMessage(mContext,
                        R.string.uhf_msg_inventory_stop_fail);
            }
        }
    }

    /**
     * 判断EPC是否在列表中
     *
     * @param strEPC 索引
     * @return
     */
    public int checkIsExist(String strEPC) {
        int existFlag = -1;
        if (StringUtils.isEmpty(strEPC)) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < tagList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = tagList.get(i);
            tempStr = temp.get("tagUii");
            if (strEPC.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    class TagThread extends Thread {
        public void run() {
            String strTid;
            String strResult;
            String[] res = null;
            while (loopFlag) {
                res = mContext.mReader.readTagFromBuffer();
                if (res != null) {
                    strTid = res[0];
                    if (strTid.length() != 0 && !strTid.equals("0000000" +
                            "000000000") && !strTid.equals("000000000000000000000000")) {
                        strResult = "TID:" + strTid + "\n";
                    } else {
                        strResult = "";
                    }
                    Log.i("data","EPC:"+res[1]+"|"+strResult);
                    Message msg = handler.obtainMessage();
                    msg.obj = strResult + "EPC:" + mContext.mReader.convertUiiToEPC(res[1]) + "@" + res[2];

                    handler.sendMessage(msg);

                    try {
                       RfidInfoContainer.insertRfidInfo(mContext.mReader.convertUiiToEPC(res[1]), getActivity().getApplicationContext());



                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void myOnKeyDwon() {
        readTag();
    }


    public void rfidPopup(final RfidInfo rfidInfo) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_rfid_information, null))
                // Add action buttons
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogAlert, int id) {
                        // sign in the user ...
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogAlert, int id) {
                        //builder.cancel();
                    }
                });

        // 3. Get the AlertDialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Setting tagID from RfidInfo object obtained from the local app-cache
        final EditText tagIDfield = (EditText) dialog.findViewById(R.id.tagID);
        tagIDfield.setText(rfidInfo.getTagId());

        // Setting label from RfidInfo object obtained from the local app-cache
        final EditText labelField = (EditText) dialog.findViewById(R.id.label);
        labelField.setText(rfidInfo.getLabelling());

        // Setting next_inspection_date from RfidInfo object obtained from the local app-cache
        final EditText next_inspection_date = (EditText) dialog.findViewById(R.id.next_inspection_date);
        next_inspection_date.setText(dateFormatter.format(rfidInfo.getNextInspectionDate()));

        // Setting next_inspection_date from RfidInfo object obtained from the local app-cache
        final EditText remarksField = (EditText) dialog.findViewById(R.id.remarks);
        remarksField.setText(rfidInfo.getRemarks());

        // Setting username from RfidInfo object obtained from the local app-cache
        if(username != null) {
            EditText usernameField = (EditText) dialog.findViewById(R.id.username);
            usernameField.setText(username);
        }

        // Setting the value of the current date
        String date_n = dateFormatter.format(new Date());
        TextView date  = (TextView) dialog.findViewById(R.id.currentDate);
        date.setText(date_n);


        // Setting the value of the equipment_status dropdown based on the value in the response received.
        final Spinner statusDropDown = (Spinner) dialog.findViewById(R.id.equipment_status);
//        if(rfidInfo.getEquipment_status().equals("new")) {
//            statusDropDown.setSelection(0);
//        }
//        else if(rfidInfo.getEquipment_status().equals("old/usable")) {
//            statusDropDown.setSelection(1);
//        }
//        else if(rfidInfo.getEquipment_status().equals("non-usable")) {
//            statusDropDown.setSelection(2);
//        }
        statusDropDown.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0) {
                    if(position == 1) {
                        Calendar cal = Calendar.getInstance();
                        Date today = cal.getTime();
                        cal.add(Calendar.YEAR, 3); // to get previous year add -1
                        Date after3Years = cal.getTime();
                        ((EditText) dialog.findViewById(R.id.next_inspection_date)).setText(dateFormatter.format(after3Years));
                    } else if(position == 2) {
                        Calendar cal = Calendar.getInstance();
                        Date today = cal.getTime();
                        cal.add(Calendar.YEAR, 2); // to get previous year add -1
                        Date after2Years = cal.getTime();
                        ((EditText) dialog.findViewById(R.id.next_inspection_date)).setText(dateFormatter.format(after2Years));
                    } else if(position == 3) {
                        Calendar cal = Calendar.getInstance();
                        Date today = cal.getTime();
                        ((EditText) dialog.findViewById(R.id.next_inspection_date)).setText(dateFormatter.format(today));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button theButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = ((EditText) dialog.findViewById(R.id.username)).getText().toString();

                String tagid = tagIDfield.getText().toString();
                String remarks = remarksField.getText().toString();
                String next_insp_date = next_inspection_date.getText().toString();
                String equipment_status = "";
                int selectedOption = statusDropDown.getSelectedItemPosition();
                if(selectedOption == 1)
                    equipment_status = "functional";
                else if(selectedOption == 2)
                    equipment_status = "defective";
                else if(selectedOption == 3)
                    equipment_status = "damaged";

                if(username.isEmpty() || equipment_status.isEmpty())
                    UIHelper.ToastMessage(getActivity().getApplicationContext(), "Consider entering username and equipment's status!");
                else {
                    String postRequestJSON = "{\"tagid\":\"" + tagid + "\"," +
                            "\"equipment_status\":\"" + equipment_status + "\"," +
                            "\"remarks\":\"" + remarks + "\"," +
                            "\"username\":\"" + username + "\"," +
                            "\"nextinspdate\":\"" + next_insp_date + "\"}";
                    System.out.print(postRequestJSON);
                    RfidInfoContainer.submitDataToBackend(postRequestJSON, getActivity().getApplicationContext());
                }
            }
        });
    }
}
