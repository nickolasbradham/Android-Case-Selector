package nbradham.caseselector;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles all code execution and GUI setup and interactions.
 */
public final class MainActivity extends AppCompatActivity {

    private static final String OPT_SFW = "sfw", OPT_BOTH_SIDES = "both", OPT_NO_REPEAT = "diff", CH_SAFE = "s", CH_EROTIC = "e", CH_EXPLICIT = "x";
    private static final Random RAND = new Random();

    private final Properties props = new Properties();
    private final ArrayList<Case> history = new ArrayList<>();

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
        if (opts.exists()) try {
            FileReader fr = new FileReader(opts);
            props.load(fr);
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        props.putIfAbsent(OPT_SFW, CH_EXPLICIT);
        props.putIfAbsent(OPT_BOTH_SIDES, "false");
        props.putIfAbsent(OPT_NO_REPEAT, "true");

        //Setup app from options.
        switch (props.getProperty(OPT_SFW)) {
            case CH_SAFE:
                ((RadioButton) findViewById(R.id.rbSafe)).setChecked(true);
                break;
            case CH_EROTIC:
                ((RadioButton) findViewById(R.id.rbErotic)).setChecked(true);
        }

        ((CheckBox) findViewById(R.id.cbBothSide)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES)));
        ((CheckBox) findViewById(R.id.cbNoRepeat)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT)));

        //Try to retrieve history from file.
        if (hist.exists()) try {
            Scanner scan = new Scanner(hist);
            while (scan.hasNextLine()) history.add(Case.parse(scan.nextLine()));
            scan.close();

            setCaseView(history.get(history.size() - 1));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Retrieve all cases from image dir.
        ArrayList<Case> tmpCases = new ArrayList<>();
        for (String s : Objects.requireNonNull(imgDir.list()))
            tmpCases.add(Case.parse(s));
        allCases = tmpCases.toArray(new Case[0]);

        if (allCases.length == 0)
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
        history.remove(history.size() - 1);
        handleRoll();
    }

    /**
     * Handles one random case selection.
     */
    private void handleRoll() {
        Case[] avail = getAvailable();
        if(avail.length == 0) {
            ((TextView) findViewById(R.id.caseText)).setText(R.string.err_no_case);
            return;
        }
        Case sel = avail[RAND.nextInt(avail.length)];
        setCaseView(sel);
        history.add(sel);

        //Write new history.
        try {
            FileWriter fw = new FileWriter(hist);
            history.forEach(c -> {
                try {
                    fw.append(c.getFileName()).append('\n');
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((Button) findViewById(R.id.bNext)).setText(String.format(Locale.US, "Next (%d left)", allCases.length - history.size()));
    }

    /**
     * Gets available cases for selection. Includes repeat characters if needed. Resets history if needed.
     *
     * @return An array of Cases that are valid selections.
     */
    private Case[] getAvailable() {
        Case[] avail = availPass();
        if (avail.length == 0) {
            Case last = history.get(history.size() - 1);
            history.clear();
            history.add(last);
            avail = availPass();
        }
        return avail;
    }

    /**
     * Gets available cases for selection. Includes repeat characters if needed.
     *
     * @return An array of Cases that are valid selections.
     */
    private Case[] availPass() {
        boolean avoidRepeat = Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT));
        Case[] avail = getAvailable(avoidRepeat);
        if (avail.length == 0 && avoidRepeat) avail = getAvailable(false);
        return avail;
    }

    /**
     * Gets available cases for selection.
     *
     * @param excludeRepeat Should the function exclude repeat characters?
     * @return An array of Cases that are valid selections.
     */
    private Case[] getAvailable(boolean excludeRepeat) {
        ArrayList<Case> avail = new ArrayList<>();
        byte tarSFW = getSFWN(props.getProperty(OPT_SFW).charAt(0));
        boolean nBoth = !Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES)), repeatOk = !excludeRepeat, notRepeat, aSafe, bSafe;
        Case last = null;
        if (history.size() > 0) last = history.get(history.size() - 1);
        for (Case c : allCases) {
            notRepeat = true;
            if (last != null) outer:for (char c1 : c.getChars())
                for (char c2 : last.getChars())
                    if (c1 == c2) {
                        notRepeat = false;
                        break outer;
                    }
            if ((((aSafe = getSFWN(c.getSFW(0)) >= tarSFW) & (bSafe = getSFWN(c.getSFW(1)) >= tarSFW)) || ((aSafe || bSafe) && nBoth)) && (repeatOk || notRepeat) && !history.contains(c))
                avail.add(c);
        }
        return avail.toArray(new Case[0]);
    }

    /**
     * Converts {@code sfw} character into a comparable byte.
     *
     * @param sfw The SFW rating to convert.
     * @return 2 if {@code sfw} = "s", 1 if {@code sfw} = "e", 0 if {@code sfw} = "x", and -1 for any other value.
     */
    private byte getSFWN(char sfw) {
        switch (String.valueOf(sfw)) {
            case CH_SAFE:
                return 2;
            case CH_EROTIC:
                return 1;
            case CH_EXPLICIT:
                return 0;
        }
        return -1;
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
     * Updates the case text and image on the GUI.
     *
     * @param c The Case to put on the GUI.
     */
    private void setCaseView(Case c) {
        ((TextView) findViewById(R.id.caseText)).setText(c.getName());
        ((ImageView) findViewById(R.id.iImage)).setImageBitmap(BitmapFactory.decodeFile(Paths.get(imgDir.toString(), c.getFileName()).toString()));
    }
}