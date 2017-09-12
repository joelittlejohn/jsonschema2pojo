package com.mysema.examples;

import org.apache.commons.lang3.StringUtils;

public class SelectedText {

    private String selection;

    public SelectedText(){}

    public SelectedText(String selection) {
        this.selection = selection;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getFirstWord(){
        String[] words = StringUtils.split(selection);
        return words[0];
    }

    public String getLastWord(){
        String[] words = StringUtils.split(selection);
        return words[words.length-1];
    }

}