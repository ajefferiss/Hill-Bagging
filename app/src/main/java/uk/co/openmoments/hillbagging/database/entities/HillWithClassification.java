package uk.co.openmoments.hillbagging.database.entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class HillWithClassification {
    @Embedded
    public Hill hill;

    @Relation(
            parentColumn = "hId",
            entity = Classification.class,
            entityColumn = "cId",
            associateBy = @Junction(
                    value = HillClassification.class,
                    parentColumn = "hill_id",
                    entityColumn = "classification_id"
            )
    )
    public List<Classification> classifications;
}
