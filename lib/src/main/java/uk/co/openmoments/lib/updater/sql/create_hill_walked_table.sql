CREATE TABLE hills_walked(wId INTEGER PRIMARY KEY NOT NULL, hill_id INTEGER NOT NULL, walked_date INTEGER NOT NULL, FOREIGN KEY(hill_id) REFERENCES hill(hId));
CREATE INDEX hills_walked_hill_id_idx ON hills_walked (hill_id);
