package uk.co.openmoments.hillbagging.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.Classification;

@Dao
public interface ClassificationDAO {
    @Query("SELECT * FROM classification")
    List<Classification> getAll();

    @Query("SELECT * FROM classification WHERE classification IN (:classifications)")
    List<Classification> getAllByText(String[] classifications);
}
