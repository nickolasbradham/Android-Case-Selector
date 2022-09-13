package nbradham.caseselector;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles all code execution and GUI setup and interactions.
 */
public class MainActivity extends AppCompatActivity {

    private static final String OPT_SFW = "sfw", OPT_BOTH_SIDES = "both", OPT_NO_REPEAT = "diff", CH_SAFE = "s", CH_EROTIC = "e", CH_EXPLICIT = "x";
    private static final Random RAND = new Random();

    private final Properties props = new Properties();
    private final ArrayList<String> history = new ArrayList<>();

    private Case[] allCases;
    private File imgDir, hist, opts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Android app things.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup file objects for later use.
        File workDir = getExternalFilesDir(null);
        imgDir = new File(workDir, "images");
        imgDir.mkdirs();
        hist = new File(workDir, "history.txt");
        opts = new File(workDir, "options.txt");

        //Try and read options file.
        if (opts.exists())
            try {
                FileReader fr = new FileReader(opts);
                props.load(fr);
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        //Setup app from options.
        switch (props.getProperty(OPT_SFW, CH_EXPLICIT)) {
            case CH_SAFE:
                ((RadioButton) findViewById(R.id.rbSafe)).setChecked(true);
                break;
            case CH_EROTIC:
                ((RadioButton) findViewById(R.id.rbErotic)).setChecked(true);
        }

        ((CheckBox) findViewById(R.id.cbBothSide)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES, "false")));
        ((CheckBox) findViewById(R.id.cbNoRepeat)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT, "true")));

        //Try to retrieve history from file.
        if (hist.exists())
            try {
                Scanner scan = new Scanner(hist);
                while (scan.hasNextLine())
                    history.add(scan.nextLine());
                scan.close();

                setCaseView(parseCase(history.get(history.size() - 1)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        //Retrieve all cases from image dir.
        ArrayList<Case> tmpCases = new ArrayList<>();
        for (String s : Objects.requireNonNull(imgDir.list()))
            tmpCases.add(parseCase(s));
        allCases = tmpCases.toArray(new Case[0]);

        if (allCases.length <= 0)
            ((TextView) findViewById(R.id.caseText)).setText(getString(R.string.err_no_images, imgDir));
    }

    /**
     * Handles when the user pushes the next case button.
     *
     * @param v Android app thing.
     */
    public void onRoll(View v) {
        handleRoll();
    }

    /**
     * Handles when the user pushes the reselect button.
     *
     * @param v Android app thing.
     */
    public void onReselect(View v) {
        if (history.size() > 0)
            history.remove(history.size() - 1);
        handleRoll();
    }

    /**
     * Handles when the user changes any options.
     *
     * @param v Android app thing.
     * @throws IOException Thrown by {@link FileWriter#FileWriter(File)} and {@link FileWriter#close()}
     */
    public void onOption(View v) throws IOException {
        //Update properties.
        props.setProperty(OPT_SFW, ((RadioButton) findViewById(R.id.rbSafe)).isChecked() ? CH_SAFE : ((RadioButton) findViewById(R.id.rbErotic)).isChecked() ? CH_EROTIC : CH_EXPLICIT);
        props.setProperty(OPT_BOTH_SIDES, Boolean.toString(((CheckBox) findViewById(R.id.cbBothSide)).isChecked()));
        props.setProperty(OPT_NO_REPEAT, Boolean.toString(((CheckBox) findViewById(R.id.cbNoRepeat)).isChecked()));

        //Store properties.
        FileWriter fw = new FileWriter(opts);
        props.store(fw, "Case Selector Options");
        fw.close();
    }

    /**
     * Retrieves the numeric value of a SFW char.
     *
     * @param c The character to get the value from.
     * @return The SFW byte value.
     */
    private byte getSFWByte(String c) {
        switch (c) {
            case CH_SAFE:
                return 0;
            case CH_EROTIC:
                return 1;
            case CH_EXPLICIT:
                return 2;
        }
        return -1;
    }

    /**
     * Retrieves the SFW rating of case {@code c} character index {@code i}.
     *
     * @param c The case to get the SFW rating of.
     * @param i The index of the character to get the SFW rating of.
     * @return The SFW rating.
     */
    private byte getSFWByte(Case c, int i) {
        return getSFWByte(String.valueOf(c.getSFW(i)));
    }

    /**
     * Parses a String into a new Case instance.
     *
     * @param fileName The file name to convert.
     * @return A new Case instance parsed from the string.
     */
    private Case parseCase(String fileName) {
        String[] chars = fileName.substring(2, fileName.lastIndexOf('.')).split("-");
        int last = chars.length - 1;
        chars[last] = chars[last].replaceAll("[0-9]*$", "");
        return new Case(fileName, chars, fileName.charAt(0), fileName.charAt(1));
    }

    /**
     * Checks to see if Case {@code c} has different characters than the last Case.
     *
     * @param c The Case to compare to the last Case.
     * @return True if {@code c} has different characters.
     */
    private boolean lastHasDifChars(Case c) {
        if (history.size() > 0)
            for (String ch : parseCase(history.get(history.size() - 1)).getChars())
                for (String ct : c.getChars())
                    if (ch.equals(ct))
                        return false;
        return true;
    }

    /**
     * Updates the case text and image on the GUI.
     *
     * @param c The Case to put on the GUI.
     */
    private void setCaseView(Case c) {
        ((TextView) findViewById(R.id.caseText)).setText(c.getName());
        ((ImageView) findViewById(R.id.iImage)).setImageBitmap(BitmapFactory.decodeFile(Paths.get(imgDir.toString(), c.getFileName()).toString()));
    }

    /**
     * Handles picking the next random Case.
     */
    private void handleRoll() {
        boolean avoidRepeat = Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT));
        Case[] avail = getCases(avoidRepeat);

        if (avail.length <= 0) {
            //Reset history and try again.
            if (history.size() > 0) {
                String last = history.get(history.size() - 1);
                history.clear();
                history.add(last);
            }

            avail = getCases(!avoidRepeat);

            if (avail.length <= 0) {
                ((TextView) findViewById(R.id.caseText)).setText(R.string.err_no_case);
                return;
            }
        }
        Case sel = avail[RAND.nextInt(avail.length)];
        setCaseView(sel);
        history.add(sel.getFileName());

        //Write new history.
        try {
            FileWriter fw = new FileWriter(hist);
            history.forEach(s -> {
                try {
                    fw.append(s).append('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tries to get available options while respecting {@code avoidRepeat}. If no options are found, tries again with {@code avoidRepeat} set to false.
     *
     * @param avoidRepeat Should the function try to avoid a case with same character as previous?
     * @return An array of all available options.
     */
    private Case[] getCases(boolean avoidRepeat) {
        Case[] avail = getAvailableCases(!avoidRepeat);

        if (avail.length <= 0 && avoidRepeat)
            avail = getAvailableCases(true);
        return avail;
    }

    /**
     * Retrieves all available cases to pick from based on selected options and history.
     *
     * @return A Case array containing all valid options.
     */
    private Case[] getAvailableCases(boolean allowRepeat) {
        ArrayList<Case> avail = new ArrayList<>();
        boolean both = Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES));
        byte tarSFW = getSFWByte(props.getProperty(OPT_SFW, CH_EXPLICIT));

        for (Case c : allCases) {
            byte sfw0 = getSFWByte(c, 0), sfw1 = getSFWByte(c, 1);
            //Add Case c to available options if it meets all option and history criteria...
            if ((both && Math.max(sfw0, sfw1) <= tarSFW || !both && Math.min(sfw0, sfw1) <= tarSFW) && !history.contains(c.getFileName()) && (allowRepeat || lastHasDifChars(c)))
                avail.add(c);
        }

        return avail.toArray(new Case[0]);
    }
}