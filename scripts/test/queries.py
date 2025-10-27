import sqlite3
import datetime
import math
from collections import defaultdict
import os

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
OUTPUT_PATH = os.path.join(BASE_DIR, 'scripts', 'test', 'outputs')

def query1(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    out_file = 'query1_output.csv'
    f = open(os.path.join(OUTPUT_PATH, out_file), 'w', encoding='utf-8')
    f.write("start_zone;end_zone;total_trips\n")

    query = """
        SELECT 
            z1.Zone AS start_zone,
            z2.Zone AS end_zone,
            COUNT(*) AS total_trips
        FROM trips t
        JOIN zones z1 ON t.PULocationID = z1.LocationID
        JOIN zones z2 ON t.DOLocationID = z2.LocationID
        WHERE t.PULocationID != t.DOLocationID
        GROUP BY z1.Zone, z2.Zone
        ORDER BY total_trips DESC, start_zone ASC, end_zone ASC
    """

    cursor.execute(query)
    results = cursor.fetchall()

    for row in results:
        start_zone, end_zone, total_trips = row
        f.write(f"{start_zone};{end_zone};{total_trips}\n")

    f.close()
    conn.close()


def query2(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    out_file = 'query2_output.csv'
    f = open(os.path.join(OUTPUT_PATH, out_file), 'w', encoding='utf-8')
    f.write("start_zone;end_zone;request_datetime;trip_miles;company\n")

    query = """
        WITH ranked AS (
            SELECT 
                z1.Zone AS start_zone,
                z2.Zone AS end_zone,
                t.request_datetime,
                t.trip_miles,
                t.company,
                ROW_NUMBER() OVER (
                    PARTITION BY z1.Zone
                    ORDER BY t.trip_miles DESC, t.request_datetime DESC
                ) AS rn
            FROM trips t
            JOIN zones z1 ON t.PULocationID = z1.LocationID
            JOIN zones z2 ON t.DOLocationID = z2.LocationID
            WHERE z1.Zone != 'Outside of NYC'
              AND z2.Zone != 'Outside of NYC'
        )
        SELECT 
            start_zone,
            end_zone,
            request_datetime,
            trip_miles,
            company
        FROM ranked
        WHERE rn = 1
        ORDER BY start_zone ASC
    """

    cursor.execute(query)
    results = cursor.fetchall()

    for start_zone, end_zone, req_dt, miles, company in results:
        try:
            dt_obj = datetime.fromisoformat(req_dt)
            formatted_date = dt_obj.strftime("%d/%m/%Y %H:%M:%S")
        except Exception:
            formatted_date = req_dt  

        miles_trunc = math.trunc(float(miles) * 100) / 100
        f.write(f"{start_zone};{end_zone};{formatted_date};{miles_trunc:.2f};{company}\n")
    
    f.close()
    conn.close()


def query3(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    out_file = 'query3_output.csv'
    f = open(os.path.join(OUTPUT_PATH, out_file), 'w', encoding='utf-8')
    f.write("start_borough;company;avg_price\n")

    query = """
        SELECT 
            z.Borough AS start_borough,
            t.company,
            AVG(t.base_passenger_fare) AS avg_price
        FROM trips t
        JOIN zones z ON t.PULocationID = z.LocationID
        WHERE z.Zone != 'Outside of NYC'
        GROUP BY z.Borough, t.company
        ORDER BY avg_price DESC, start_borough ASC, t.company ASC
    """

    cursor.execute(query)
    results = cursor.fetchall()

    for borough, company, avg_price in results:
        # Truncate to 2 decimals
        avg_price_trunc = math.trunc(float(avg_price) * 100) / 100
        f.write(f"{borough};{company};{avg_price_trunc:.2f}\n")

    f.close()
    conn.close()


def query4(db_path, borough_filter='Manhattan'):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    out_file = 'query4_output.csv'
    f = open(os.path.join(OUTPUT_PATH, out_file), 'w', encoding='utf-8')
    f.write("start_zone;end_zone;max_delay_seconds\n")

    query = """
        SELECT 
            z1.Zone AS start_zone,
            z2.Zone AS end_zone,
            t.request_datetime,
            t.pickup_datetime
        FROM trips t
        JOIN zones z1 ON t.PULocationID = z1.LocationID
        JOIN zones z2 ON t.DOLocationID = z2.LocationID
        WHERE z1.Borough = ?
    """

    cursor.execute(query, (borough_filter,))
    results = cursor.fetchall()

    max_delay_per_zone = {}

    for start_zone, end_zone, req_dt, pick_dt in results:
        try:
            req = datetime.fromisoformat(req_dt)
            pick = datetime.fromisoformat(pick_dt)
            delay_sec = (pick - req).total_seconds()
        except Exception:
            continue  

        if start_zone not in max_delay_per_zone:
            max_delay_per_zone[start_zone] = (end_zone, delay_sec)
        else:
            current_end, current_delay = max_delay_per_zone[start_zone]
            if delay_sec > current_delay:
                max_delay_per_zone[start_zone] = (end_zone, delay_sec)
            elif delay_sec == current_delay:
                if end_zone < current_end:
                    max_delay_per_zone[start_zone] = (end_zone, delay_sec)

    for start_zone in sorted(max_delay_per_zone.keys()):
        end_zone, delay_sec = max_delay_per_zone[start_zone]
        f.write(f"{start_zone};{end_zone};{int(delay_sec)}\n")

    f.close()
    conn.close()



def query5(db_path):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    out_file = 'query5_output.csv'
    f = open(os.path.join(OUTPUT_PATH, out_file), 'w', encoding='utf-8')
    f.write("company;year;month;ytd_miles\n")

    query = """
        SELECT company, request_datetime, trip_miles
        FROM trips
    """

    cursor.execute(query)
    results = cursor.fetchall()

    data = defaultdict(lambda: defaultdict(lambda: defaultdict(float)))

    for company, req_dt, miles in results:
        try:
            dt = datetime.fromisoformat(req_dt)
            year = dt.year
            month = dt.month
            day = dt.day
        except Exception:
            continue  

        data[company][year][month] += float(miles)

    for company in sorted(data.keys()):
        for year in sorted(data[company].keys()):
            ytd = 0.0
            for month in sorted(data[company][year].keys()):
                ytd += data[company][year][month]
                ytd_trunc = math.trunc(ytd * 100) / 100
                f.write(f"{company};{year};{month};{ytd_trunc:.2f}\n")

    f.close()
    conn.close()
