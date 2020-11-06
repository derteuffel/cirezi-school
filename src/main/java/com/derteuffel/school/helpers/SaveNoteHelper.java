package com.derteuffel.school.helpers;

import java.util.List;

public class SaveNoteHelper {

    private String periode;
    private int noteMax;

    List<NoteHelper> noteHelpers;


    public int getNoteMax() {
        return noteMax;
    }

    public void setNoteMax(int noteMax) {
        this.noteMax = noteMax;
    }

    public String getPeriode() {
        return periode;
    }

    public void setPeriode(String periode) {
        this.periode = periode;
    }

    public List<NoteHelper> getNoteHelpers() {
        return noteHelpers;
    }

    public void setNoteHelpers(List<NoteHelper> noteHelpers) {
        this.noteHelpers = noteHelpers;
    }
}
