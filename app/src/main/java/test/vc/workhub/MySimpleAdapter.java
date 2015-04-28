package test.vc.workhub;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

/**
 * Created by HaoZhe Chen on 2015/4/11.
 */
public class MySimpleAdapter extends SimpleAdapter {

    public MySimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                           int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    /**
     * Change the background for interfacing list item.
     *
     * @param position    as super
     * @param convertView as super
     * @param parent      as super
     * @return as super
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.layout_group_item);
        if (position % 2 == 0)
            v.setBackgroundColor(Color.WHITE);
        else
            v.setBackgroundColor(Color.argb(0x25, 0x55, 0x88, 0x88));
        return v;
    }
}
