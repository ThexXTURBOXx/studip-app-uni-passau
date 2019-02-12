package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private List<Object> listDataHeader;
    private List<List<Object>> listDataChild;
    private List<Integer> listDataColorsBg, listDataColorsText;

    public ExpandableListAdapter(Activity context, List<Object> listDataHeader,
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
    @SuppressWarnings({"inflateParams", "unchecked"})
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Object data = listDataChild.get(groupPosition).get(childPosition);
        LayoutInflater infalInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (infalInflater != null) {
            if (data instanceof String) {
                convertView = infalInflater.inflate(R.layout.list_item, null);
                TextView txtListChild = convertView
                        .findViewById(R.id.lblListItem);
                txtListChild.setTextColor(listDataColorsText.get(groupPosition));
                convertView.setBackgroundColor(listDataColorsBg.get(groupPosition));
                txtListChild.setText((String) getChild(groupPosition, childPosition));
            } else if (data instanceof List) {
                if (((List) data).isEmpty())
                    ((List) data).add(R.drawable.unknown);
                if (((List) data).get(0) instanceof Integer) {
                    convertView = infalInflater.inflate(R.layout.list_image_item, null);
                    convertView.setBackgroundColor(listDataColorsBg.get(groupPosition));
                    LinearLayout ll = convertView.findViewById(R.id.imageViewFoodPp);
                    int c = 0;
                    for (int i : (List<Integer>) data) {
                        ImageView iv = new ImageView(context);
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
        if (convertView == null) {
            if (infalInflater != null)
                convertView = infalInflater.inflate(R.layout.list_group, null);
        }
        if (convertView != null) {
            Object group = getGroup(groupPosition);
            if (group instanceof String) {
                TextView lblListHeader = convertView.findViewById(R.id.lblListHeaderText);
                lblListHeader.setTypeface(null, Typeface.BOLD);
                lblListHeader.setText((String) group);
                lblListHeader.setTextColor(listDataColorsText.get(groupPosition));
                lblListHeader.setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.lblListHeaderButton).setVisibility(View.GONE);
            } else if (group instanceof ButtonPreset) {
                Button lblListHeader = convertView.findViewById(R.id.lblListHeaderButton);
                lblListHeader.setVisibility(View.VISIBLE);
                lblListHeader.setText(((ButtonPreset) group).text);
                lblListHeader.setBackgroundColor(((ButtonPreset) group).bgColor);
                lblListHeader.setTextColor(((ButtonPreset) group).textColor);
                lblListHeader.setOnClickListener(((ButtonPreset) group).onClickListener);
                convertView.findViewById(R.id.lblListHeaderText).setVisibility(View.GONE);
            }
            convertView.setBackgroundColor(listDataColorsBg.get(groupPosition));
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

    public static class ButtonPreset {
        private String text;
        private int textColor, bgColor;
        private View.OnClickListener onClickListener;

        public ButtonPreset(String text, int textColor, int bgColor, View.OnClickListener onClickListener) {
            this.text = text;
            this.textColor = textColor;
            this.bgColor = bgColor;
            this.onClickListener = onClickListener;
        }
    }

}