package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        if (content == null) {
            name.setText(R.string.error_no_file);
        } else {
            switch (content.getType()) {
                case FILE:
                    name.setText(content.getFile().getName());
                    break;
                case FOLDER:
                    name.setText(content.getFolder().getName());
                    break;
                case COURSE:
                    name.setText(getContext().getString(R.string.format_course_title,
                            content.getCourse().getNumber(), content.getCourse().getTitle()));
                    break;
            }
        }
        return convertView;
    }

}
