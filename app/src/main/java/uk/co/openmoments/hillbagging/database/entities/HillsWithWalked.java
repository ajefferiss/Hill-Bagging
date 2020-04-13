package uk.co.openmoments.hillbagging.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HillsWithWalked {
    @Embedded public Hill hill;

    @Relation(
            parentColumn = "hill_id",
            entityColumn = "hill_id"
    )
    public HillsWalked hillsWalked;
}
