package uk.co.openmoments.hillbagging.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "classification")
public class Classification {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "cId")
    private int id;

    @ColumnInfo(name = "classification")
    private String classification;

    @ColumnInfo(name = "description")
    private String description;

    public Classification(int id, String classification, String description) {
        this.id = id;
        this.classification = classification;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
