package notes.alex.notesclient;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alex on 27.04.2016.
 */
public class NotePrimitive {
    private String _data;
    private Date _cdate;
    private int _id;

    public NotePrimitive() {
        this._data = "";
        Calendar cc = Calendar.getInstance();
        this._cdate = cc.getTime();
        this._id = 0;
    }

    public NotePrimitive(String s, int ident) {
        this._data = s;
        Calendar cc = Calendar.getInstance();
        this._cdate = cc.getTime();
        this._id = ident;
    }

    public NotePrimitive(int id, Date date, String data) {
        this._data = data;
        this._cdate = date;
        this._id = id;
    }

    public String GetData() { return this._data; }
    public Date GetCDate() {return this._cdate; }
    public void SetData(String ns)
    {
        this._data = ns;
    }
    public void SetCDate(Date nd)
    {
        this._cdate = nd;
    }
    public int GetID() {
        return this._id;
    }

    public void ChangeNote(int pos, String newData) {
        for (int i = 0; i < (pos+newData.length()-this._data.length()); i++)
            this._data+=" ";
        char arr[] = this._data.toCharArray();
        for (int i = pos; i < pos + newData.length(); i++)
            arr[i] = newData.charAt(i - pos);
        this._data = new String(arr);
    }

    public void DelSubstr(int beg, int end) {
        int swapper = (beg > end) ? beg : end;
        beg = (beg > end) ? end : beg;
        end = swapper;
        String ss = this._data;
        this._data = "";
        for (int i = 0; i<ss.length(); i++)
            if ((i<beg) && (i>end))
                this._data+=ss.charAt(i);
    }
}
