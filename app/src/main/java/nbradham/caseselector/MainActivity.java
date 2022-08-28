package nbradham.caseselector;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String OPT_SFW = "sfw", OPT_BOTH_SIDES = "both", OPT_NO_REPEAT = "diff", CH_SAFE = "s", CH_EROTIC = "e", CH_EXPLICIT = "x";
    private static final Random RAND = new Random();

    private final Properties props = new Properties();
    private final ArrayList<String> history = new ArrayList<>();

    private Case[] allCases;
    private File imgDir, hist, opts;

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

            setCaseView(parseCase(history.get(history.size() - 1)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<Case> tmpCases = new ArrayList<>();
        for (String s : Objects.requireNonNull(imgDir.list()))
            tmpCases.add(parseCase(s));
        allCases = tmpCases.toArray(new Case[0]);
    }

    public void onRoll(View v) {
        handleRoll();
    }

    public void onReroll(View v) {
        history.remove(history.size() - 1);
        handleRoll();
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

    private byte getSFWByte(Case c, int i) {
        return getSFWByte(c.getSFW(i));
    }

    private Case parseCase(String fileName) {
        String[] chars = fileName.substring(2, fileName.lastIndexOf('.')).split("-");
        for (byte n = 0; n < chars.length; n++)
            chars[n] = chars[n].replaceAll("[0-9]*$", "");

        return new Case(fileName, chars, fileName.charAt(0), fileName.charAt(1));
    }

    private boolean lastHasDifChars(Case c) {
        if (history.size() > 0)
            for (String ch : parseCase(history.get(history.size() - 1)).getChars())
                for (String ct : c.getChars())
                    if (ch.equals(ct))
                        return false;
        return true;
    }

    private void setCaseView(Case c) {
        ((TextView) findViewById(R.id.caseText)).setText(c.getName());
        ((ImageView) findViewById(R.id.iImage)).setImageBitmap(BitmapFactory.decodeFile(Paths.get(imgDir.toString(), c.getFileName()).toString()));
    }

    private void handleRoll() {
        Case[] avail = getAvailableCases();

        System.out.println("Pool: " + Arrays.toString(avail));

        if (avail.length <= 0) {
            String last = history.get(history.size()-1);
            history.clear();
            history.add(last);

            avail = getAvailableCases();
            System.out.println("Reset Pool: " + Arrays.toString(avail));
        }

        Case sel = avail[RAND.nextInt(avail.length)];
        setCaseView(sel);
        history.add(sel.getFileName());

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

    private Case[] getAvailableCases() {
        ArrayList<Case> avail = new ArrayList<>();
        boolean both = Boolean.parseBoolean(props.getProperty(OPT_BOTH_SIDES)), noRepeat = Boolean.parseBoolean(props.getProperty(OPT_NO_REPEAT));
        byte tarSFW = getSFWByte(props.getProperty(OPT_SFW));

        for (Case c : allCases) {
            byte sfw0 = getSFWByte(c, 0), sfw1 = getSFWByte(c, 1);
            if ((both && Math.max(sfw0, sfw1) <= tarSFW || !both && Math.min(sfw0, sfw1) <= tarSFW) && !history.contains(c.getFileName()) && (!noRepeat || lastHasDifChars(c)))
                avail.add(c);
        }

        return avail.toArray(new Case[0]);
    }
}