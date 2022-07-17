package nbradham.caseselector;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final byte AVAILABLE = 0, USED = 1, LAST = 2;
    private String last;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File workDir = getExternalFilesDir(null), datFile = new File(workDir, "sel.txt");

        try {
            Scanner scanner = new Scanner(datFile);
            HashMap<String, Byte> data = new HashMap<>();

            while (scanner.hasNext())
                data.put(scanner.next(), scanner.nextByte());

            ArrayList<String> options = new ArrayList<>();

            findOptions(data, options);

            if (options.size() == 0) {
                data.forEach((k, v) -> {
                    if (v == USED)
                        data.put(k, AVAILABLE);
                });
                findOptions(data, options);
            }

            Random rand = new Random();
            String next = options.get(rand.nextInt(options.size()));

            data.put(next, LAST);
            data.put(last, USED);

            FileWriter writer = new FileWriter(datFile);
            for (String k : data.keySet())
                writer.append(String.format(Locale.US, "%s %d%n", k, data.get(k)));
            writer.close();

            ((TextView) findViewById(R.id.textView)).setText(next);
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeFile(Paths.get(workDir.toString(),"Images",next+".jpg").toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves available options from {@code data} and places them in
     * {@code options}.
     *
     * @param data    The data to retrieve options from.
     * @param options The {@link ArrayList} to place available options.
     */
    private void findOptions(HashMap<String, Byte> data, ArrayList<String> options) {
        data.forEach((k, v) -> {
            if (v == AVAILABLE)
                options.add(k);
            else if (v == LAST)
                last = k;
        });
    }
}