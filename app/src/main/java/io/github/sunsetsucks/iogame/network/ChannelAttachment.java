package io.github.sunsetsucks.iogame.network;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by ssuri on 7/28/16.
 */
public class ChannelAttachment implements Parcelable
{
    public String name;
    public int id;

    public ChannelAttachment()
    {
        name = "";
        id = 0;
    }

    protected ChannelAttachment(Parcel in)
    {
        name = in.readString();
        id = in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeInt(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChannelAttachment> CREATOR = new Parcelable.Creator<ChannelAttachment>()
    {
        @Override
        public ChannelAttachment createFromParcel(Parcel in)
        {
            return new ChannelAttachment(in);
        }

        @Override
        public ChannelAttachment[] newArray(int size)
        {
            return new ChannelAttachment[size];
        }
    };

    @Override
    public String toString()
    {
        return String.format(Locale.US, "ChannelAttachment[name=%s, id=%d]", name, id);
    }
}
