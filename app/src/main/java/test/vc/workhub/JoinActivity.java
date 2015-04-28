package test.vc.workhub;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/25.
 */
public class JoinActivity extends Activity {
    private int offset = 0;
    private String searchName;
    List<HashMap<String, Object>> searchData;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceStatus) {
        super.onCreate(savedInstanceStatus);
        setContentView(R.layout.activity_join);
        Bundle args = getIntent().getExtras();
        String searchName = args.getString("search_name");
        offset = 0;
        handler = new Handler() {
            public void handleMessage(Message msg) {
                Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        ListView list = (ListView) findViewById(R.id.join_list);
        searchData = MyData.searchGroup(searchName, 20, 0);
        list.setAdapter(new SimpleAdapter(this, searchData, R.layout.listview_item_group,
                new String[]{"group_name", "creator_name", "group_id"},
                new int[]{R.id.group_groupname, R.id.group_creator_name, R.id.group_jobdone}));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder b = new AlertDialog.Builder(JoinActivity.this);
                b.setTitle("Apply");
                b.setMessage("Send application to group");
                b.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String rstsrc = NetHelper.requestPost("/Group/Apply/" + searchData.get(position).get("group_id").toString(),
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
                });
                b.setNegativeButton("Cancel", null);
                b.show();
            }
        });
    }

    public void onClickSearch(View view) {
        EditText nameEditText = (EditText) findViewById(R.id.join_edittext_name);
        ListView list = (ListView) findViewById(R.id.join_list);
        final String name = nameEditText.getText().toString();
        if (name != null) {
            list.setAdapter(new SimpleAdapter(this, MyData.searchGroup(name, 20, 0), R.layout.listview_item_group,
                    new String[]{"group_name", "creator_name", "group_id"},
                    new int[]{R.id.group_groupname, R.id.group_creator_name, R.id.group_jobdone}));
        }
    }
}
