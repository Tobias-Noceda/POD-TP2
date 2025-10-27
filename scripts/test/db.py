import sqlite3
import os
import csv

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
DATASET_PATH = os.path.join(BASE_DIR, 'POD-TP2', 'client', 'src', 'main', 'assembly', 'overlay')
TRIPS_PATH = 'trips-2025-01.csv'
ZONES_PATH = 'zones.csv'

def init_db(db_path):
    path = os.path.join(BASE_DIR, 'scripts', 'test', db_path)
    conn = sqlite3.connect(path)
    cursor = conn.cursor()

    zones_table = """
        CREATE TABLE IF NOT EXISTS zones (
            LocationID INTEGER PRIMARY KEY,
            Borough TEXT,
            Zone TEXT
        )
    """

    trips_table = """
        CREATE TABLE IF NOT EXISTS trips (
            company TEXT,
            request_datetime TEXT,
            pickup_datetime TEXT,
            dropoff_datetime TEXT,
            PULocationID INTEGER,
            DOLocationID INTEGER,
            trip_miles FLOAT,
            base_passenger_fare FLOAT
        )
    """
    cursor.execute(zones_table)
    cursor.execute(trips_table)

    conn.commit()
    conn.close()

    return path

    
def populate(file, table, db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    file_path = os.path.join(DATASET_PATH, file)

    with open(file_path, 'r', encoding='utf-8-sig') as f:  
        reader = csv.DictReader(f, delimiter=';')
        
        reader.fieldnames = [name.strip() for name in reader.fieldnames]

        if table == 'zones':
            rows = [
                (row['LocationID'].strip(), row['Borough'].strip(), row['Zone'].strip())
                for row in reader
            ]
            cursor.executemany(
                "INSERT OR IGNORE INTO zones (LocationID, Borough, Zone) VALUES (?, ?, ?)",
                rows
            )

        elif table == 'trips':
            batch_size = 1000
            batch = []

            for row in reader:
                batch.append((
                    row['company'].strip(),
                    row['request_datetime'].strip(),
                    row['pickup_datetime'].strip(),
                    row['dropoff_datetime'].strip(),
                    row['PULocationID'].strip(),
                    row['DOLocationID'].strip(),
                    row['trip_miles'].strip(),
                    row['base_passenger_fare'].strip()
                ))

                if len(batch) >= batch_size:
                    cursor.executemany(
                        """
                        INSERT INTO trips (
                            company, request_datetime, pickup_datetime, dropoff_datetime,
                            PULocationID, DOLocationID, trip_miles, base_passenger_fare
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        batch
                    )
                    batch = []

            if batch:
                cursor.executemany(
                    """
                    INSERT INTO trips (
                        company, request_datetime, pickup_datetime, dropoff_datetime,
                        PULocationID, DOLocationID, trip_miles, base_passenger_fare
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    batch
                )

    conn.commit()
    conn.close()
    print(f"Inserted data from {file} into {table} table.")

