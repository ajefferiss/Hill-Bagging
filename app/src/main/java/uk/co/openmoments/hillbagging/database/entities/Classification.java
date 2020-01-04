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

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "description")
    private String description;

    public Classification(int id, String text, String description) {
        this.id = id;
        this.text = text;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
