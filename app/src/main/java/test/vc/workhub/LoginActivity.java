package test.vc.workhub;

import android.app.Activity;
import android.content.Intent;
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
 * Login activity.
 */
public class LoginActivity extends Activity {
    private static final int TOAST_SHOW = 1;
    static Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TOAST_SHOW:
                        Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    /**
     * Execute the login request.
     *
     * @param view Action view
     */
    public void onClickLogin(View view) {
        Intent data = new Intent();
        final EditText editTextName = (EditText) findViewById(R.id.edittext_username);
        final EditText editTextPw = (EditText) findViewById(R.id.edittext_password);
        final TextView textViewMessage = (TextView) findViewById(R.id.textview_login_message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String rstsrc = NetHelper.requestPost("/Act/Login", new String[]{"Username", "Password"},
                            new String[]{editTextName.getText().toString(), editTextPw.getText().toString()});
                    JSONObject result = new JSONObject(rstsrc);
                    Message msg = new Message();
                    msg.what = TOAST_SHOW;
                    msg.obj = result.getString("data");
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        setResult(RESULT_OK, data);
    }

    /**
     * Simple return parent activity
     *
     * @param view action view
     */
    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

}
