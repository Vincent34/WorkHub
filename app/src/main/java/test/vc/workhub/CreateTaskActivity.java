package test.vc.workhub;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/22.
 */
public class CreateTaskActivity extends Activity {
    private int group_id;
    private ListView listView;
    private Handler handler;
    private List<HashMap<String, Object>> memberData;
    private ArrayList<Integer> checkedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        Bundle args = getIntent().getExtras();
        group_id = args.getInt("Group_id");
        ArrayList arrayList = args.getParcelableArrayList("memberdata");
        memberData = (List<HashMap<String, Object>>) arrayList.get(0);
        handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        listView = (ListView) findViewById(R.id.member_list);
        listView.setAdapter(new MySimpleAdapter(this, memberData, R.layout.listview_item_select_member,
                new String[]{"member_name", "member_tel"},
                new int[]{R.id.select_name, R.id.select_tel}));
        checkedId = new ArrayList<>();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb = (CheckBox) view.findViewById(R.id.select_check);
                if (!cb.isChecked()) {
                    checkedId.add((int) memberData.get(position).get("member_id"));
                    cb.setChecked(true);
                } else {
                    int mid = (int) memberData.get(position).get("member_id");
                    if (checkedId.contains(mid)) checkedId.remove(mid);
                    cb.setChecked(false);
                }
            }
        });
    }

    public void onClickCreate(View v) {
        final EditText editTextTitle = (EditText) findViewById(R.id.newtask_title);
        final EditText editTextContent = (EditText) findViewById(R.id.newtask_content);
        final EditText editTextDueTime = (EditText) findViewById(R.id.newtask_due_time);
        final TextView textViewMessage = (TextView) findViewById(R.id.newtask_textview_message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = "/Task/Append/";
                    path += String.valueOf(group_id);
                    path += "/";
                    if (checkedId.size() > 0) {
                        path += String.valueOf(checkedId.get(0));
                        for (int i = 1; i < checkedId.size(); i++) {
                            path += ",";
                            path += String.valueOf(checkedId.get(i));
                        }
                    }
                    String rstsrc = NetHelper.requestPost(path,
                            new String[]{"Title", "Content", "EndTime"},
                            new String[]{editTextTitle.getText().toString(), editTextContent.getText().toString(),
                                    editTextDueTime.getText().toString()});
                    JSONObject result = new JSONObject(rstsrc);
                    if (result.getString("desc").equals(0)) {
                        Message msg = new Message();
                        msg.obj = "Task " + String.valueOf(result.getInt("data")) + " Created";
                        handler.sendMessage(msg);
                        textViewMessage.setText("Task " + String.valueOf(result.get("data")) + " Created");
                    } else {
                        Message msg = new Message();
                        msg.obj = result.getString("data");
                        handler.sendMessage(msg);
                        textViewMessage.setText(result.getString("data"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onClickCancel(View v) {
        finish();
    }
}
