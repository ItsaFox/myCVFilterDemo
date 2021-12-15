package d.blueshoestring.mycvdemo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class CVEntry implements Parcelable{
    public String title, start, end, location, company;
    public ArrayList<String> resp;
    public boolean highlighted;
    public int months;

    protected CVEntry(Parcel in) {
        title = in.readString();
        start = in.readString();
        end = in.readString();
        location = in.readString();
        company = in.readString();          //I suppose organization might have been a better word to use
        resp = in.createStringArrayList();
        highlighted = in.readByte() != 0;
    }

    public static final Creator<CVEntry> CREATOR = new Creator<CVEntry>() {
        @Override
        public CVEntry createFromParcel(Parcel in) {
            return new CVEntry(in);
        }

        @Override
        public CVEntry[] newArray(int size) {
            return new CVEntry[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    public String getCompany() {
        return company;
    }

    public ArrayList<String> getResp() {
        return resp;
    }

    public String getRespVal(int n) {
        return resp.get(n);
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public int getMonths() {
        return months;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(start);
        dest.writeString(end);
        dest.writeString(location);
        dest.writeString(company);
        dest.writeStringList(resp);
        dest.writeByte((byte) (highlighted ? 1 : 0));
    }

    public CVEntry(){               //Just needs to be there

    }
}
