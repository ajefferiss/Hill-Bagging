package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "hill_classification",
        foreignKeys = {
            @ForeignKey(entity = Hill.class, parentColumns = "hill_id", childColumns = "hill_id"),
            @ForeignKey(entity = Classification.class, parentColumns = "classification_id", childColumns = "classification_id")
        },
        indices = {
            @Index(name="hill_classification_hill_id_idx", value={"hill_id"}),
            @Index(name="hill_classification_classification_id_idx", value={"classification_id"})
        }
)
public class HillClassification {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "hill_classification_id")
    private int hillClassificationId;

    @ColumnInfo(name = "hill_id")
    private int hillId;

    @ColumnInfo(name = "classification_id")
    private int classificationId;

    public HillClassification(int hillClassificationId, int hillId, int classificationId) {
        this.hillClassificationId = hillClassificationId;
        this.hillId = hillId;
        this.classificationId = classificationId;
    }

    public int getHillClassificationId() {
        return hillClassificationId;
    }

    public void setHillClassificationId(int hillClassificationId) {
        this.hillClassificationId = hillClassificationId;
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
