package nbradham.caseselector;

import java.util.Arrays;

/**
 * Holds all info related to a single case.
 */
final class Case {

    private final String fileName, name;
    private final char[] chars;
    private final char[] sfws;

    /**
     * Constructs a new Case.
     *
     * @param setFileName The file name of the image.
     * @param setChars    The string names of the character(s) on the case.
     * @param sfw0        The SFW rating of the first character.
     * @param sfw1        The SFW rating of the second character.
     */
    Case(String setFileName, char sfw0, char sfw1, char[] setChars) {
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
    char getSFW(int i) {
        return sfws[i];
    }

    /**
     * Retrieves the file name.
     *
     * @return The file name of the case.
     */
    String getFileName() {
        return fileName;
    }

    /**
     * Retrieves the name of the case. This is the file name without the extension.
     *
     * @return The name of the case.
     */
    String getName() {
        return name;
    }

    /**
     * Retrieves the name(s) of the character(s) on the case.
     *
     * @return The names of the characters in a String array.
     */
    char[] getChars() {
        return chars;
    }

    /**
     * Parses input string into a Case instance.
     *
     * @param str The String to parse.
     * @return The new Case instance.
     */
    static Case parse(String str) {
        char[] split = str.replaceAll("^[0-9]*|.jpg", "").toCharArray();
        return new Case(str, split[0], split[1], Arrays.copyOfRange(split, 2, split.length));
    }
}