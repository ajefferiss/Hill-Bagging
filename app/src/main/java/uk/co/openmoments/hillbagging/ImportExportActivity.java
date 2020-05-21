package uk.co.openmoments.hillbagging;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import uk.co.openmoments.hillbagging.database.AppDatabase;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

public class ImportExportActivity extends AppCompatActivity {
    private static final int PICKFILE_RESULT_CODE = 1;
    private enum HillCols {NUMBER, CLIMBED}
    EnumMap<HillCols, Integer> hillColMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        hillColMap = new EnumMap<>(HillCols.class);
        hillColMap.put(HillCols.NUMBER, 0);
        hillColMap.put(HillCols.CLIMBED, 8);

        Button button = findViewById(R.id.import_btn);
        button.setOnClickListener(v -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, getString(R.string.import_file_choose));
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_RESULT_CODE && resultCode == -1) {
            importCSVFile(data.getData());
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void importCSVFile(Uri fileUri) {

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        try (CSVReader reader = new CSVReader(new InputStreamReader(Objects.requireNonNull(getContentResolver().openInputStream(fileUri))))) {
            // Skip header...
            reader.readNext();

            String[] nextLine;
            List<HillsWalked> walkedHills = new ArrayList<>();
            while ((nextLine = reader.readNext()) != null) {
                int hillId = Integer.parseInt(nextLine[hillColMap.get(HillCols.NUMBER)]);
                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(nextLine[hillColMap.get(HillCols.CLIMBED)]);

                if (database.hillWalkedDAO().getHillById(hillId).isEmpty()) {
                    HillsWalked hillWalked = new HillsWalked();
                    hillWalked.setHillId(hillId);
                    hillWalked.setWalkedDate(java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(date)));
                    walkedHills.add(hillWalked);
                }
            }
            database.hillWalkedDAO().insertAll(walkedHills.toArray(new HillsWalked[walkedHills.size()]));
            Toast.makeText(this, getString(R.string.import_file_success), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(ImportExportActivity.class.getSimpleName(), "Unable to import from CSV File", e);
            Toast.makeText(this, getString(R.string.unable_to_import_file), Toast.LENGTH_SHORT).show();
        }
    }
}
