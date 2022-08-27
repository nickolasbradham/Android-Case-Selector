package nbradham.caseselector;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String OPT_SFW = "sfw", OPT_BOTH_SIDES = "both", OPT_NO_REPEAT = "diff", CH_SAFE = "s", CH_EROTIC = "e", CH_EXPLICIT = "x";

    private final Properties props = new Properties();

    private Case[] allCases;
    private File imgDir, hist, opts;
    private ArrayList<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File workDir = getExternalFilesDir(null);
        imgDir = new File(workDir, "images");
        hist = new File(workDir, "history.txt");
        opts = new File(workDir, "options.txt");

        try {
            FileReader fr = new FileReader(opts);
            props.load(fr);
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (props.getProperty(OPT_SFW, CH_EROTIC)) {
            case CH_SAFE:
                ((RadioButton) findViewById(R.id.rbSafe)).setChecked(true);
                break;
            case CH_EROTIC:
                ((RadioButton) findViewById(R.id.rbErotic)).setChecked(true);
        }

        ((CheckBox) findViewById(R.id.cbBothSide)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES, "false")));
        ((CheckBox) findViewById(R.id.cbNoRepeat)).setChecked(Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT, "true")));

        try {
            Scanner scan = new Scanner(hist);
            while (scan.hasNextLine())
                history.add(scan.nextLine());
            scan.close();

            String last = history.get(history.size() - 1);

            ((TextView) findViewById(R.id.caseText)).setText(last);
            ((ImageView) findViewById(R.id.iImage)).setImageBitmap(BitmapFactory.decodeFile(Paths.get(imgDir.toString(), last + ".jpg").toString()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<Case> tmpCases = new ArrayList<>();
        for (String s : Objects.requireNonNull(imgDir.list())) {
            String name = s.substring(0, s.lastIndexOf('.'));
            String[] chars = name.substring(2).split("-");
            tmpCases.add(new Case(name, chars[0], chars.length > 1 ? chars[1] : null, s.charAt(0), s.charAt(1)));
        }
        allCases = tmpCases.toArray(new Case[0]);
    }

    public void onRoll(View v) {
        ArrayList<Case> avail = new ArrayList<>();
        boolean both = Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES));
        byte tarSFW = getSFWByte(props.getProperty(OPT_SFW));
        for (Case c : allCases)
            if ((both && Math.max(getSFWByte(c.getSFW(0)), getSFWByte(c.getSFW(1))) < tarSFW) || (!both && Math.max(getSFWByte(c.getSFW(0)), getSFWByte(c.getSFW(1))) < tarSFW))
                avail.add(c);
    }

    public void onReroll(View v) {
        //TODO Write function.
    }

    public void onOption(View v) throws IOException {
        props.setProperty(OPT_SFW, ((RadioButton) findViewById(R.id.rbSafe)).isChecked() ? CH_SAFE : ((RadioButton) findViewById(R.id.rbErotic)).isChecked() ? CH_EROTIC : CH_EXPLICIT);
        props.setProperty(OPT_BOTH_SIDES, Boolean.toString(((CheckBox) findViewById(R.id.cbBothSide)).isChecked()));
        props.setProperty(OPT_NO_REPEAT, Boolean.toString(((CheckBox) findViewById(R.id.cbNoRepeat)).isChecked()));

        FileWriter fw = new FileWriter(opts);
        props.store(fw, "Case Selector Options");
        fw.close();
    }

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

    private byte getSFWByte(char c) {
        return getSFWByte(c + "");
    }
}