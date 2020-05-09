package uk.co.openmoments.hillbagging.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

@Dao
public interface HillWalkedDAO {
    @Query("SELECT * FROM hills_walked")
    List<HillsWalked> getAll();

    @Query("SELECT * FROM hills_walked WHERE hill_id = :hill_id")
    List<HillsWalked> getHillById(int hill_id);

    @Insert
    long[] insertAll(HillsWalked... hills);

    @Delete
    void delete(HillsWalked hill);
}

