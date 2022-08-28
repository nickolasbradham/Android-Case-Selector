package nbradham.caseselector;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Case {

    private final String fileName, name;
    private final String[] chars;
    private final char[] sfws;

    protected Case(String fileName, String[] setChars, char sfw0, char sfw1) {
        this.fileName = fileName;
        name = this.fileName.substring(0, this.fileName.lastIndexOf('.'));
        chars = setChars;
        sfws = new char[]{sfw0, sfw1};
    }

    protected char getSFW(int i) {
        return sfws[i];
    }

    protected String getFileName() {
        return fileName;
    }

    protected String getName() {
        return name;
    }

    protected String[] getChars() {
        return chars;
    }

    @NonNull
    @Override
    public String toString() {
        return fileName + Arrays.toString(chars) + Arrays.toString(sfws);
    }
}
