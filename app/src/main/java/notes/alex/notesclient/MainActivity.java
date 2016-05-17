package notes.alex.notesclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.net.Inet4Address;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Note> _notes;
    private ArrayList<NotePrimitive> _versions;
    private static ArrayList<Tag> _tagList;

    private static ArrayList<String> _noteCaptions;
    private static ArrayList<String> _noteVersions;
    private ListView left;
    ArrayAdapter<String> adapterNotes;
    private ListView right;
    ArrayAdapter<String> adapterVersions;

    private ServerSendTask _serverIO;
    private static final RequestsParser _parser = new RequestsParser();

    private boolean _lastLoginRes;

    private EditText _captionText;
    private EditText _noteText;
    private EditText _tagsText;

    private String _undoBuff;
    private boolean _isNewNote = true;
    private boolean _isNoteDel = false;
    private int _mode = 0;
    private int _selectedNote = 0;
    private int _selectedVersion = 0;

    private static Calendar cc;
    private DateFormat df;
    private DateFormat verDf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _notes = new ArrayList<Note>();
        _versions = new ArrayList<NotePrimitive>();
        _tagList = new ArrayList<Tag>();

        _noteCaptions = new ArrayList<String>();
        _noteVersions = new ArrayList<String>();

        cc = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        verDf = new SimpleDateFormat("yy-MM-dd HH:mm");

        // находим список
        left = (ListView) findViewById(R.id.notesView);
        right = (ListView) findViewById(R.id.versionsView);


        left.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                _selectedNote = position;
                noteSelected();
            }
        });

        right.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                _selectedVersion = position;
                versionSelected();
            }
        });

        // создаем адаптер
        adapterNotes = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, _noteCaptions);
        adapterVersions = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, _noteVersions);

        // присваиваем адаптер списку
        left.setAdapter(adapterNotes);
        right.setAdapter(adapterVersions);

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        Button newNoteButton = (Button) findViewById(R.id.newNoteButton);
        Button deleteNoteButton = (Button) findViewById(R.id.deleteNoteButton);
        Button saveButton = (Button) findViewById(R.id.saveButton);
        Button undoButton = (Button) findViewById(R.id.undoButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logout();
            }
        });

        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNote();
            }
        });

        deleteNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Delete();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveButtonClicked();
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UndoButtonClicked();
            }
        });

        _captionText = (EditText) findViewById(R.id.noteCaption);
        _noteText = (EditText) findViewById(R.id.noteDataView);
        _tagsText = (EditText) findViewById(R.id.tagsView);

        //selecting base info
        GetTags();
        GetCaptions();
        adapterNotes.notifyDataSetChanged();
        _isNewNote = true;
    }

    private void noteSelected() {
        _isNoteDel = true;
        if (_notes.size() > 0) {
            if (_notes.size() > 0) {
                //_selectedNote = left.getSelectedItemPosition();
                SomeNoteSelected();
                _mode = 1;
            }
        } else {
            _mode = 0;
            _tagsText.setText("");
            _captionText.setText("");
        }
    }

    public void SomeNoteSelected() {
        _tagsText.setText("");
        _noteVersions.clear();
        _captionText.setText("");
        _noteText.setText("");
        adapterVersions.notifyDataSetChanged();
        SyncTags();
        GetVersions();
        GetMoreNoteInfo();
        String s = tagsToStr(_notes.get(_selectedNote).GetTags());
        _tagsText.setText(s);

        _captionText.setText(_notes.get(_selectedNote).GetTitle());
        adapterVersions.notifyDataSetChanged();
    }

    private void Delete() {
        if (!_isNoteDel) {
            _noteText.setText("");
            _undoBuff = "";
            if (_versions.size() > 1) {
                DeleteVersion();
                adapterVersions.notifyDataSetChanged();
            } else {
                DeleteNote();
                _isNoteDel = true;
                _mode = 0;
                adapterVersions.notifyDataSetChanged();
                adapterNotes.notifyDataSetChanged();
                _isNewNote=true;
            }
        } else {
            _mode = 0;
            DeleteNote();
            _isNoteDel = true;
            adapterNotes.notifyDataSetChanged();
            adapterVersions.notifyDataSetChanged();
            if (_notes.size()==0)
                _isNewNote=true;
        }
    }

    private void versionSelected() {
        _isNoteDel = false;
        if (_notes.get(_selectedNote).GetVersionsCount() > 0) {
            if (_mode == 1) {
                _isNewNote = false;
                ///_selectedVersion = right.getSelectedItemPosition();
                _noteText.setText(_versions.get(_selectedVersion).GetData());
                _undoBuff = _noteText.getText().toString();
            }
        } else {
            _noteText.setText("");
        }
    }

    private void SaveButtonClicked() {
        if (_captionText.getText().length()==0)
            _captionText.setText("Unnamed");
        _mode = 1;
        //String ss = cc.getTime().toString();
        if (_isNewNote) {
            _isNewNote = false;
            NewNote();
            _selectedNote = _noteCaptions.size()-1;
            _isNoteDel = true;
        } else {
            if (!_noteText.getText().toString().equals(_undoBuff)) {
                CreateVersion();
            } else {
                if (_tagsText.getText().toString().length()>0)
                    AddTagsToNote(_tagsText.getText().toString(), _notes.get(_selectedNote).GetId());
                ChangeNoteCaption(_captionText.getText().toString(), _notes.get(_selectedNote).GetId());
            }
            _isNoteDel = false;
        }
        adapterNotes.notifyDataSetChanged();
        adapterVersions.notifyDataSetChanged();
        _undoBuff = _noteText.getText().toString();
    }

    private void UndoButtonClicked() {
        _noteText.setText(_undoBuff);
    }

    public String tagsToStr(ArrayList<Integer> tagIds) {
        String res = "";
        int t = 0;
        for (int i = 0; i < tagIds.size(); i++) {
            for (int j = 0; j < _tagList.size(); j++) {
                if (_tagList.get(j).GetId() == tagIds.get(i))
                    res += _tagList.get(j).GetStrData() + " ";
            }
        }
        return res;
    }

    public void CreateNote() {
        _tagsText.setText("");
        _captionText.setText("");
        _noteText.setText("");
        _noteVersions.clear();
        _versions.clear();
        _isNewNote = true;
        _mode = 0;
        adapterNotes.notifyDataSetChanged();
        adapterVersions.notifyDataSetChanged();
    }

    public void Logout() {
        boolean res = false;
        String st = _parser.Build("", CommonData.O_LOGOUT);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
            if (buff.size() > 1)
                if (buff.get(0) == CommonData.O_RESPOND) {
                    if (buff.get(1) == CommonData.SERV_YES) {
                        //_isAuth = false;
                        _notes.clear();
                        _versions.clear();
                        finish();
                    } else
                        ShowDialog("Error", "Something went wrong while logging out! Try again later!");
                }
        }
        //return CommonData.SERV_NO;
    }

    public void ShowDialog(String title, String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public void GetTags() {
        ArrayList<String> s = new ArrayList<String>();
        String st = _parser.Build(s, CommonData.O_GETTAGS);
        String str = SocketWorker.serverIO(st);
        if (!str.isEmpty()) {
            ArrayList<String> buff = _parser.ParseListOfString(str);
            if ((buff.size() > 2) && (buff.size() % 2 == 0))
                if (Integer.parseInt(buff.get(0)) == CommonData.O_RESPOND) {
                    if (Integer.parseInt(buff.get(1)) == CommonData.SERV_YES) {
                        ArrayList<String> foo = buff;
                        foo.remove(0);
                        foo.remove(0);
                        int tagId = 0;
                        String tagData = new String();
                        for (int i = 0; i < (foo.size() / 2); i++) {
                            tagId = Integer.parseInt(foo.get(i * 2));
                            tagData = foo.get(i * 2 + 1);
                            _tagList.add(new Tag(tagId, tagData));
                        }
                    }
                }
        }
    }

    public void GetCaptions() {
        ArrayList<String> s = new ArrayList<String>();
        String st = _parser.Build(s, CommonData.O_GETCAPTIONS);
        String str = SocketWorker.serverIO(st);
        if (!str.isEmpty()) {
            ArrayList<String> buff = _parser.ParseListOfString(str);
            if (buff.size() > 2)
                if (Integer.parseInt(buff.get(0)) == CommonData.O_RESPOND) {
                    if (Integer.parseInt(buff.get(1)) == CommonData.SERV_YES) {
                        buff.remove(0);
                        buff.remove(0);
                        _notes.clear(); //-------------------------------------------------------------------------------------------------------------------------------------
                        if ((buff.size() > 0) && (buff.size() % 2 == 0)) {
                            for (int i = 0; i < buff.size(); i += 2) {
                                Date d = cc.getTime();
                                _notes.add(new Note(Integer.parseInt(buff.get(i)), buff.get(i + 1), "", d, d));  //---------------------------------------------------------
                                _noteCaptions.add(buff.get(i + 1));
                            }
                        }
                    }
                }
        }
    }

    private void GetVersions() {
        _versions.clear();
        ArrayList<String> buf = new ArrayList<String>();
        buf.add(_notes.get(_selectedNote).GetId() + "");
        String st = _parser.Build(buf, CommonData.O_GET_VERSIONS);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            buf = _parser.ParseListOfString(str);
            if (buf.size() > 2)
                if (Integer.parseInt(buf.get(0)) == CommonData.O_RESPOND) {
                    if (Integer.parseInt(buf.get(1)) == CommonData.SERV_YES) {
                        buf.remove(0);
                        buf.remove(0);
                        if ((buf.size() % 2 == 0) && (buf.size() > 0))
                            for (int i = 0; i < buf.size(); i += 2) {
                                Date d = cc.getTime();
                                try {
                                    d = df.parse(buf.get(i));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                _versions.add(new NotePrimitive(i / 2, d, buf.get(i + 1)));
                                _noteVersions.add(verDf.format(d));
                            }
                    }
                }
        }
    }

    private void SyncTags() {
        String st;
        /*Sync tag list with server*/
        st = this._parser.Build(_parser.BuildTagList(this._tagList), CommonData.O_SYNC_TAG_LIST);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            ArrayList<String> buff = this._parser.ParseListOfString(str);
            if (buff.size() > 2)
                if (Integer.parseInt(buff.get(0)) == CommonData.O_RESPOND) {
                    //remove command id
                    buff.remove(0);
                    //save new tags
                    if (buff.size() % 2 == 0)
                        this._tagList = this._parser.ParseListOfTags(buff);
                    else {
                        //TODO: do anything to prevent unsynchronysation of data between server and client
                    }
                }
        }
    }

    private String GetTagById(final int id) {
        if (_tagList.size() > 0)
            for (int i = 0; i < _tagList.size(); i++) {
                if (_tagList.get(i).GetId() == id) {
                    return _tagList.get(i).GetStrData();
                }
            }

        return new String();
    }

    // Get some info for selected note primitive: cDate - mDate - tags(ids)
    private void GetMoreNoteInfo() {
        ArrayList<String> buf = new ArrayList<String>();
        buf.add(_notes.get(_selectedNote).GetId() + "");
        String st = _parser.Build(buf, CommonData.O_GET_MORE_INFO);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            buf = _parser.ParseListOfString(str);
            if (buf.size() >= 5)
                if (Integer.parseInt(buf.get(0)) == CommonData.O_RESPOND) {
                    if (Integer.parseInt(buf.get(1)) == CommonData.SERV_YES) {
                        buf.remove(0);
                        buf.remove(0);
                        Date mDate = cc.getTime();
                        Date cDate = cc.getTime();
                        try {
                            cDate = df.parse(buf.get(0));
                            buf.remove(0);
                            mDate = df.parse(buf.get(0));
                            buf.remove(0);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        StringBuilder tags = new StringBuilder();
                        ArrayList<Integer> buff = new ArrayList<Integer>();
                        if (buf.size() > 0) {
                            for (int i = 0; i < buf.size(); i++) {
                                int t = Integer.parseInt(buf.get(i));
                                buff.add(t);
                                tags.append(GetTagById(t));
                                tags.append(CommonData.USER_INPUT_TAGS_SEP);
                            }
                        }
                        _notes.get(_selectedNote).SetTags(buff);
                        _notes.get(_selectedNote).SetCDate(cDate);
                        _notes.get(_selectedNote).SetMDate(mDate);
                        //_tagsText.setText(tagsToStr(buff));
                    }
                }
        }
    }

    public void CreateVersion() {
        String newText = _noteText.getText().toString();
        Date newDate = cc.getTime();
        String tags = _tagsText.getText().toString();
        String newCaption = _captionText.getText().toString();

        ArrayList<String> buf = new ArrayList<String>();
        int verId = CommonData.SERV_NO;
        int noteId;
        if (this._notes.size() == 0) noteId = 0;
        else noteId = this._notes.get(this._selectedNote).GetId();
        String text = _parser.fixNoteData(newText);

        buf.add(noteId + "");
        buf.add(text);
        buf.add(df.format(newDate));

        String st = _parser.Build(buf, CommonData.O_ADD_VERSION);

        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
            if (buff.size() > 2)
                if (buff.get(0) == CommonData.O_RESPOND) {
                    if (buff.get(1) == CommonData.SERV_YES) {
                        verId = buff.get(2);
                        NotePrimitive temp = new NotePrimitive(verId, newDate, text);
                        this._versions.add(temp);
                        AddTagsToNote(tags, noteId);
                        ChangeNoteCaption(newCaption, noteId);
                        _noteVersions.add(verDf.format(newDate));
                    }
                }
        }
    }

    private int AddTagsToNote(final String tagString, final int newNoteId) {
        //if (tagString.length()>0) {
            String st;
            ArrayList<String> tagData = UpdateTagList(tagString);
        /**/
            StringBuilder stb = new StringBuilder();

            if (_notes.size() > 0)
                if (_notes.get(_selectedNote).GetTags().size() > 0)
                    for (int i = 0; i < _notes.get(_selectedNote).GetTags().size(); i++) {
                        for (int j = 0; j < _tagList.size(); j++)
                            if (_tagList.get(j).GetId() == _notes.get(_selectedNote).GetTags().get(i)) {
                                stb.append(_tagList.get(j).GetStrData());
                                if (!((j == _tagList.size() - 1) && tagData.size() > 0))
                                    stb.append(CommonData.USER_INPUT_TAGS_SEP);
                            }
                    }
            if (tagData.size() > 0) {
                for (int i = 0; i < tagData.size(); i++) {
                    stb.append(tagData.get(i));
                    if (i != tagData.size() - 1)
                        stb.append(CommonData.USER_INPUT_TAGS_SEP);
                }
            }
            _tagsText.setText(stb.toString());
        /**/
            ArrayList<String> res = new ArrayList<String>();
            //Sync tags with server
            SyncTags();
            // Add tags to created note
            res.clear();
            //Convert tags of new note to tag ids
            res.add(newNoteId + "");
            ArrayList<Integer> tags = ConvertTagsIntoIds(tagData);
            if (tags.size() > 0)
                for (int i = 0; i < tags.size(); i++) {
                    res.add(tags.get(i).toString());
                }
            if (tags.size()>0) {
                st = this._parser.Build(res, CommonData.O_ADD_TAGS_TO_NOTE);
                String str = SocketWorker.serverIO(st);
                if (!st.equals("")) {
                    ArrayList<Integer> buff = this._parser.ParseListOfInteger(st);
                    if (buff.size() > 1)
                        if (buff.get(0) == CommonData.O_RESPOND) {
                            if (buff.get(1) == CommonData.SERV_YES) {
                                if (_notes.size() > 0) _notes.get(_selectedNote).SetTags(tags);
                                return CommonData.SERV_YES;
                            }
                        }
                }
            }
            else
                return CommonData.SERV_YES;
        return CommonData.SERV_NO;
    }

    private int ChangeNoteCaption(final String caption, final int newNoteId) {
        String st;
        ArrayList<String> res = new ArrayList<String>();
        // Add tags to created note
        res.clear();
        //Convert tags of new note to tag ids
        res.add(newNoteId + "");
        res.add(caption);

        st = this._parser.Build(res, CommonData.O_CHANGE_CAPTION);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            ArrayList<Integer> buff = this._parser.ParseListOfInteger(str);
            if (buff.size() > 1)
                if (buff.get(0) == CommonData.O_RESPOND) {
                    if (buff.get(1) == CommonData.SERV_YES) {
                        _notes.get(_selectedNote).SetTitle(caption);
                        _noteCaptions.set(_selectedNote, caption);
                        adapterNotes.notifyDataSetChanged();
                        adapterVersions.notifyDataSetChanged();
                        return CommonData.SERV_YES;
                    }
                }
        }
        return CommonData.SERV_NO;
    }

    public void NewNote() {
        String newText = _noteText.getText().toString();
        Date newDate = cc.getTime();
        String newTags = _tagsText.getText().toString();
        String newCaption = _captionText.getText().toString();

        ArrayList<String> res = new ArrayList<String>();
        int newNoteId = -1;
        String st;
        String text = _parser.fixNoteData(newText);

        //Fill request
        res.clear();
        res.add(text);
        res.add(newCaption);
        res.add(df.format(newDate));
        res.add(df.format(newDate));

        st = this._parser.Build(res, CommonData.O_CREATE_N);
        String str = SocketWorker.serverIO(st);
        if (!str.equals("")) {
            ArrayList<Integer> buff = this._parser.ParseListOfInteger(str);
            if (buff.size() > 2)
                if (buff.get(0) == CommonData.O_RESPOND) {
                    if (buff.get(1) == CommonData.SERV_YES) {
                        newNoteId = buff.get(2);
                    } else
                        ShowDialog("Error", "New note hasn't been created. Please, try again later.");
                }
        }

        AddTagsToNote(newTags, newNoteId);

        NotePrimitive vim = new NotePrimitive(0, newDate, text);
        _versions.add(vim);
        Note nm = new Note(newNoteId, newCaption, newTags, newDate, newDate);
        nm.AddVersion(newDate, text);
        _notes.add(nm);
        _noteCaptions.add(newCaption);
        _noteVersions.add(verDf.format(newDate));
        //_selectedNote++;
    }

    private boolean IsTagNew(ArrayList<Integer> arr, String tag) {
        if (arr.size() > 0)
            for (int i = 0; i < arr.size(); i++) {
                for (int j = 0; j < _tagList.size(); j++)
                    if (_tagList.get(j).GetId() == arr.get(i))
                        if (_tagList.get(j).GetStrData().equals(tag))
                            return false;
            }
        return true;
    }

    private ArrayList<String> UpdateTagList(final String tags) {
        int nextId = 0;
        ArrayList<String> res = new ArrayList<>();
        if (_tagList.size() > 0)
            nextId = _tagList.get(_tagList.size() - 1).GetId() + 1;

        StringBuilder str = new StringBuilder();
        if (tags.length() > 0) {
            for (int i = 0; i < tags.length(); i++) {
                if (tags.charAt(i) == CommonData.USER_INPUT_TAGS_SEP) {
                    if (str.length() > 0) {
                        if (!res.contains(str.toString()) && ((_notes.size() > 0 && IsTagNew(_notes.get(_selectedNote).GetTags(), str.toString())) || (_notes.size() == 0))) {
                            Tag t = new Tag(nextId, str.toString());
                            if (!t.TagIsInArray(_tagList)) {
                                nextId++;
                                _tagList.add(t);
                            }
                            res.add(str.toString());
                            //str.delete(0, str.length());
                        }
                        str.delete(0, str.length());
                    }
                } else {
                    str.append(tags.charAt(i));
                }
            }
            if (str.length() > 0) {
                if (!res.contains(str.toString()) && ((_notes.size() > 0 && IsTagNew(_notes.get(_selectedNote).GetTags(), str.toString())) || (_notes.size() == 0))) {
                    Tag t = new Tag(nextId, str.toString());
                    if (!t.TagIsInArray(_tagList)) {
                        nextId++;
                        _tagList.add(t);
                    }
                    res.add(str.toString());
                    //str.delete(0, str.length());
                }
                str.delete(0, str.length());
            }
        }
        return res;
    }

    private ArrayList<Integer> ConvertTagsIntoIds(final ArrayList<String> tagData) {
        ArrayList<Integer> res = new ArrayList<Integer>();
        if (_tagList.size() > 0 && tagData.size() > 0) {
            for (int i = 0; i < tagData.size(); i++) {
                for (int j = 0; j < _tagList.size(); j++) {
                    if (_tagList.get(j).GetStrData().equals(tagData.get(i))) {
                        int t = _tagList.get(j).GetId();
                        if (!res.contains(t)) {
                            res.add(t);
                        }
                        break;
                    }
                }
            }
        }
        return res;
    }

    public void DeleteVersion() {
        if (_versions.size() > 0) {
            ArrayList<Integer> buff = new ArrayList<Integer>();
            int noteId = _notes.get(this._selectedNote).GetId();
            int versId = _versions.get(this._selectedVersion).GetID();
            buff.add(noteId);
            buff.add(versId);
            String st = _parser.Build(CommonData.O_DELETE_N_V, buff);
            String str = SocketWorker.serverIO(st);
            if (!str.equals("")) {
                buff = _parser.ParseListOfInteger(str);
                if (buff.size() > 1)
                    if (buff.get(0) == CommonData.O_RESPOND) {
                        if (buff.get(1) == CommonData.SERV_YES) {
                            _versions.remove(this._selectedVersion);
                            _noteVersions.remove(_selectedVersion);
                            _notes.get(_selectedNote).DelVersion(_selectedVersion);
                        }
                    }
            }
        }
    }

    public void DeleteNote() {
        _undoBuff = "";
        if (_notes.size() > 0) {
            int noteId = _notes.get(this._selectedNote).GetId();
            String st = _parser.Build(noteId, CommonData.O_DELETE_N);
            String str = SocketWorker.serverIO(st);
            if (!str.equals("")) {
                ArrayList<Integer> buff = _parser.ParseListOfInteger(str);
                if (buff.size() > 1)
                    if (buff.get(0) == CommonData.O_RESPOND) {
                        if (buff.get(1) == CommonData.SERV_YES) {
                            _notes.remove(_selectedNote);
                            _versions.clear();
                            _tagsText.setText("");
                            _captionText.setText("");
                            _noteCaptions.remove(_selectedNote);
                            _noteVersions.clear();
                            if (_notes.size() > 0 && _selectedNote > 0) {
                                _selectedNote--;
                                SomeNoteSelected();
                            }
                        }
                    }
            }
        }

    }

}
