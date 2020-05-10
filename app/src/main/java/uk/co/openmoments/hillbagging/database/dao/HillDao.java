package uk.co.openmoments.hillbagging.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import uk.co.openmoments.hillbagging.database.entities.Hill;
import uk.co.openmoments.hillbagging.database.entities.HillWithClassification;
import uk.co.openmoments.hillbagging.database.entities.HillsWithWalked;

@Dao
public interface HillDao {
    @Query("SELECT * FROM hill")
    List<HillWithClassification> getAll();

    @Query("SELECT h.hill_id, h.number, h.name, h.region, h.area, h.topo_section, h.county, h.metres, h.feet, h.hill_url, h.latitude, h.longitude, w.walked_id, w.hill_id, w.walked_date FROM hill h, hills_walked w WHERE h.hill_id = w.hill_id ORDER BY w.walked_date DESC")
    LiveData<List<HillsWithWalked>> getAllWalked();

    @Query("SELECT COUNT(*) FROM hill")
    int getHillCount();

    @Query("SELECT * FROM hill WHERE name LIKE :name")
    LiveData<List<Hill>> searchByName(String name);

    @Query("SELECT * FROM hill WHERE latitude > :lat1 AND latitude < :lat AND longitude < :lon AND longitude > :lon1")
    LiveData<List<Hill>> searchByPosition(float lat, float lat1, float lon, float lon1);
}
