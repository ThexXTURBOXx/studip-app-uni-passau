package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class ListScheduleAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;

    public ListScheduleAdapter(Activity context, List<Object> listDataHeader,
                               List<List<Object>> listChildData,
                               List<Integer> listDataColorsBg,
                               List<Integer> listDataColorsText) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        this.listDataColorsBg = listDataColorsBg;
        this.listDataColorsText = listDataColorsText;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(groupPosition).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    @SuppressWarnings("inflateParams")
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Object data = getChild(groupPosition, childPosition);
        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (infalInflater != null) {
            if (data instanceof String) {
                convertView = infalInflater.inflate(R.layout.list_schedule_item, null);
                AppCompatTextView txtListChild = convertView.findViewById(R.id.lblListItem);
                txtListChild.setText((String) data);
            }
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    @SuppressWarnings({"inflateParams"})
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null && infalInflater != null) {
            convertView = infalInflater.inflate(R.layout.list_schedule_group, null);
        }
        if (convertView != null) {
            Object group = getGroup(groupPosition);
            if (group instanceof ScheduleItem) {
                ScheduleItem item = (ScheduleItem) group;
                AppCompatTextView clock = convertView.findViewById(R.id.lblClock);
                AppCompatTextView room = convertView.findViewById(R.id.lblRoom);
                AppCompatTextView info = convertView.findViewById(R.id.lblInfo);

                int textColor = listDataColorsText.get(groupPosition);

                clock.setText(item.clock);
                clock.setTextColor(StudIPHelper.getComplementaryColor(listDataColorsBg.get(groupPosition)));
                clock.setVisibility(View.VISIBLE);

                room.setText(item.room);
                room.setTextColor(textColor);
                room.setVisibility(View.VISIBLE);

                info.setText(item.info);
                info.setTextColor(textColor);
                info.setVisibility(View.VISIBLE);
            }
            convertView.findViewById(R.id.scheduleCard).setBackgroundColor(listDataColorsBg.get(groupPosition));
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public static class ScheduleItem {

        private String clock, room, info;

        public ScheduleItem(String clock, String room, String info) {
            this.clock = clock;
            this.room = room;
            this.info = info;
        }

    }

}