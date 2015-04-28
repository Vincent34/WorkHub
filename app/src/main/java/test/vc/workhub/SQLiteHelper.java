package test.vc.workhub;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Help doing the database work.
 * Created by HaoZhe Chen on 2015/4/11.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "WorkHub.db";
    private static final int version = 1;
    private static final String CREATE_TABLE_ME = "create table me(" +
            "id integer primary key," + "user_id integer," + "username text," + "tel text," + "nickname text," + "cookie text);";
    private static final String CREATE_TABLE_GROUP = "create table mygroup(" +
            "id integer primary key," + "name text," + "creator_id integer," + "creator_name text);";
    private static final String CREATE_TABLE_TASK = "create table mytask(" +
            "id integer primary key," + "title text," + "content text, " + "creator_id integer," +
            "creator_name text," + "group_id integer," + "due_time text," +
            "foreign key (group_id) references mygroup(id) on delete cascade on update cascade);";
    private static final String CREATE_TABLE_REPLY = "create table myreply(" +
            "id integer primary key," + "content text," + "task_id integer," + "replier_name text," + "reply_time text," +
            "foreign key (task_id) references mytask(id) on delete cascade on update cascade);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ME);
        db.execSQL("insert into me values(0, 0, 'test', 'test', 'test', null);");
        db.execSQL(CREATE_TABLE_GROUP);
        db.execSQL(CREATE_TABLE_TASK);
        db.execSQL(CREATE_TABLE_REPLY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("TaskDBAdapter", "Upgrade database from Version" + oldVersion + " to Version" + newVersion);
        db.execSQL("drop table if exists me");
        db.execSQL("drop table if exists mygroup");
        db.execSQL("drop table if exists mytask");
        db.execSQL("drop table if exists myreply");
        onCreate(db);
    }

}
