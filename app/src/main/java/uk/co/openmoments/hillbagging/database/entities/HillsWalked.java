package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.sql.Date;

@Entity(tableName = "hills_walked",
        foreignKeys = {@ForeignKey(entity = Hill.class, parentColumns = "hill_id", childColumns = "hill_id")},
        indices = {@Index(name="hills_walked_hill_id_idx", value={"hill_id"})}
)
public class HillsWalked {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "walked_id")
    private int walkedId;

    @ColumnInfo(name = "hill_id")
    private int hillId;

    @ColumnInfo(name = "walked_date")
    @NonNull
    private Date walkedDate;

    public int getWalkedId() {
        return walkedId;
    }

    public void setWalkedId(int walkedId) {
        this.walkedId = walkedId;
    }

    public int getHillId() {
        return hillId;
    }

    public void setHillId(int hillId) {
        this.hillId = hillId;
    }

    public Date getWalkedDate() {
        return walkedDate;
    }

    public void setWalkedDate(Date walkedDate) {
        this.walkedDate = walkedDate;
    }
}
