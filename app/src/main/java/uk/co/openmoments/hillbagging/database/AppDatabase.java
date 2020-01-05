package uk.co.openmoments.hillbagging.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import uk.co.openmoments.hillbagging.database.dao.ClassificationDAO;
import uk.co.openmoments.hillbagging.database.dao.HillClassificationDAO;
import uk.co.openmoments.hillbagging.database.dao.HillDao;
import uk.co.openmoments.hillbagging.database.dao.HillsWalkedDAO;
import uk.co.openmoments.hillbagging.database.entities.Classification;
import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillClassification;
import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

@Database(entities = {Hill.class, Classification.class, HillsWalked.class, HillClassification.class}, version = 1)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract HillDao hillDao();

    public abstract ClassificationDAO classificationDAO();

    public abstract HillsWalkedDAO hillsWalkedDAO();

    public abstract HillClassificationDAO hillClassificationDAO();
}
