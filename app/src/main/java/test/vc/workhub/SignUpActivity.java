package test.vc.workhub;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

/**
 * Created by HaoZhe Chen on 2015/4/12.
 */
public class SignUpActivity extends Activity {
    private static Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void onClickSignup(View view) {
        final EditText editTextUsername = (EditText) findViewById(R.id.edittext_username);
        final EditText editTextPassword = (EditText) findViewById(R.id.edittext_password);
        final EditText editTextConfirm = (EditText) findViewById(R.id.edittext_confirm);
        final TextView textViewMessage = (TextView) findViewById(R.id.textview_signup_message);
        String message;
        if (editTextConfirm.getText().toString().equals(editTextPassword.getText().toString())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String rstsrc = NetHelper.requestPost("/Act/Reg", new String[]{"Username", "Password"},
                                new String[]{editTextUsername.getText().toString(), editTextPassword.getText().toString()});
                        JSONObject result = new JSONObject(rstsrc);
                        textViewMessage.setText(result.getString("data"));
                        Message msg = new Message();
                        msg.obj = result.getString("data");
                        handler.sendMessage(msg);
                        if (result.getString("desc").equals("0")) {
                            String rst = NetHelper.requestPost("/Act/Login", new String[]{"Username", "Password"},
                                    new String[]{editTextUsername.getText().toString(), editTextPassword.getText().toString()});
                            JSONObject loginResult = new JSONObject(rstsrc);
                            msg.obj = loginResult.getString("data");
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

}
