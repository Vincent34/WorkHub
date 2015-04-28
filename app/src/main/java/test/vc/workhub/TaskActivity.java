package test.vc.workhub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/13.
 */
public class TaskActivity extends Activity {
    private int id;
    HashMap<String, Object> data;
    Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Bundle args = getIntent().getExtras();
        id = args.getInt("id");
        TextView titleTextView = (TextView) findViewById(R.id.textview_task_title);
        TextView contentTextView = (TextView) findViewById(R.id.textview_task_content);
        TextView userTextView = (TextView) findViewById(R.id.textview_task_creator);
        data = MyData.getTaskData(id);
        if (!data.isEmpty()) {
            userTextView.setText("Creator: " + data.get("creator_name").toString());
            titleTextView.setText(data.get("title").toString());
            contentTextView.setText(data.get("content").toString());
        } else {
            userTextView.setText("Task not exists");
        }
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        ListView replys = (ListView) findViewById(R.id.list_reply);
        replys.setAdapter(new SimpleAdapter(this, MyData.getReplyData(id), R.layout.listview_item_reply,
                new String[]{"replier", "content"},
                new int[]{R.id.reply_replier, R.id.reply_content}));
    }

    public void onClickPositive(View v) {
        final EditText reply_content;
        new AlertDialog.Builder(this)
                .setTitle("Reply Task")
                .setView(reply_content = new EditText(this))
                .setPositiveButton("Reply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String reply = reply_content.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String rstsrc = NetHelper.requestPost("/Task/Reply/" + String.valueOf(id),
                                            new String[]{"Content"}, new String[]{reply});
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
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onClickDelete(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String rstsrc = NetHelper.requestPost("/Task/Remove/" + String.valueOf(id),
                                            null, null);
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
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onClickNegative(View v) {
        finish();
    }

}
