package uk.co.openmoments.hillbagging.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.openmoments.hillbagging.database.converters.DateConverter;
import uk.co.openmoments.hillbagging.database.dao.ClassificationDAO;
import uk.co.openmoments.hillbagging.database.dao.HillClassificationDAO;
import uk.co.openmoments.hillbagging.database.dao.HillDao;
import uk.co.openmoments.hillbagging.database.dao.HillWalkedDAO;
import uk.co.openmoments.hillbagging.database.entities.Classification;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillClassification;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

@Database(entities = {Hill.class, Classification.class, HillsWalked.class, HillClassification.class}, version = 2)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract HillDao hillDao();

    public abstract ClassificationDAO classificationDAO();

    public abstract HillClassificationDAO hillClassificationDAO();

    public abstract HillWalkedDAO hillWalkedDAO();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREATS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREATS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class,
                            "hill_bagging.db"
                    ).createFromAsset("database/hill_bagging.db").fallbackToDestructiveMigrationFrom(1, 2).allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }
}
