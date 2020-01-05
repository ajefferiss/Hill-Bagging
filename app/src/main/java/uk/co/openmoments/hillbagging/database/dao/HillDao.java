package uk.co.openmoments.hillbagging.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillWithClassification;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;

@Dao
public interface HillDao {
    @Query("SELECT * FROM hill")
    List<HillWithClassification> getAll();

    @Query("SELECT * FROM hill")
    List<HillsWithWalked> getAllWalked();

    @Query("SELECT COUNT(*) FROM hill")
    int getHillCount();

    @Query("SELECT * FROM hill WHERE name LIKE :name")
    List<Hill> searchByName(String name);

    @Insert
    void insertAll(Hill... hills);

    @Delete
    void delete(Hill hill);
}
