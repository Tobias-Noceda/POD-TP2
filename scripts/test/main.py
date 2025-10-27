from db import init_db, populate, TRIPS_PATH, ZONES_PATH, DB_PATH
from queries import query1, query2, query3, query4, query5

DATABASE = 'test.db'

def init():
    print("Initializing database")
    init_db()

    print("Populating zones table")
    populate(ZONES_PATH, 'zones', DB_PATH)

    print("Populating trips table")
    populate(TRIPS_PATH, 'trips', DB_PATH)

def run_queries():
    print("Running Query 1")
    query1(DB_PATH)

    print("Running Query 2")
    query2(DB_PATH)

    print("Running Query 3")
    query3(DB_PATH)

    print("Running Query 4")
    query4(DB_PATH)

    print("Running Query 5")
    query5(DB_PATH)

    print("Done.")

def main():

    init()
    run_queries()

if __name__ == "__main__":
    main()