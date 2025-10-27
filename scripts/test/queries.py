import sqlite3
import datetime
import math
from collections import defaultdict
import os

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../..'))
OUTPUT_PATH = os.path.join(BASE_DIR, 'POD-TP2', 'scripts', 'test', 'outputs')

def get_db(db_path):
    conn = sqlite3.connect(db_path)
    return conn

def query1(db_path):
    conn = get_db(db_path)
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
    conn = get_db(db_path)
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
    conn = get_db(db_path)
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

def query4(db_path, borough_filter='Bronx'):
    conn = get_db(db_path)
    cursor = conn.cursor()

    out_file = os.path.join(OUTPUT_PATH, 'query4_output.csv')
    os.makedirs(os.path.dirname(out_file), exist_ok=True)

    f = open(out_file, 'w', encoding='utf-8')
    f.write("pickUpZone;dropOffZone;delayInSeconds\n")

    query = """
        SELECT 
            z1.Zone AS start_zone,
            z2.Zone AS end_zone,
            t.request_datetime,
            t.pickup_datetime
        FROM trips t
        JOIN zones z1 ON t.PULocationID = z1.LocationID
        JOIN zones z2 ON t.DOLocationID = z2.LocationID
        WHERE LOWER(TRIM(z1.Borough)) = LOWER(TRIM(?))
    """

    cursor.execute(query, (borough_filter,))
    results = cursor.fetchall()
    print(f"Fetched {len(results)} rows for borough {borough_filter}")

    max_delay_per_zone = {}

    for start_zone, end_zone, req_dt, pick_dt in results:
        if not req_dt or not pick_dt:
            continue

        try:
            req = datetime.datetime.strptime(req_dt.strip(), "%Y-%m-%d %H:%M:%S")
            pick = datetime.datetime.strptime(pick_dt.strip(), "%Y-%m-%d %H:%M:%S")

            delay_sec = (pick - req).total_seconds()
        except Exception as e:
            print(f"Exception: {e}")
            continue

        if delay_sec <= 0:
            continue

        if start_zone not in max_delay_per_zone:
            max_delay_per_zone[start_zone] = (end_zone, delay_sec)
        else:
            current_end, current_delay = max_delay_per_zone[start_zone]
            if delay_sec > current_delay or (delay_sec == current_delay and end_zone < current_end):
                max_delay_per_zone[start_zone] = (end_zone, delay_sec)

    for start_zone in sorted(max_delay_per_zone.keys()):
        end_zone, delay_sec = max_delay_per_zone[start_zone]
        f.write(f"{start_zone};{end_zone};{int(delay_sec)}\n")

    f.close()
    conn.close()

def query5(db_path):
    conn = get_db(db_path)
    cursor = conn.cursor()

    out_file = os.path.join(OUTPUT_PATH, 'query5_output.csv')
    os.makedirs(os.path.dirname(out_file), exist_ok=True)

    with open(out_file, 'w', encoding='utf-8') as f:
        f.write("company;year;month;ytd_miles\n")

        monthly_totals = defaultdict(lambda: defaultdict(float))

        cursor.execute("SELECT company, request_datetime, trip_miles FROM trips")
        for company, req_dt, miles in cursor:
            if not (company and req_dt and miles):
                continue
            try:
                dt = datetime.datetime.strptime(req_dt.strip(), "%Y-%m-%d %H:%M:%S")
            except Exception:
                continue

            year, month = dt.year, dt.month
            monthly_totals[company][(year, month)] += float(miles)

        for company in sorted(monthly_totals.keys()):
            by_year = defaultdict(list)
            for (year, month) in monthly_totals[company]:
                by_year[year].append(month)

            for year in sorted(by_year.keys()):
                ytd = 0.0
                for month in sorted(by_year[year]):
                    ytd += monthly_totals[company][(year, month)]
                    ytd_trunc = math.trunc(ytd * 100) / 100
                    f.write(f"{company};{year};{month};{ytd_trunc:.2f}\n")
    conn.close()
