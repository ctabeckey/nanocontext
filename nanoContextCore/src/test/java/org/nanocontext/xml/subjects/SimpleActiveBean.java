package org.nanocontext.xml.subjects;

/**
 * Created by cbeckey on 3/13/17.
 */
public class SimpleActiveBean implements Runnable {
    public final static String ranValue = "YEP, I RAN";
    private String value = "NEVER RAN";
    private boolean ran = false;

    public SimpleActiveBean() {
        value = "CONSTRUCTED, NEVER RAN";
    }

    @Override
    public void run() {
        this.value = ranValue;
        this.ran = true;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isRan() {
        return ran;
    }
}
