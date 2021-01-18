package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import studip_uni_passau.femtopedia.de.unipassaustudip.R;

public class FileAdapter extends ArrayAdapter<SubContent> {

    public FileAdapter(Context ctx, List<SubContent> content) {
        super(ctx, 0, content);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SubContent content = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_content, parent, false);
        }
        TextView name = convertView.findViewById(R.id.content_name);
        ImageView icon = convertView.findViewById(R.id.content_icon);
        if (content == null) {
            name.setText(R.string.error_no_file);
        } else {
            switch (content.getType()) {
                case FILE:
                    name.setText(content.getFile().getName().trim());
                    icon.setImageResource(R.drawable.ic_insert_drive_file);
                    break;
                case FOLDER:
                    name.setText(content.getFolder().getName().trim());
                    icon.setImageResource(R.drawable.ic_baseline_folder_open);
                    break;
                case COURSE:
                    String number = content.getCourse().getNumber();
                    String title = content.getCourse().getTitle();
                    if (number == null || number.equals("null")) {
                        name.setText(title.trim());
                    } else {
                        name.setText(getContext().getString(R.string.format_course_title,
                                number.trim(), title.trim()));
                    }
                    icon.setImageResource(R.drawable.ic_baseline_folder_open);
                    break;
            }
        }
        return convertView;
    }

}
