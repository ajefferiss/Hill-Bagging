package uk.co.openmoments.hillbagging;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
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
    private FirebaseAnalytics firebaseAnalytics;

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

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(nextLine[hillColMap.get(HillCols.CLIMBED)]);

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

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ImportExportActivity.class.getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "import_error");
            bundle.putString("full_text", e.toString());
            firebaseAnalytics.logEvent("import_error", bundle);
        }
    }

    private void exportWalkedHills() {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "hills_walked.csv");
        contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/csv");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        }

        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues);
        if (uri == null) {
            Log.e(TAG, "Unable to export CSV file are URI is null");
            Toast.makeText(getApplicationContext(), getString(R.string.unable_to_export_file), Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStreamWriter outputFileWriter = new OutputStreamWriter(Objects.requireNonNull(getContentResolver().openOutputStream(uri)))) {
            outputFileWriter.write(new String(getExportCSV()));
            Toast.makeText(getApplicationContext(), getString(R.string.export_file_success), Toast.LENGTH_LONG).show();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to write output CSV file", ioe);
            Toast.makeText(getApplicationContext(), getString(R.string.unable_to_export_file), Toast.LENGTH_SHORT).show();

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, ImportExportActivity.class.getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "export_error");
            bundle.putString("full_text", ioe.toString());
            firebaseAnalytics.logEvent("export_error", bundle);
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
