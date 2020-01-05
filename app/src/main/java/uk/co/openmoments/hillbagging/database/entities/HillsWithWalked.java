package uk.co.openmoments.hillbagging.database.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class HillsWithWalked {
    @Embedded
    public Hill hill;

    @Relation(
            parentColumn = "hId",
            entity = HillsWalked.class,
            entityColumn = "wId"
    )
    public List<HillsWalked> hillsWalked;
}
