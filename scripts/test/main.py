from db import init_db, populate, TRIPS_PATH, ZONES_PATH
from queries import query1, query2, query3, query4, query5

DATABASE = 'test.db'

print("Initializing database")
path = init_db(DATABASE)

print("Populating zones table")
populate(ZONES_PATH, 'zones', path)

print("Populating trips table")
populate(TRIPS_PATH, 'trips', path)

