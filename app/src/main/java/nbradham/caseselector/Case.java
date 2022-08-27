package nbradham.caseselector;

import java.util.Arrays;

public class Case {

    private final String name;
    private final String[] chars;
    private final char[] sfws;

    protected Case(String caseName, String char0, String char1, char sfw0, char sfw1) {
        name = caseName;
        chars = new String[]{char0, char1};
        sfws = new char[]{sfw0, sfw1};
    }

    protected char getSFW(int i) {
        return sfws[i];
    }
}
