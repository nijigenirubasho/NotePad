package i.notepad.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import i.notepad.R;
import i.notepad.bean.Note;

/**
 * 适配器模式：记事数据和LiveView之间
 *
 * @author 511721050589
 * @see android.widget.ArrayAdapter
 */

public class NoteViewAdapter extends ArrayAdapter<Note> {

    private static final String TAG = "NoteViewAdapter";

    private final int mResId;
    private final List<Note> mNoteList;

    public NoteViewAdapter(@NonNull Context context, int resource, @NonNull List<Note> objects) {
        super(context, resource, objects);
        mResId = resource;
        mNoteList = objects;
    }

    /* 必须在源数据上进行改动 */
    public void reload(List<Note> noteList) {
        mNoteList.clear();
        mNoteList.addAll(noteList);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mNoteList.get(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Note note = getItem(position);
        if (note == null) {
            Log.e(TAG, "getView: note == null");
            return new View(getContext());
        }
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResId, parent, false);
            holder = new ViewHolder();
            holder.mTitleOverview = convertView.findViewById(R.id.tv_note_title_overview);
            holder.mBodyOverview = convertView.findViewById(R.id.tv_note_body_overview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitleOverview.setText(note.getTitle());
        holder.mBodyOverview.setText(note.getBody());
        return convertView;
    }

    private class ViewHolder {
        TextView mTitleOverview, mBodyOverview;
    }
}
