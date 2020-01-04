package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "hills_walked", foreignKeys = {
        @ForeignKey(entity = Hill.class, parentColumns = "hId", childColumns = "hill_id")
})
public class HillsWalked {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "wId")
    private int id;

    @ColumnInfo(name = "hill_id")
    private int hillId;

    @ColumnInfo(name = "walked_date")
    private String walkedDate;

    public HillsWalked(int id, int hillId, String walkedDate) {
        this.id = id;
        this.hillId = hillId;
        this.walkedDate = walkedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHillId() {
        return hillId;
    }

    public void setHillId(int hillId) {
        this.hillId = hillId;
    }

    public String getWalkedDate() {
        return walkedDate;
    }

    public void setWalkedDate(String walkedDate) {
        this.walkedDate = walkedDate;
    }
}
