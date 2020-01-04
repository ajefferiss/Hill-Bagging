CREATE TABLE hills_walked(wId INTEGER PRIMARY KEY, hill_id INTEGER, walked_date DATE, FOREIGN KEY(hill_id) REFERENCES hill(hId));
CREATE INDEX hills_walked_hill_id_idx ON hills_walked (hill_id);
