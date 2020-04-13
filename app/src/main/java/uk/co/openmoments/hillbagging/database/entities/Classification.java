package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "classification")
public class Classification {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "classification_id")
    private int classificationId;

    @ColumnInfo(name = "classification")
    private String classification;

    @ColumnInfo(name = "description")
    private String description;

    public Classification(int classificationId, String classification, String description) {
        this.classificationId = classificationId;
        this.classification = classification;
        this.description = description;
    }

    public int getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(int id) {
        this.classificationId = id;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
