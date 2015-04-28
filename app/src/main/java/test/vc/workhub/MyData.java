package test.vc.workhub;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/10.
 * Store and provide data.
 */
public class MyData {

    private static SQLiteDatabase db;
    private static SQLiteHelper sqLiteHelper;
    private static String cookie;

    public static void update() {
        ContentValues cv = new ContentValues();
        cv.putNull("user_id");
        cv.putNull("username");
        cv.putNull("nickname");
        cv.putNull("tel");
        db.update("me", cv, "id=0", null);

        Thread updateSelf = new Thread(new Runnable() {
            @Override
            public void run() {
                updateSelf();
            }
        });
        updateSelf.start();

        db.delete("mygroup", null, null);
        Thread updateGroup = new Thread(new Runnable() {
            @Override
            public void run() {
                updateGroup();
            }
        });
        updateGroup.start();

        db.delete("mytask", null, null);
        Thread updateTask = new Thread(new Runnable() {
            @Override
            public void run() {
                updateTask();
            }
        });
        updateTask.start();

        db.delete("myreply", null, null);

        while (updateSelf.isAlive() || updateGroup.isAlive() || updateTask.isAlive()) {
        }
        ;
        Message msg = new Message();
        msg.what = 1;
        msg.obj = "Data Update finish";
        MainActivity.handler.sendMessage(msg);
    }

