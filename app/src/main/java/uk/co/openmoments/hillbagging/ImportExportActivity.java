package uk.co.openmoments.hillbagging;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private static final String TAG = ImportExportActivity.class.getSimpleName();
    private enum HillCols {NUMBER, CLIMBED}
    EnumMap<HillCols, Integer> hillColMap;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        hillColMap = new EnumMap<>(HillCols.class);
        hillColMap.put(HillCols.NUMBER, 0);
        hillColMap.put(HillCols.CLIMBED, 8);
        database = AppDatabase.getDatabase(getApplicationContext());

        Button button = findViewById(R.id.import_btn);
        button.setOnClickListener(v -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, getString(R.string.import_file_choose));
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        });

        button = findViewById(R.id.export_btn);
        button.setOnClickListener(v -> exportWalkedHills());

        TextView exportText = findViewById(R.id.export_text);
        exportText.setText(new String(getExportCSV()));

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
        try (InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(getContentResolver().openInputStream(fileUri)))) {
            try (CSVReader reader = new CSVReaderBuilder(inputStreamReader).withSkipLines(1).build()) {
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
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to import from CSV File", e);
            Toast.makeText(this, getString(R.string.unable_to_import_file), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportWalkedHills() {
        File outputDir = getApplicationContext().getFilesDir(); //Environment.DIRECTORY_DOCUMENTS);
        File outputFile = new File(outputDir, "hills_walked.csv");

        try (FileWriter outputFileWriter = new FileWriter(outputFile)) {
            outputFileWriter.write(new String(getExportCSV()));
            Toast.makeText(getApplicationContext(), getString(R.string.export_file_success, outputFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to write output CSV file", ioe);
            Toast.makeText(getApplicationContext(), getString(R.string.unable_to_export_file), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private byte[] getExportCSV() {
        byte[] returnArray = new byte[]{};

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
            CSVWriter writer = new CSVWriter(streamWriter);
            String[] headerRecord = {"hillnumber", "climbed"};
            writer.writeNext(headerRecord);

            List<HillsWalked> hillsWalked = database.hillWalkedDAO().getAll();
            hillsWalked.forEach(hillWalked -> {
                String hillId = String.valueOf(hillWalked.getHillId());
                String walkedDate = new SimpleDateFormat("YYYY-MM-dd").format(hillWalked.getWalkedDate());
                writer.writeNext(new String[]{hillId, walkedDate});
            });

            streamWriter.flush();

            returnArray = outputStream.toByteArray();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to create output CSV array: " + ioe.toString());
        }

        return returnArray;
    }
}
