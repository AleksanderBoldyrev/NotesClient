package notes.alex.notesclient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Alex on 27.04.2016.
 */
public class Note {
    private int _id;
    private String _title;
    private ArrayList<NotePrimitive> _note_n;
    private Date _n_cdate;
    private Date _n_mdate;
    private ArrayList<Integer> _tags;

    public Note (final int id, final String title, final String data, final Date cDate, final Date mDate){
        this._id = id;
        this._title = title;
        this._tags = new ArrayList<Integer>();
        this._n_cdate = cDate;
        this._n_mdate = mDate;
        this._note_n = new ArrayList<NotePrimitive>();
        this._note_n.add(new NotePrimitive(0, mDate, data));
    }

    public Note (int id, ArrayList<Integer> tags, ArrayList<NotePrimitive> notes, String title) {
        this._note_n = new ArrayList<NotePrimitive>();
        this._note_n.clear();
        this._id = id;
        this._title = title;
        this._tags = new ArrayList<Integer>();
        this._tags.clear();
        for (int i = 0; i < tags.size(); i++) {
            this._tags.add(i, tags.get(i));
        }
        this._note_n = new ArrayList<NotePrimitive>();
        for (int i = 0; i < notes.size(); i++) {
            this._note_n.add(i, notes.get(i));
        }
        if (this._note_n.size() > 0) {
            this._n_cdate = (this._note_n.get(0)).GetCDate();
            this._n_mdate = (this._note_n.get(this._note_n.size() - 1)).GetCDate();
        }
        else {
            Calendar cc = Calendar.getInstance();
            this._n_cdate = cc.getTime();
            this._n_mdate = cc.getTime();
        }
    }

    public NotePrimitive getNote() {
        if (this._id > this._note_n.size()) this._id = 0;
        return this._note_n.get(0);
    }

    public String GetNoteVerDateByPos(int pos) {
        if (this._note_n.size()>0 && this._note_n.size() < pos)
            return this._note_n.get(pos).GetCDate().toString();
        return "";
    }

    public int GetVersionsCount() {
        return this._note_n.size();
    }

    public NotePrimitive GetNoteByPos(int pos) {
        if (pos>(this._note_n.size()-1))
            pos = this._note_n.size()-1;
        if (pos<0)
            pos = 0;
        return this._note_n.get(pos);
    }


    public int GetId() {
        return this._id;
    }

    public String GetTitle() {return this._title; }

    public  int GetTagsCount() {
        return this._tags.size();
    }

    public Date GetCDate(){
        return this._n_cdate;
    }

    public Date GetMDate(){
        return this._n_mdate;
    }

    public int GetTagById(int id) {
        if (id>(this._tags.size()-1))
            id = this._tags.size()-1;
        if (id<0)
            id = 0;

        if (this._tags.size() > 0) return this._tags.get(id);
        else return 0;
    }

    public void SetTitle(String data)
    {
        this._title = data;
    }

    public void SetTags(ArrayList<Integer> _tagIds)
    {
        this._tags = _tagIds;
    }

    public void AddTags(final ArrayList<Integer> tagList){
        if (tagList.size()>0)
            for (int i = 0; i < tagList.size(); i++)
                this._tags.add(tagList.get(i));
    }

    public int DelVersion(int _targetId) {
        if (this._note_n.size()>1)
            for (int i=0; i < this._note_n.size(); i++) {
                if (this._note_n.get(i).GetID()==_targetId) {
                    this._note_n.remove(i);
                    return CommonData.SERV_YES;
                }

            }
        return CommonData.SERV_NO;
    }
    public int AddVersion(final Date date, final String data)
    {
        int id = this._note_n.get(this._note_n.size()-1).GetID()+1;
        NotePrimitive np = new NotePrimitive(id, date, data);
        this._note_n.add(np);
        return id;
    }

    public ArrayList<Integer> GetTags()
    {
        return _tags;
    }

    public ArrayList<NotePrimitive> GetVersions()
    {
        return _note_n;
    }

    public void SetCDate(Date dat){
        _n_cdate=dat;
    }

    public void SetMDate(Date dat)
    {
        _n_mdate=dat;
    }
}
