package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "hill_classification", foreignKeys = {
        @ForeignKey(entity = Hill.class, parentColumns = "hId", childColumns = "hill_id"),
        @ForeignKey(entity = Classification.class, parentColumns = "cId", childColumns = "classification_id")
})
public class HillClassification {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "hill_id")
    private int hillId;

    @ColumnInfo(name = "classification_id")
    private int classificationId;

    public HillClassification(int id, int hillId, int classificationId) {
        this.id = id;
        this.hillId = hillId;
        this.classificationId = classificationId;
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

    public int getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(int classificationId) {
        this.classificationId = classificationId;
    }
}
