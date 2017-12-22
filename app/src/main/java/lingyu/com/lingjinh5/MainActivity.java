package lingyu.com.lingjinh5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.Toast;

import com.lingyu.web.BaseWebActivity;
import com.xinmei365.game.proxy.GameProxy;
import com.xinmei365.game.proxy.PayCallBack;
import com.xinmei365.game.proxy.XMUser;
import com.xinmei365.game.proxy.XMUserListener;
import com.xinmei365.game.proxy.exit.LJExitCallback;
import com.xinmei365.game.proxy.init.XMInitCallback;
import com.xinmei365.game.proxy.pay.XMPayParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends BaseWebActivity {
    private static final String TAG = "Hello";
    protected XMUser currentLoginUser;

    public class Person {
        public String name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GameProxy.getInstance().setUserListener(this, new XMUserListener() {
            @Override
            public void onLoginSuccess(XMUser xmUser, Object o) {
                Log.d(TAG, "onLoginSuccess: " + xmUser.getUserID());
                currentLoginUser=xmUser;
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", xmUser.getUsername());
                    json.put("uid", xmUser.getUserID());
                    json.put("token", xmUser.getToken());
                    json.put("productCode", xmUser.getProdcutCode());
                    json.put("channelCode", xmUser.getChannelCode());
                    json.put("channelUserId", xmUser.getChannelUserId());

                    Log.d(TAG, "doLogin: " + json.toString());
                    javascriptCall("doLogin", json.toString());
                }catch (Exception ex){
                    Log.d(TAG, "onLoginSuccessError: "+ex);
                }
                boolean isTest=false;
                if(isTest) {
                    HashMap map = new HashMap();
                    //levelUp,createRole,enterServer
                    map.put("_id", "enterServer");
                    map.put("isTest", "1");
                    doNativeCall("info", map);
                }
            }

            @Override
            public void onLoginFailed(String s, Object o) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("登录失败");
                b.setMessage("登录失败,请重试! " + s);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLogin(null);
                    }
                });
                b.setCancelable(false);
                b.create().show();

                Log.d(TAG, "onLoginFailed: " + s);
            }

            @Override
            public void onLogout(Object o) {
                Log.d(TAG, "onLogout: " + o);
                if(webView!=null){
                    webView.clearCache(false);
                    webView.reload();
                }
            }
        });

        doInit(null);

        GameProxy.getInstance().onCreate(this);
    }

    @Override
    protected void doNativeCall(String key, HashMap<String,String> map) {
        switch (key){
            case "info":
                try {
                    HashMap<String,String> datas = new HashMap<>();
                   if(map.containsKey("isTest")==false){
                       datas=map;
                   }else {
                       String id=map.get("_id");
                       datas.put("_id", id);
                       datas.put("roleId", "13524696");
                       datas.put("roleName", "方木");
                       datas.put("roleLevel", "24");
                       datas.put("zoneId", "1");
                       datas.put("zoneName", "墨土1区");
                       datas.put("balance", "88");
                       datas.put("vip", "2");
                       datas.put("partyName", "无尽天涯");
                       datas.put("extra", "extra");
                   }

                    JSONObject json=new JSONObject(datas);
                    GameProxy.getInstance().setExtData(this,json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case "doLogout":
                GameProxy.getInstance().logout(this,null);
                break;
        }

    }

    @Override
    protected void doInit(Map o) {
        GameProxy.getInstance().init(this, new XMInitCallback() {
            @Override
            public void onInitSuccess() {
                //初始化成功之后才可调用其他接口
                Log.d(TAG, "onInitSuccess: ");
                doStartWeb();
            }

            @Override
            public void onInitFailure(String msg) {
                //初始化失败
                Log.d(TAG, "onInitFailure: ");
            }
        });
    }


    @Override
    protected void doLogin(Map o) {
        Person person = new Person();
        GameProxy.getInstance().login(MainActivity.this, person);
    }

    @Override
    protected void doPay(Map o) {
        XMPayParams params = new XMPayParams();
        float rmb=Float.parseFloat((String) o.get("rmb"));
        int yuan=(int)(rmb*100);

        int count=(int)rmb;
        if(count<1){
            count=1;
        }
        params.setAmount(yuan);//支付金额,单位人民币分;
        params.setItemName("兑换币");//商品名称;
        params.setCount(count);//购买数量;
        params.setCustomParam((String)o.get("extra"));//自定义参数;
        params.setCallbackUrl((String)o.get("notifyurl"));//支付结果通知地址,即游戏服务器地址,交易结束后，我方会向该地址发送通知，通知交易的金额， customParams等信息 ;
        params.setChargePointName("");//计费点名称，用于有计费        点的渠道，没有可传空

        GameProxy.getInstance().pay(MainActivity.this, params, new PayCallBack() {
            @Override
            public void onSuccess(String sucessInfo) {
                // 此处回调仅代表用户已发起支付操作，不代表是否充值成功，具体充值是否到账需以服务器间通知为准；
                javascriptCall("doPay",sucessInfo);
            }

            @Override
            public void onFail(String failInfo) {
                // 此处回调代表用户已放弃支付，无需向服务器查询充值状态;
                Toast.makeText(getApplicationContext(), "支付失败:"+failInfo, Toast.LENGTH_SHORT);
            }
        });
    }

    protected void doExitGame() {
        GameProxy.getInstance().exit(this, new LJExitCallback() {
            @Override
            public void onGameExit() {
                Log.e(TAG, "exit application");
                MainActivity.this.doExit(false);
            }

            @Override
            public void onChannelExit() {
                gameExitEvent();
            }
        });
    }

    @Override
    protected void gameExitEvent() {
        GameProxy.getInstance().applicationDestroy(MainActivity.this);
        MainActivity.this.finish();
        System.exit(0);
    }

    protected void doStartWeb() {
        Date date=new Date();
        long t=date.getTime();
        String url =String.format("http://gate.shushanh5.lingyunetwork.com/gate/micro/login.aspx?t=%d&p=lingjin",t);
        //url="file:///android_asset/test.html";
        Log.d(TAG, "doStartWeb: "+url);
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //exit();
            doExitGame();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
//    private long clickTime=0;
//    private void exit() {
//        if ((System.currentTimeMillis() - clickTime) > 2000) {
//            Toast.makeText(getApplicationContext(), "再次点击退出",  Toast.LENGTH_SHORT).show();
//            clickTime = System.currentTimeMillis();
//        } else {
//            Log.e(TAG, "exit application");
//            this.finish();
//            System.exit(0);
//        }
//    }

    @Override
    protected void onStop() {
        super.onStop();
        GameProxy.getInstance().onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if(currentLoginUser!=null){
//            currentLoginUser=null;
//            GameProxy.getInstance().logout(this,null);
//        }

        GameProxy.getInstance().onDestroy(this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        GameProxy.getInstance().onPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GameProxy.getInstance().onStart(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        GameProxy.getInstance().onRestart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GameProxy.getInstance().onResume(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GameProxy.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        GameProxy.getInstance().onActivityResult(this, requestCode, resultCode, data);
    }
}
