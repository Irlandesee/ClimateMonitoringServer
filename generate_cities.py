def parse_file():
    filename = "geonames-and-coordinates.csv"
    geoname_pos = 0
    ascii_name_pos = 2
    country_pos = 4
    coords_pos = 5

    with open(filename, "r", encoding='utf-8') as f:
        lines = f.readlines()
    f.close()
    lines = lines[1:] 
    cities = [] 
    
    for line in lines:
        line = line.split(";")
        geoname_id = line[geoname_pos]
        ascii_name = line[ascii_name_pos]
        country = line[country_pos]
        coords = line[coords_pos].split(",")
        latitude = coords[0].strip()
        longitude = coords[1].strip()
        cities.append((geoname_id, ascii_name, country, latitude, longitude))
    
    return cities

def build_insert_query(cities = []) -> []:
    queries = []
    for city in cities:
        query = f"INSERT INTO public.city (geoname_id, ascii_name, country, country_code, latitude, longitude) VALUES ('{city[0]}', '{city[1]}', '{city[2]}', {city[3]}, {city[4]});"
        queries.append(query)
    return queries

def write_file(filename: str, queries = []):
    with open(filename,  "w") as f:
        for query in queries:
            f.write(query + "\n")
    f.close()

if __name__ == "__main__":
    cities = parse_file()
    queries = build_insert_query(cities)
    write_file("cities.sql", queries)

