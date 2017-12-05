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

import java.util.Date;
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

        doInit(null);
    }

    @Override
    protected void doInit(Map o) {
        GameProxy.getInstance().setUserListener(this, new XMUserListener() {
            @Override
            public void onLoginSuccess(XMUser xmUser, Object o) {
                Log.d(TAG, "onLoginSuccess: " + xmUser.getUserID());
                doStartWeb(xmUser);
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
            }
        });


        GameProxy.getInstance().init(this, new XMInitCallback() {
            @Override
            public void onInitSuccess() {
                //初始化成功之后才可调用其他接口
                Log.d(TAG, "onInitSuccess: ");
                doLogin(null);
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
        int rmb=Integer.parseInt((String) o.get("rmb"));

        params.setAmount(rmb);//支付金额,单位人民币分;
        params.setItemName("元");//商品名称;
        params.setCount(rmb);//购买数量;
        params.setCustomParam((String)o.get("extra"));//自定义参数;
        params.setCallbackUrl((String)o.get("notifyurl"));//支付结果通知地址,即游戏服务器地址,交易结束后，我方会向该地址发送通知，通知交易的金额， customParams等信息 ;
        params.setChargePointName("");//计费点名称，用于有计费        点的渠道，没有可传空

        GameProxy.getInstance().pay(MainActivity.this, params, new PayCallBack() {
            @Override
            public void onSuccess(String sucessInfo) {
                // 此处回调仅代表用户已发起支付操作，不代表是否充值成功，具体充值是否到账需以服务器间通知为准；
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
            }

            @Override
            public void onChannelExit() {
            }
        });
    }

    protected void doStartWeb(XMUser xmUser) {
        this.currentLoginUser=xmUser;

        Date date=new Date();
        long t=date.getTime();
        String url =String.format("http://gate.shushanh5.lingyunetwork.com/gate/micro/login.aspx?t=%d&userId=%s&p=lingjin",t,xmUser.getUserID());
        Log.d(TAG, "doStartWeb: "+url);
        webView.loadUrl(url);
    }

    protected void  doStartTestWeb(){
        String url="http://192.168.1.51/webNative.html";
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    private long clickTime=0;
    private void exit() {
        if ((System.currentTimeMillis() - clickTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再次点击退出",  Toast.LENGTH_SHORT).show();
            clickTime = System.currentTimeMillis();
        } else {
            Log.e(TAG, "exit application");
            this.finish();
            System.exit(0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        GameProxy.getInstance().onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
