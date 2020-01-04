package uk.co.openmoments.hillbagging.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.Classification;

@Dao
public interface ClassificationDAO {
    @Query("SELECT * FROM classification")
    List<Classification> getAll();

    @Query("SELECT * FROM classification WHERE text IN (:textIds)")
    List<Classification> getAllByText(String[] textIds);

    @Insert
    void insertAll(Classification... classifications);

    @Delete
    void delete(Classification classification);
}