    public static void updateSelf() {
        try {
            String rstsrc = NetHelper.requestPost("/Self/Info", null, null);
            JSONObject result = new JSONObject(rstsrc);
            if (result.getString("desc").equals("0")) {
                JSONObject data = result.getJSONObject("data");
                ContentValues cv = new ContentValues();
                cv.put("user_id", Integer.parseInt(data.getString("id")));
                cv.put("username", data.getString("username"));
                cv.put("nickname", data.getString("name"));
                cv.put("tel", data.getString("mobile"));
                db.update("me", cv, "id=0", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateGroup() {
        try {
            String rstsrc = NetHelper.requestPost("/Group/List", null, null);
            JSONObject result = new JSONObject(rstsrc);
            if (result.getString("desc").equals("0")) {
                JSONArray array = result.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject group = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    cv.put("id", Integer.parseInt(group.getString("id")));
                    cv.put("name", group.getString("name"));
                    cv.put("creator_id", Integer.parseInt(group.getString("creator_id")));
                    cv.put("creator_name", group.getString("creator_name"));
                    db.insert("mygroup", null, cv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateTask() {
        try {
            String rstsrc = NetHelper.requestPost("/Task/List", null, null);
            JSONObject result = new JSONObject(rstsrc);
            if (result.getString("desc").equals("0")) {
                JSONArray array = result.getJSONArray("data");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject task = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    cv.put("id", Integer.parseInt(task.getString("id")));
                    cv.put("title", task.getString("title"));
                    cv.put("content", task.getString("content"));
                    cv.put("creator_id", Integer.parseInt(task.getString("creator_id")));
                    cv.put("creator_name", task.getString("creator_name"));
                    cv.put("group_id", Integer.parseInt(task.getString("group_id")));
                    cv.put("due_time", task.getString("end_time"));
                    db.insert("mytask", null, cv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateReply() {

    }

    public static void setSQLiteDataBase(SQLiteHelper _sqLiteHelper) {
        sqLiteHelper = _sqLiteHelper;
        db = sqLiteHelper.getWritableDatabase();
    }

    public static void setCookie(String cookiesrc) {
        ContentValues cv = new ContentValues();
        cv.put("cookie", cookiesrc);
        db.update("me", cv, "id=0", null);
    }

    public static String getCookie() {
        Cursor cursor = db.query("me", null, "id=0", null, null, null, null);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex("cookie"));
    }

    public static int getUserId() {
        Cursor cursor = db.query("me", null, "id=0", null, null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex("user_id"));
    }

    public static String getUsername() {
        Cursor cursor = db.query("me", null, "id=0", null, null, null, null);
        cursor.moveToFirst();
        String rst = cursor.getString(cursor.getColumnIndex("username"));
        cursor.close();
        return rst;
    }

    public static String getNickname() {
        Cursor cursor = db.query("me", null, "id=0", null, null, null, null);
        cursor.moveToFirst();
        String rst = cursor.getString(cursor.getColumnIndex("nickname"));
        cursor.close();
        return rst;
    }

    public static String getTel() {
        Cursor cursor = db.query("me", null, "id=0", null, null, null, null);
        cursor.moveToFirst();
        String rst = cursor.getString(cursor.getColumnIndex("tel"));
        cursor.close();
        return rst;
    }

    public static List<HashMap<String, Object>> getGroupData() {
        ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
        Cursor cursor = db.query("mygroup", null, null, null, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("group_name", cursor.getString(cursor.getColumnIndex("name")));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            map.put("group_id", id);
            map.put("creator_name", cursor.getString(cursor.getColumnIndex("creator_name")));
            Cursor check = db.query("mytask", new String[]{"group_id"}, "group_id=?", new String[]{String.valueOf(id)},
                    null, null, null);
            if (check.getCount() == 0)
                map.put("job_done", "Done");
            else
                map.put("job_done", "Not_Done");
            groupData.add(map);
            check.close();
        }
        cursor.close();
        return groupData;
    }

    public static int getGroupCreator(int groupId) {
        Cursor cursor = db.query("mygroup", null, "id=?", new String[]{String.valueOf(groupId)}, null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex("creator_id"));
    }

    public static List<HashMap<String, Object>> searchGroup(final String name, final int limit, final int offset) {
        final List<HashMap<String, Object>> groupData = new ArrayList<>();
        Thread searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String rstsrc = NetHelper.requestPost("/Group/Search/" + String.valueOf(limit) + "/" + String.valueOf(offset),
                            new String[]{"Name"}, new String[]{name});
                    HashMap<String, Object> map;
                    JSONObject result = new JSONObject(rstsrc);
                    JSONArray array = result.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject group = array.getJSONObject(i);
                        map = new HashMap<>();
                        map.put("group_id", Integer.parseInt(group.getString("id")));
                        map.put("group_name", group.getString("name"));
                        map.put("creator_name", group.getString("creator_name"));
                        groupData.add(map);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        searchThread.start();
        while (searchThread.isAlive()) {
        }
        ;
        return groupData;
    }

    public static List<HashMap<String, Object>> getTaskData() {
        List<HashMap<String, Object>> taskData = new ArrayList<HashMap<String, Object>>();

        Cursor cursor = db.query("mytask", null, null, null, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", cursor.getInt(cursor.getColumnIndex("id")));
            map.put("title", cursor.getString(cursor.getColumnIndex("title")));
            map.put("type", "Task");
            map.put("creator_name", cursor.getString(cursor.getColumnIndex("creator_name")));
            map.put("due_time", cursor.getString(cursor.getColumnIndex("due_time")));
            taskData.add(map);
        }
        cursor.close();
        return taskData;
    }

    public static HashMap<String, Object> getTaskData(int taskId) {
        HashMap<String, Object> taskData = new HashMap<String, Object>();
        Cursor cursor = db.query("mytask", new String[]{"title", "content", "creator_name"}, "id=?", new String[]{String.valueOf(taskId)},
                null, null, null);
        cursor.moveToFirst();
        taskData.put("title", cursor.getString(cursor.getColumnIndex("title")));
        taskData.put("content", cursor.getString(cursor.getColumnIndex("content")));
        taskData.put("creator_name", cursor.getString(cursor.getColumnIndex("creator_name")));
        cursor.close();
        return taskData;
    }

    public static List<HashMap<String, Object>> getReplyData(final int taskId) {
        final List<HashMap<String, Object>> replyData = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map;
        Cursor cursor = db.query("myreply", null, "task_id=?", new String[]{String.valueOf(taskId)}, null, null, null);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            map = new HashMap<>();
            map.put("replier", cursor.getString(cursor.getColumnIndex("replier_name")));
            map.put("content", cursor.getString(cursor.getColumnIndex("content")));
            replyData.add(map);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, Object> map;
                    String rstsrc = NetHelper.requestPost("/Task/ListReply/" + String.valueOf(taskId),
                            null, null);
                    JSONObject result = new JSONObject(rstsrc);
                    if (result.getString("desc").equals("0")) {
                        JSONArray array = result.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject reply = array.getJSONObject(i);
                            map = new HashMap<String, Object>();
                            map.put("replier", reply.getString("name"));
                            map.put("content", reply.getString("content"));
                            replyData.add(map);
                            ContentValues cv = new ContentValues();
                            cv.put("id", Integer.parseInt(reply.getString("id")));
                            cv.put("content", reply.getString("content"));
                            cv.put("task_id", taskId);
                            cv.put("replier_name", reply.getString("name"));
                            cv.put("reply_time", reply.getString("time"));
                            db.insert("myreply", null, cv);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return replyData;
    }

    public static List<HashMap<String, Object>> getMemberName(final int groupId) {
        final boolean ok = false;
        final List<HashMap<String, Object>> memberData = new ArrayList<HashMap<String, Object>>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, Object> map;
                    String rstsrc = NetHelper.requestPost("/Group/User/" + String.valueOf(groupId), null, null);
                    JSONObject result = new JSONObject(rstsrc);
                    if (result.getString("desc").equals("0")) {
                        JSONArray array = result.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject member = array.getJSONObject(i);
                            map = new HashMap<String, Object>();
                            map.put("member_id", Integer.parseInt(member.getString("id")));
                            map.put("member_name", member.getString("name"));
                            map.put("member_tel", member.getString("mobile"));
                            memberData.add(map);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while (thread.isAlive()) {
        }
        HashMap<String, Object> map = new HashMap<>();
        return memberData;
    }

    public static List<HashMap<String, Object>> getApplication() {
        final List<HashMap<String, Object>> data = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String rst = NetHelper.requestPost("/Group/GetApply", null, null);
                    JSONObject result = new JSONObject(rst);
                    HashMap<String, Object> map;
                    if (result.getString("desc").equals("0")) {
                        JSONArray array = result.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject application = array.getJSONObject(i);
                            map = new HashMap<>();
                            map.put("username", application.getString("username"));
                            map.put("name", application.getString("name"));
                            map.put("userid", Integer.parseInt(application.getString("userid")));
                            map.put("group_name", application.getString("groupname"));
                            map.put("id", Integer.parseInt(application.getString("id")));
                            data.add(map);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        while (thread.isAlive()) {
        }
        ;
        return data;
    }

}
