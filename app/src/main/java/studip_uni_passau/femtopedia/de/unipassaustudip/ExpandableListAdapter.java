package studip_uni_passau.femtopedia.de.unipassaustudip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity _context;
    private List<String> _listDataHeader;
    private List<List<Object>> _listDataChild;
    private List<Integer> _listDataColorsBg, _listDataColorsText;

    ExpandableListAdapter(Activity context, List<String> listDataHeader,
                          List<List<Object>> listChildData,
                          List<Integer> listDataColorsBg,
                          List<Integer> listDataColorsText) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this._listDataColorsBg = listDataColorsBg;
        this._listDataColorsText = listDataColorsText;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(groupPosition).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    @SuppressWarnings({"inflateParams", "unchecked"})
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Object data = _listDataChild.get(groupPosition).get(childPosition);
        LayoutInflater infalInflater = (LayoutInflater) this._context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (infalInflater != null) {
            if (data instanceof String) {
                convertView = infalInflater.inflate(R.layout.list_item, null);
                TextView txtListChild = convertView
                        .findViewById(R.id.lblListItem);
                txtListChild.setTextColor(_listDataColorsText.get(groupPosition));
                convertView.setBackgroundColor(_listDataColorsBg.get(groupPosition));
                txtListChild.setText((String) getChild(groupPosition, childPosition));
            } else if (data instanceof List && !((List) data).isEmpty()) {
                if (((List) data).get(0) instanceof Integer) {
                    convertView = infalInflater.inflate(R.layout.list_image_item, null);
                    convertView.setBackgroundColor(_listDataColorsBg.get(groupPosition));
                    LinearLayout ll = convertView.findViewById(R.id.imageViewFoodPp);
                    int c = 0;
                    for (int i : (List<Integer>) data) {
                        ImageView iv = new ImageView(_context);
                        iv.setPadding(c == 0 ? 125 : 0, 20, 0, 20);
                        iv.setImageResource(i);
                        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100));
                        iv.setScaleType(ImageView.ScaleType.FIT_XY);
                        iv.setAdjustViewBounds(true);
                        ll.addView(iv);
                        c++;
                    }
                }
            }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    @SuppressWarnings({"inflateParams"})
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (infalInflater != null)
                convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        if (convertView != null) {
            TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText((String) getGroup(groupPosition));
            lblListHeader.setTextColor(_listDataColorsText.get(groupPosition));
            convertView.setBackgroundColor(_listDataColorsBg.get(groupPosition));
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}