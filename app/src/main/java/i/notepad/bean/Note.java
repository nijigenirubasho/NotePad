package i.notepad.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 记事的数据结构
 *
 * @author 511721050589
 */

public class Note implements Parcelable {

    private String title, body;
    private long id;

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public Note() {
    }

    private Note(Parcel in) {
        title = in.readString();
        body = in.readString();
        id = in.readLong();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeLong(id);
    }
}
