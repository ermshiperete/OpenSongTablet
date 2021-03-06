package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PopUpImportExternalFile extends DialogFragment {

    static PopUpImportExternalFile newInstance() {
        PopUpImportExternalFile frag;
        frag = new PopUpImportExternalFile();
        return frag;
    }

    SetActions setActions;

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    // The views
    TextView fileTitle_TextView, fileType_TextView, title, progressText;
    Spinner chooseFolder_Spinner;
    CheckBox overWrite_CheckBox;
    ProgressBar progressBar, progressBarH;
    LinearLayout progressLinearLayout;
    FloatingActionButton saveMe, closeMe;
    View V;

    // Helper classes
    Bible bibleC;
    StorageAccess storageAccess;
    Preferences preferences;
    SongFolders songFolders;
    ListSongFiles listSongFiles;
    ImportOnSongBackup import_os;
    OnSongConvert onSongConvert;
    ChordProConvert chordProConvert;
    SongXML songXML;

    String what, errormessage = "", filetype, chosenfolder;

    // Folder variables
    ArrayList<String> folderlist;
    ArrayAdapter<String> arrayAdapter;

    // Other variables
    boolean overwrite, ok, error;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bibleC = new Bible();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        listSongFiles = new ListSongFiles();
        setActions = new SetActions();
        onSongConvert = new OnSongConvert();
        chordProConvert = new ChordProConvert();
        songXML = new SongXML();

        Log.d("d", "Opened importexternalfile fragment");
        V = inflater.inflate(R.layout.popup_importexternalfile, container, false);
        Log.d("d", "V=" + V);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        initialiseViews(V);
        Log.d("d", "initialiseViews success");

        setAction();
        Log.d("d", "setActions success");

        updateTextViews();
        Log.d("d", "updateTextViews success");

        initialiseLocationsToSave();
        Log.d("d", "initialiseLocationsToSave success");

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        Log.d("d", "decoratePopUp success");

        return V;
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    public void setTitle(String s) {
        try {
            if (title != null) {
                title.setText(s);
            } else {
                if (getDialog() != null) {
                    getDialog().setTitle(s);
                }
            }
        } catch (Exception e) {
            Log.d("d","Problem with title");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    void setAction() {
        String s = getActivity().getString(R.string.importnewsong);
        String ext;

        // Defaults
        overWrite_CheckBox.setChecked(false);
        overWrite_CheckBox.setVisibility(View.VISIBLE);
        overWrite_CheckBox.setEnabled(true);

        if (FullscreenActivity.whattodo.equals("doimportset")) {
            s = getActivity().getString(R.string.importnewset);
            what = "set";
            filetype = getActivity().getString(R.string.options_set);
        } else {
            what = "song";
            filetype = getActivity().getString(R.string.options_song);
        }
        if (FullscreenActivity.file_uri!=null && FullscreenActivity.file_uri.getPath()!=null) {
            ext = FullscreenActivity.file_uri.getPath();
            if (ext != null) {
                ext = ext.toLowerCase();
                if (ext.endsWith(".backup")) {
                    s = getActivity().getString(R.string.import_onsong_choose);
                    what = "onsongbackup";
                    filetype = getActivity().getString(R.string.import_onsong_choose);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".osts")) {
                    s = getActivity().getString(R.string.importnewset);
                    what = "set";
                    filetype = getActivity().getString(R.string.export_set);
                } else if (ext.endsWith(".onsong")) {
                    what = "onsongfile";
                    filetype = getActivity().getString(R.string.export_onsong);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".pro") || ext.endsWith(".cho") || ext.endsWith(".chopro") || ext.endsWith("chordpro")) {
                    what = "chordpro";
                    filetype = getActivity().getString(R.string.export_chordpro);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".usr")) {
                    what = "songselect";
                    filetype = getActivity().getString(R.string.songselect);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".gif") || ext.endsWith(".jpg") || ext.endsWith(".png")) {
                    what = "image";
                    filetype = getActivity().getString(R.string.image);
                } else if (ext.endsWith(".pdf")) {
                    what = "pdf";
                    filetype = "PDF";
                } else if (ext.endsWith(".txt")) {
                    what = "text";
                    filetype = getActivity().getString(R.string.export_text);
                } else if (bibleC.isYouVersionScripture(FullscreenActivity.incoming_text)) {
                    what = "bible";
                    filetype = getActivity().getString(R.string.scripture);
                } else {
                    // Need to check that this is an OpenSong xml file (may have .ost extension though)
                    if (storageAccess.containsXMLTags(getActivity(), FullscreenActivity.file_uri)) {
                        if (FullscreenActivity.myToastMessage.equals("foundset")) {
                            what = "set";
                            filetype = getActivity().getString(R.string.options_set);
                        } else {
                            what = "song";
                            filetype = getActivity().getString(R.string.options_song);
                        }
                    } else {
                        notValid();
                    }
                }
            }
            try {
                title.setText(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            notValid();
        }
    }

    void initialiseViews(View v) {
        fileTitle_TextView = v.findViewById(R.id.fileTitle_TextView);
        fileType_TextView = v.findViewById(R.id.fileType_TextView);
        title = v.findViewById(R.id.dialogtitle);
        chooseFolder_Spinner = v.findViewById(R.id.chooseFolder_Spinner);
        overWrite_CheckBox = v.findViewById(R.id.overWrite_CheckBox);
        progressBar = v.findViewById(R.id.progressBar);
        progressBarH = v.findViewById(R.id.progressBarH);
        progressText = v.findViewById(R.id.progressText);
        progressLinearLayout = v.findViewById(R.id.progressLinearLayout);
        saveMe = v.findViewById(R.id.saveMe);
        closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                overwrite = overWrite_CheckBox.isChecked();
                getChosenFolder();
                setUpSaveAction();
            }
        });
    }

    void notValid() {
        // Not a valid or recognised file, so warn the user and close the popup
        FullscreenActivity.myToastMessage = getActivity().getString(R.string.file_type_unknown);
        if (mListener != null) {
            mListener.showToastMessage(FullscreenActivity.myToastMessage);
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateTextViews() {
        if ((FullscreenActivity.file_uri != null) && (FullscreenActivity.file_uri.getLastPathSegment() != null)) {
            fileTitle_TextView.setText(FullscreenActivity.file_uri.getLastPathSegment());
        }
        fileType_TextView.setText(filetype);
    }

    void importOnSongBackup() {
        // Hide the cancel button
        if (closeMe != null) {
            closeMe.setVisibility(View.GONE);
        }

        if (progressLinearLayout != null) {
            progressLinearLayout.setVisibility(View.VISIBLE);
        }
        if (saveMe != null) {
            saveMe.setClickable(false);
        }

        // Now start the AsyncTask
        import_os = new ImportOnSongBackup(FullscreenActivity.file_uri);
        try {
            import_os.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("d", "Error importing");
        }
    }

    void initialiseLocationsToSave() {
        if (what.equals("set")) {
            folderlist = new ArrayList<>();
            folderlist.add(getActivity().getString(R.string.options_set));
        } else if (what.contains("onsong")) {
            folderlist = new ArrayList<>();
            folderlist.clear();
            songFolders.prepareSongFolders(getActivity(), storageAccess, preferences);
            folderlist = new ArrayList<>(Arrays.asList(FullscreenActivity.mSongFolderNames));
            folderlist.add(0, "OnSong");
        } else {
            songFolders.prepareSongFolders(getActivity(), storageAccess, preferences);
            folderlist = new ArrayList<>(Arrays.asList(FullscreenActivity.mSongFolderNames));
        }
        arrayAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,folderlist);
        chooseFolder_Spinner.setAdapter(arrayAdapter);
    }

    void setUpSaveAction() {
        switch (what) {
            case "onsongbackup":
                importOnSongBackup();
                break;

            case "set":
                importOpenSongSet();
                break;

            case "bible":
                importBibleText();
                break;

            default:
                importFile();
                break;
        }
    }

    void importOpenSongSet() {
        // Get the new file name
        String filename = FullscreenActivity.file_uri.getLastPathSegment();
        if (filename!=null && filename.endsWith(".osts")) {
            filename = filename.replace(".osts","");
        }

        progressLinearLayout.setVisibility(View.VISIBLE);

        // Copy the set and to open in the app
        completedImportSet(filename);
    }

    void importBibleText() {
        String translation = FullscreenActivity.scripture_title.substring(FullscreenActivity.scripture_title.lastIndexOf(" "));
        String verses = FullscreenActivity.scripture_title.replace(translation, "");

        // Since the scripture is one big line, split it up a little (50 chars max)
        FullscreenActivity.mScripture = bibleC.shortenTheLines(FullscreenActivity.mScripture,50,8);

        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<song>" +
                "  <title>"+verses+"</title>\n" +
                "  <author>"+translation+"</author>\n" +
                "  <user1></user1>\n" +
                "  <user2>false</user2>\n" +
                "  <user3></user3>\n" +
                "  <aka></aka>\n" +
                "  <key_line></key_line>\n" +
                "  <hymn_number></hymn_number>\n" +
                "  <lyrics>"+FullscreenActivity.mScripture+"</lyrics>\n" +
                "</song>";

        // Write the file
        Uri scripturefile = storageAccess.getUriForItem(getActivity(), preferences, "Scripture", "", "YouVersion");

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, scripturefile, null,
                "Scripture", "", "YouVersion");

        OutputStream outputStream = storageAccess.getOutputStream(getActivity(),scripturefile);
        storageAccess.writeFileFromString(text,outputStream);
        completedImport("YouVersion","../Scripture");
    }

    void copyFile(String folder, String subfolder, String filename) {
        Uri newfile = storageAccess.getUriForItem(getActivity(), preferences, folder, subfolder, filename);
        if (!storageAccess.uriExists(getActivity(), newfile) || overwrite) {
            InputStream inputStream = storageAccess.getInputStream(getActivity(), FullscreenActivity.file_uri);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, newfile, null,
                    folder, subfolder, filename);

            OutputStream outputStream = storageAccess.getOutputStream(getActivity(), newfile);
            error = false;
            ok = storageAccess.copyFile(inputStream, outputStream);
            if (!ok) {
                errormessage = errormessage + filename + ": " + getActivity().getString(R.string.backup_error) + "\n";
            }
        } else {
            error = true;
            errormessage = errormessage + filename + ": " + getActivity().getString(R.string.file_exists) + "\n";
        }
    }

    void importFile() {
        // Get the new file name
        String filename = FullscreenActivity.file_uri.getLastPathSegment();
        if (filename!=null && filename.endsWith(".ost")) {
            filename = filename.replace(".ost","");
        }

        // Set the variable to initialise the search index
        FullscreenActivity.needtorefreshsongmenu = true;

        // Copy the file
        copyFile("Songs", chosenfolder, filename);

        // Set up the file ready to open in the app
        completedImport(filename, chosenfolder);
    }

    void getChosenFolder() {
        int i = chooseFolder_Spinner.getSelectedItemPosition();
        chosenfolder = folderlist.get(i);
    }

    void completedImport(String song, String subfolder) {
        FullscreenActivity.songfilename = song;
        FullscreenActivity.whichSongFolder = subfolder;
        storageAccess.listSongs(getActivity(), preferences);
        listSongFiles.getAllSongFiles(getActivity(), preferences, storageAccess);
        if (ok && !error) {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.success);
        } else {
            FullscreenActivity.myToastMessage = errormessage;
        }
        if (mListener != null) {
            try {
                mListener.showToastMessage(FullscreenActivity.myToastMessage);
                mListener.rebuildSearchIndex();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void completedImportSet(final String set) {
        ImportSet importSet = new ImportSet(set);
        importSet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface MyInterface {
        void refreshAll();
        void rebuildSearchIndex();
        void onSongImportDone(String message);
        void openFragment();
        void showToastMessage(String message);
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportSet extends AsyncTask<String, Void, String> {

        String setname;

        ImportSet(String set) {
            setname = set;
        }

        @Override
        protected void onPreExecute() {
            FullscreenActivity.settoload = setname;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                // Copy the file
                copyFile("Sets", "", setname);

                if (!error) {
                    setActions.loadASet(getActivity(), preferences, storageAccess);
                    FullscreenActivity.setView = true;

                    // Get the set first item
                    setActions.prepareFirstItem(getActivity(), preferences, listSongFiles, storageAccess);

                    // Save the new set to the preferences
                    Preferences.savePreferences();

                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.success);

                    FullscreenActivity.abort = false;

                } else {

                    FullscreenActivity.myToastMessage = errormessage;
                }

            } catch (Exception e) {
                FullscreenActivity.myToastMessage = getActivity().getString(R.string.error);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                mListener.showToastMessage(FullscreenActivity.myToastMessage);
                if (!error) {
                    mListener.refreshAll();
                    FullscreenActivity.whattodo = "editset";
                    mListener.openFragment();
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportOnSongBackup extends AsyncTask<Object, String, String> {
        String message;
        InputStream is;
        ZipArchiveInputStream zis;
        String filename;
        Uri zipUri;
        int numitems;
        int curritem;

        ImportOnSongBackup(Uri zU) {
            zipUri = zU;
        }

        @Override
        protected void onPreExecute() {
            progressText.setVisibility(View.VISIBLE);
            progressBarH.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... string) {
            try {
                progressText.setText(string[0]);
                progressBarH.setMax(numitems);
                progressBarH.setProgress(curritem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            File dbfile = new File(getActivity().getExternalFilesDir("OnSong"), "OnSong.Backup.sqlite3");
            try {
                dbfile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }


            Uri chosenfolderuri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", chosenfolder, "");

            // Create the chosen folder (if it doesn't already exist)
            if (chosenfolder.equals("OnSong")) {
                storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, chosenfolderuri,
                        DocumentsContract.Document.MIME_TYPE_DIR, "Songs", chosenfolder, "");
            }

            try {
                is = storageAccess.getInputStream(getActivity(), zipUri);
                zis = new ZipArchiveInputStream(new BufferedInputStream(is), "UTF-8", false);

                ZipArchiveEntry ze;
                while ((ze = (ZipArchiveEntry) zis.getNextEntry()) != null) {
                    final byte[] buffer = new byte[2048];
                    int count;
                    filename = ze.getName();
                    if (!filename.startsWith("Media")) {
                        // The Media folder throws errors (it has zero length files sometimes
                        // It also contains stuff that is irrelevant for OpenSongApp importing
                        // Only process stuff that isn't in that folder!
                        // It will also ignore any song starting with 'Media' - not worth a check for now!

                        OutputStream out;
                        if (filename.equals("OnSong.Backup.sqlite3") || filename.equals("OnSong.sqlite3")) {
                            dbfile = new File(getActivity().getExternalFilesDir("OnSong"), "OnSong.Backup.sqlite3");
                            Uri outuri = Uri.fromFile(dbfile);
                            if (!dbfile.mkdirs()) {
                                Log.d("PopUpImport", "Database file already exists - ok");
                            }
                            out = storageAccess.getOutputStream(getActivity(), outuri);

                        } else if (!filename.endsWith(".doc") && !filename.endsWith(".docx") && !filename.endsWith(".sqlite3") &&
                                !filename.endsWith(".preferences")) {
                            Uri outuri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", chosenfolder, filename);
                            storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences,
                                    outuri, null, "Songs", chosenfolder, filename);
                            out = storageAccess.getOutputStream(getActivity(), outuri);
                            publishProgress(filename);

                        } else {
                            // Don't copy this
                            out = null;
                        }

                        if (out != null) {
                            final BufferedOutputStream bout = new BufferedOutputStream(out);
                            try {
                                while ((count = zis.read(buffer)) != -1) {
                                    bout.write(buffer, 0, count);
                                }
                                bout.flush();
                            } catch (Exception e) {
                                message = getActivity().getResources().getString(R.string.file_type_unknown);
                                e.printStackTrace();
                            } finally {
                                try {
                                    bout.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                zis.close();

            } catch (Exception e) {
                e.printStackTrace();
                message = getActivity().getResources().getString(R.string.import_onsong_error);
                publishProgress(message);
                return message;
            }

            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbfile, null);

            // Go through each row and read in the content field
            // Save the files with the .onsong extension

            String query = "SELECT title,content FROM Song";

            //Cursor points to a location in your results
            Cursor cursor;
            String str_title;
            String str_content;

            try {
                cursor = db.rawQuery(query, null);

                numitems = cursor.getCount();
                curritem = 0;

                // Move to first row
                cursor.moveToFirst();

                while (cursor.moveToNext()) {
                    curritem++;

                    // Extract data
                    str_title = cursor.getString(cursor.getColumnIndex("title"));
                    // Make sure title doesn't have /
                    str_title = str_title.replace("/", "_");
                    str_title = TextUtils.htmlEncode(str_title);
                    str_content = cursor.getString(cursor.getColumnIndex("content"));

                    try {
                        message = curritem + "/" + numitems + "  " + getActivity().getString(R.string.extracting) +
                                ": " + str_title + ".onsong";
                        publishProgress(message);

                        // Prepare the OnSong file
                        Uri oldsong = storageAccess.getUriForItem(getActivity(), preferences, "Songs",
                                chosenfolder, str_title + ".onsong");
                        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences,
                                oldsong, null, "Songs", chosenfolder, str_title + ".onsong");

                        // Now write the modified song
                        message = curritem + "/" + numitems + "  " + getActivity().getString(R.string.converting) +
                                ": " + str_title + ".onsong";
                        publishProgress(message);
                        onSongConvert.convertTextToTags(getActivity(), storageAccess, preferences,
                                songXML, chordProConvert, oldsong, str_content, 1);

                    } catch (Exception e) {
                        e.printStackTrace();
                        message = str_title + ".onsong --> " + getActivity().getString(R.string.error);
                        publishProgress(message);
                    }
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                // Error with sql database
                e.printStackTrace();
                return "Error";
            }

            return getActivity().getResources().getString(R.string.success);
        }

        @Override
        protected void onPostExecute(String s) {
            // This bit will take a while, so will be called in an async task
            try {
                ShowToast.showToast(getActivity());
                if (mListener != null) {
                    mListener.onSongImportDone(s);
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}