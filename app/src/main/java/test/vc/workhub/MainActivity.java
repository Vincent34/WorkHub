package test.vc.workhub;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    private static final int REFRESH_COMPLETE = 0x110;
    private static final int TOAST_SHOW = 0;
    private static final int UPDATE_FINISH = 1;
    private static final int REQUEST_CODE_LOGIN = 1, REQUEST_CODE_SIGNUP = 2;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SQLiteHelper sqLiteHelper;
    private MyData myData;
    private MainFragment groupfragment, taskfragment, mefragment;
    private HashMap<Integer, Object> fragment = new HashMap<Integer, Object>();
    private boolean taskShowed = true;
    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sqLiteHelper = new SQLiteHelper(this);
        MyData.setSQLiteDataBase(sqLiteHelper);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_ly);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    swipeRefreshLayout.setEnabled(true);
                else
                    swipeRefreshLayout.setEnabled(false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TOAST_SHOW:
                        Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;
                    case UPDATE_FINISH:
                        break;
                }
            }
        };
        fragment.put(0, this);
    }

    public void onClickCreateGroup(View view) {
        final EditText groupNameET;
        new AlertDialog.Builder(this)
                .setTitle("Create New Group")
                .setView((groupNameET = new EditText(this)))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String gorupname = groupNameET.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String rstsrc = NetHelper.requestPost("/Group/Append", new String[]{"Name"},
                                            new String[]{gorupname});
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
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onClickJoinGroup(View view) {
        final EditText groupNameET;
        new AlertDialog.Builder(this)
                .setTitle("Join New Group")
                .setView(groupNameET = new EditText(this))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Bundle args = new Bundle();
                        args.putString("search_name", groupNameET.getText().toString());
                        Intent intent = new Intent("vc.workhub.Join");
                        intent.putExtras(args);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onClickLogin(View view) {
        startActivityForResult(new Intent("vc.workhub.Login"), REQUEST_CODE_LOGIN);
    }

    public void onClickSignup(View view) {
        startActivityForResult(new Intent("vc.workhub.Signup"), REQUEST_CODE_SIGNUP);
    }

    public void onClickTask(View view) {
        ((MainFragment) fragment.get(2)).getListView().setAdapter(new MySimpleAdapter(this, MyData.getTaskData(), R.layout.listview_item_task,
                new String[]{"title", "due_time"},
                new int[]{R.id.task_taskname, R.id.task_duetime}));
        taskShowed = true;
    }

//    public void onClickReply(View view) {
//        ((MainFragment)fragment.get(2)).getListView().setAdapter(new MySimpleAdapter(this, MyData.getReplyData(), R.layout.listview_item_task,
//                new String[]{"title", "due_time"},
//                new int[]{R.id.task_taskname, R.id.task_duetime}));
//        taskShowed = false;
//    }

    public void onClickSetName(View v) {
        final EditText newName;
        final TextView textViewname = (TextView) (((MainFragment) (fragment.get(1))).getActivity().findViewById(R.id.textview_me_message));
        new AlertDialog.Builder(this)
                .setTitle("Set Nick Name")
                .setView(newName = new EditText(this))
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String name = newName.getText().toString();
                                    String rstsrc = NetHelper.requestPost("/Self/SetName", new String[]{"Name"},
                                            new String[]{name});
                                    JSONObject result = new JSONObject(rstsrc);
                                    Message msg = new Message();
                                    msg.what = TOAST_SHOW;
                                    msg.obj = result.getString("data");
                                    handler.sendMessage(msg);
                                    textViewname.setText(result.getString("data"));
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

    public void onClickSetPassword(View v) {
        Intent intent = new Intent("vc.workhub.Reset");
        startActivity(intent);
//        final EditText newPw;
//        final TextView textViewname = (TextView)(((MainFragment)(fragment.get(1))).getActivity().findViewById(R.id.textview_me_message));
//        new AlertDialog.Builder(this)
//                .setTitle("Set Password")
//                .setView(newPw = new EditText(this))
//                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    String pw = newPw.getText().toString();
//                                    String rstsrc = NetHelper.requestPost("/Self/SetPassword", new String[]{"Password"},
//                                            new String[]{pw});
//                                    JSONObject result = new JSONObject(rstsrc);
//                                    Message msg = new Message();
//                                    msg.what = TOAST_SHOW;
//                                    msg.obj = result.getString("data");
//                                    handler.sendMessage(msg);
//                                    textViewname.setText(result.getString("data"));
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
//                    }
//                })
//                .setNegativeButton("Cancel", null)
//                .show();
    }

    public void onClickSetTel(View v) {
        final EditText newTel;
        final TextView textViewname = (TextView) (((MainFragment) (fragment.get(1))).getActivity().findViewById(R.id.textview_me_message));
        new AlertDialog.Builder(this)
                .setTitle("Set Tel")
                .setView(newTel = new EditText(this))
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String tel = newTel.getText().toString();
                                    String rstsrc = NetHelper.requestPost("/Self/SetMobile", new String[]{"Mobile"},
                                            new String[]{tel});
                                    JSONObject result = new JSONObject(rstsrc);
                                    Message msg = new Message();
                                    msg.what = TOAST_SHOW;
                                    msg.obj = result.getString("data");
                                    handler.sendMessage(msg);
                                    textViewname.setText(result.getString("data"));
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == RESULT_OK) {

            }
        }
        if (requestCode == REQUEST_CODE_SIGNUP) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public void onRefresh() {
        if (NetHelper.isNetworkConnected(this)) {
            MyData.update();
            for (int i = 1; i <= 3; i++) {
                MainFragment tmp = (MainFragment) fragment.get(i);
                if (tmp != null) {
                    tmp.reFresh();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, "Update the data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Can't connect the Internet", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private List<View> mListViews;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public SectionsPagerAdapter(List<View> mListViews, FragmentManager fm) {
            super(fm);
            this.mListViews = mListViews;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            position++;
            MainFragment tmp = MainFragment.newInstance(position);
            fragment.put(position, tmp);
            return tmp;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section1).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MainFragment extends ListFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MainFragment newInstance(int sectionNumber) {
            MainFragment fragment = new MainFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public MainFragment() {
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Bundle args = new Bundle();
            Intent intent;
            switch (v.getId()) {
                case R.id.layout_application_item:
                    final HashMap<String, Object> map = (HashMap<String, Object>) l.getAdapter().getItem(position);
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Application to " + map.get("group_name").toString())
                            .setMessage("Approve the application?")
                            .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String rstsrc = NetHelper.requestPost("/Group/Agree/" + map.get("id").toString(),
                                                        null, null);
                                                JSONObject result = new JSONObject(rstsrc);
                                                Message msg = new Message();
                                                msg.obj = result.getString("data");
                                                msg.what = TOAST_SHOW;
                                                handler.sendMessage(msg);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String rstsrc = NetHelper.requestPost("/Group/DisAgree/" + map.get("id").toString(),
                                                        null, null);
                                                JSONObject result = new JSONObject(rstsrc);
                                                Message msg = new Message();
                                                msg.obj = result.getString("data");
                                                msg.what = TOAST_SHOW;
                                                handler.sendMessage(msg);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
                            })
                            .show();
                    break;
                case R.id.layout_group_item:
                    intent = new Intent("vc.workhub.Group");
                    args.putString("group_name", ((HashMap<String, Object>) l.getAdapter().getItem(position)).get("group_name").toString());
                    args.putInt("group_id", (int) (((HashMap<String, Object>) l.getAdapter().getItem(position)).get("group_id")));
                    intent.putExtras(args);
                    startActivity(intent);
                    break;
                case R.id.layout_task_item:
                    intent = new Intent("vc.workhub.Task");
                    args.putInt("id", (int) ((HashMap<String, Object>) l.getAdapter().getItem(position)).get("id"));
                    intent.putExtras(args);
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Touched" + String.valueOf(position) + "task", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Refresh the data of listview.
         */
        public void reFresh() {
            Bundle args = getArguments();
            switch (args.getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    ((TextView) (getActivity().findViewById(R.id.textview_username))).setText(MyData.getUsername());
                    ((TextView) (getActivity().findViewById(R.id.textview_nick_name))).setText(MyData.getNickname());
                    ((TextView) (getActivity().findViewById(R.id.textview_tel))).setText(MyData.getTel());
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getApplication(), R.layout.listview_item_application,
                            new String[]{"group_name", "username", "name"},
                            new int[]{R.id.application_group_name, R.id.application_username, R.id.application_nickname}));
                    break;
                case 2:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getTaskData(), R.layout.listview_item_task,
                            new String[]{"title", "creator_name", "due_time"},
                            new int[]{R.id.task_taskname, R.id.task_creator_name, R.id.task_duetime}));
                    break;
                case 3:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getGroupData(), R.layout.listview_item_group,
                            new String[]{"group_name", "creator_name", "job_done"},
                            new int[]{R.id.group_groupname, R.id.group_creator_name, R.id.group_jobdone}));
                    break;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            switch (args.getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    break;
                case 2:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getTaskData(), R.layout.listview_item_task,
                            new String[]{"title", "due_time"},
                            new int[]{R.id.task_taskname, R.id.task_duetime}));
                    break;
                case 3:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getGroupData(), R.layout.listview_item_group,
                            new String[]{"group_name", "creator_name", "job_done"},
                            new int[]{R.id.group_groupname, R.id.group_creator_name, R.id.group_jobdone}));
                    break;
            }
        }

        public void onStart() {
            super.onStart();
            Bundle args = getArguments();
            switch (args.getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    ((TextView) (getActivity().findViewById(R.id.textview_username))).setText(MyData.getUsername());
                    ((TextView) (getActivity().findViewById(R.id.textview_nick_name))).setText(MyData.getNickname());
                    ((TextView) (getActivity().findViewById(R.id.textview_tel))).setText(MyData.getTel());
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getApplication(), R.layout.listview_item_application,
                            new String[]{"group_name", "username", "name"},
                            new int[]{R.id.application_group_name, R.id.application_username, R.id.application_nickname}));
                    break;
                case 2:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getTaskData(), R.layout.listview_item_task,
                            new String[]{"title", "creator_name", "due_time"},
                            new int[]{R.id.task_taskname, R.id.task_creator_name, R.id.task_duetime}));
                    break;
                case 3:
                    setListAdapter(new MySimpleAdapter(getActivity(), MyData.getGroupData(), R.layout.listview_item_group,
                            new String[]{"group_name", "creator_name", "job_done"},
                            new int[]{R.id.group_groupname, R.id.group_creator_name, R.id.group_jobdone}));
                    break;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
            View rootView;
            switch (sectionNumber) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_me, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_task, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_group, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_task, container, false);
            }
            return rootView;
        }
    }

}
