package com.lingyu.web;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lingyu.com.lingjinh5.CustomWebChromeClient;

/**
 * Created by Crl on 2017/12/5.
 */

public class BaseWebActivity extends Activity {
    private static final String TAG = "BaseWebActivity";
    protected WebView webView;
    protected FrameLayout viewLayout;
    protected  ViewGroup.LayoutParams layoutParams;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(1);
        layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewLayout=new FrameLayout(this);
        viewLayout.setLayoutParams(layoutParams);
        this.setContentView(viewLayout);

        //WebView.setWebContentsDebuggingEnabled(true);

        webView = new WebView(this);

        bindWebView(webView);
        viewLayout.addView(webView);
    }

    protected void bindWebView(WebView webView){
        this.webView=webView;

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webView.setBackgroundColor(Color.parseColor("#00000000"));
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);

        webView.addJavascriptInterface(this, "phone");
        webView.setWebChromeClient(new CustomWebChromeClient(this));
    }

    /**
     * 被javascript调用的方法
     * @param key
     * @param value
     */
    @JavascriptInterface
    public void nativeCall(String key, String value) {
        HashMap<String,String> map=null;
        try {
            map=toMap(value);
        }catch (Exception e){
            map=new HashMap<String,String>();
            map.put("value",value);
        }
        Log.d(TAG, "nativeCall: "+key+","+value);
        switch (key) {
            case "doInit":
                doInit(map);
            case "doLogin":
                doLogin(map);
                break;
            case "doPay":
                doPay(map);
                break;
            case "doExit":
                int v=0;
               if(value!=null && value!=""){
                   v=Integer.parseInt(value);
               }
                doExit(v==1);
                break;
            default:
                doNativeCall(key,map);
                break;
        }
    }
    protected void  doNativeCall(String key,HashMap<String,String> map){

    }

    protected void doInit(Map o){
    }
    protected void doLogin(Map o){
    }
    protected void doPay(Map o){
    }
    protected void doExit(boolean isForce){

        if(isForce==false){
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("提示");
            b.setMessage("确定要退出");
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    gameExitEvent();
                }
            });
            b.setNegativeButton(android.R.string.no,null);
            b.create().show();
        }else {
            gameExitEvent();
        }
    }

    protected void gameExitEvent(){
        this.finish();
        System.exit(0);
    }

    /**
     *调用javascript的方法
     * @param key
     * @param value
     */
    protected void javascriptCall(String key, Object value){
        String json="";
        if((value instanceof  String) || (value instanceof Integer) || (value instanceof Boolean) || (value instanceof Float) ){
            json=value.toString();
        }else {
            json = toJSON(value).toString();
        }

        Log.d(TAG, "javascriptCall:"+key+":"+json);
       webView.loadUrl("javascript:LingyuSDK.NativeBack('"+key+"','"+json+"')");
    }

    /**
     * 将JavaBean转换成JSONObject（通过Map中转）
     * @param  bean javaBean
     * @return json对象
     */
    public static JSONObject toJSON(Object bean) {
        return new JSONObject(toMap(bean));
    }

    /**
     * 将Javabean转换为Map
     * @param javaBean  javaBean
     * @return Map对象
     */
    public static Map toMap(Object javaBean) {
        Map result = new HashMap();
        Method[] methods = javaBean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            try {
                if (method.getName().startsWith("get")) {
                    String field = method.getName();
                    field = field.substring(field.indexOf("get") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);
                    Object value = method.invoke(javaBean, (Object[]) null);
                    result.put(field, null == value ? "" : value.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    /**
     * 将Json对象转换成Map
     * @param  jsonString json对象
     * @return Map对象
     * @throws JSONException
     */
    public static HashMap<String,String> toMap(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        HashMap<String,String> result = new HashMap<>();
        Iterator iterator = jsonObject.keys();
        String key = null;
        String value = null;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = jsonObject.getString(key);
            result.put(key, value);
        }
        return result;
    }
}
