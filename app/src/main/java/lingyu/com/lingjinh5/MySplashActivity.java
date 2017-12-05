package lingyu.com.lingjinh5;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xinmei365.game.proxy.XMSplashActivity;

public class MySplashActivity extends XMSplashActivity {

    public int getBackgroundColor() {
//当闪屏PNG图片无法铺满部分机型的屏幕时，设置与闪屏颜色配合的背景色会        给用户更好的体验
return Color.WHITE;
    }
    @Override
    public void onSplashStop() {
//闪屏结束后，启动游戏的Activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}
