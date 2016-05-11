package notes.alex.notesclient;

import java.util.ArrayList;

/**
 * Created by Alex on 27.04.2016.
 */
public class Tag {

    private int id;
    private String strData;

    public Tag (int _id, String _str) {
        this.id = _id;
        this.strData = _str;
    }

    public int GetId() {
        return this.id;
    }

    public void SetId(final int newId) {
        this.id = newId;
    }

    public String GetStrData() {
        return this.strData;
    }

    public boolean TagIsInArray(final ArrayList<Tag> tags){
        if (tags.size()>0){
            for (int i = 0; i < tags.size(); i++) {
                if (tags.get(i).GetStrData().equals(this.strData))
                    return true;
            }
        }
        return false;
    }
}
