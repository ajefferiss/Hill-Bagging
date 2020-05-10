package uk.co.openmoments.hillbagging.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.HillClassification;

@Dao
public interface HillClassificationDAO {
    @Query("SELECT * FROM hill_classification")
    List<HillClassification> getAll();
}
