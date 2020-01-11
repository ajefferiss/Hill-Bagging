package uk.co.openmoments.hillbagging.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.HillsWalked;

@Dao
public interface HillsWalkedDAO {
    @Query("SELECT * FROM hills_walked")
    LiveData<List<HillsWalked>> getAll();

    @Insert
    void insertAll(HillsWalked... hillsWalked);

    @Delete
    void delete(HillsWalked hillsWalked);
}
