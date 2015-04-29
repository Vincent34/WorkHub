package test.vc.workhub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/13.
 */
public class GroupActivity extends Activity {
    private String groupname, creatorname;
    private ListView listView;
    private int group_id;
    private boolean owner;
    Handler handler;
    List<HashMap<String, Object>> memberData;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Bundle args = getIntent().getExtras();
        groupname = args.getString("group_name");
        group_id = args.getInt("group_id");
        creatorname = args.getString("creator_name");
        owner = (MyData.getGroupCreator(group_id) == MyData.getUserId());
        TextView groupnametextView = (TextView) findViewById(R.id.textview_group_name);
        TextView creatornameTextView = (TextView) findViewById(R.id.textview_group_creator_name);
        groupnametextView.setText(groupname);
        creatornameTextView.setText("Creator: " + creatorname);
        Button btnExit = (Button) findViewById(R.id.button_exit);
        if (owner) btnExit.setText("Delete Group");
        else btnExit.setText("Leave Group");
        listView = (ListView) findViewById(R.id.list_member);
        memberData = MyData.getMemberName(group_id);
        listView.setAdapter(new MySimpleAdapter(this, memberData, R.layout.listview_item_member,
                new String[]{"member_name", "member_tel"},
                new int[]{R.id.member_name, R.id.member_tel}));
        handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void onClickNewTask(View v) {
        Bundle args = new Bundle();
        Intent intent = new Intent("vc.workhub.CreateTask");
        args.putInt("Group_id", group_id);
        ArrayList arrayList = new ArrayList();
        arrayList.add(memberData);
        args.putParcelableArrayList("memberdata", arrayList);
        intent.putExtras(args);
        startActivity(intent);
    }

    public void onClickExit(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (owner) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String rstsrc = NetHelper.requestPost("/Group/Remove/" + String.valueOf(group_id),
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
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String rstsrc = NetHelper.requestPost("/Group/Leave/" + String.valueOf(group_id), null, null);
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
                })
                .setNegativeButton("No", null)
                .show();
    }

}
