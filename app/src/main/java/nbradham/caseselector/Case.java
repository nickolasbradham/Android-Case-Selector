package nbradham.caseselector;

/**
 * Holds all info related to a single case.
 */
public class Case {

    private final String fileName, name;
    private final String[] chars;
    private final char[] sfws;

    /**
     * Constructs a new Case.
     *
     * @param setFileName The file name of the image.
     * @param setChars    The string names of the character(s) on the case.
     * @param sfw0        The SFW rating of the first character.
     * @param sfw1        The SFW rating of the second character.
     */
    protected Case(String setFileName, String[] setChars, char sfw0, char sfw1) {
        fileName = setFileName;
        name = fileName.substring(0, fileName.lastIndexOf('.'));
        chars = setChars;
        sfws = new char[]{sfw0, sfw1};
    }

    /**
     * Retrieves the SFW rating of character {@code i}.
     *
     * @param i The index of the character to get.
     * @return The character representing the SFW rating.
     */
    protected char getSFW(int i) {
        return sfws[i];
    }

    /**
     * Retrieves the file name.
     *
     * @return The file name of the case.
     */
    protected String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the name of the case. This is the file name without the extension.
     *
     * @return The name of the case.
     */
    protected String getName() {
        return name;
    }

    /**
     * Retrieves the name(s) of the character(s) on the case.
     *
     * @return The names of the characters in a String array.
     */
    protected String[] getChars() {
        return chars;
    }
}
