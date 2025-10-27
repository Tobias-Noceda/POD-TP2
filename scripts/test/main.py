from db import init_db, populate, TRIPS_PATH, ZONES_PATH
from queries import query1, query2, query3, query4, query5

DATABASE = 'test.db'

print("Initializing database")
path = init_db(DATABASE)

print("Populating zones table")
populate(ZONES_PATH, 'zones', path)

print("Populating trips table")
populate(TRIPS_PATH, 'trips', path)

print("Running Query 1:")
query1(path)

print("Running Query 2:")
query2(path)

print("Running Query 3:")
query3(path)

print("Running Query 4:")
query4(path)

print("Running Query 5:")
query5(path)
