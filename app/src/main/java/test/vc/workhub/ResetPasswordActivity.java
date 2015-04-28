package test.vc.workhub;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by HaoZhe Chen on 2015/4/25.
 */
public class ResetPasswordActivity extends Activity {
    private Handler handler;

    public void onCreate(Bundle savedInstanceStatus) {
        super.onCreate(savedInstanceStatus);
        setContentView(R.layout.activity_resetpassword);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void onClickDone(View v) {
        final String newPassword = ((EditText) findViewById(R.id.reset_new_password)).getText().toString();
        String confirmPassword = ((EditText) findViewById(R.id.reset_confirm)).getText().toString();
        if (newPassword.equals(confirmPassword)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String rstsrc = NetHelper.requestPost("/Self/SetPassword", new String[]{"Password"},
                                new String[]{newPassword});
                        JSONObject result = new JSONObject(rstsrc);
                        Message msg = new Message();
                        msg.obj = result.getString("data");
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void onClickCancel(View v) {
        finish();
    }
}
