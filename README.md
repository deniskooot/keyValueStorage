# KeyValueStore Application

gRPC Java service used Tarantool.


Using: </br>
-- Java 21 </br>
-- Spring Boot 4 </br>
-- Maven </br>
-- Tarantool 3.2 </br>

Supposed methods:
- put (key, value)        - put new or update existing
- get(key)                - get Value by Key
- delete(key)             - delete by Key
- range(keyFrom, keyTo)   - returns stream of Key-Value pairs
- count                   - count number of database records

Database schema:
{
{name = ‘key’, type = ‘string’},
{name = ‘value’, type = ‘varbinary’ , is_nullable = true}
}

# Running App 

1. Create Docker Tarantool database container:
```bash
docker run --name tarantool \
  -e TARANTOOL_USER_NAME=username \
  -e TARANTOOL_USER_PASSWORD=password \
  -p 3301:3301 \
  -d tarantool/tarantool:3.2.3
```

2. Compile Maven Sources to generate classes from proto file

3. Running app


# Testing 

For api testing you can use messages:
- for put operation:
```
{
"key": {"data": "10"},
"value": {"data": "111="}
}
```
- for get/delete operation:
```
{
"key": {"data": "10"},
"value": {"data": "123="}
}
```

- for range operation:
```
{
"from": {"data": "2"},
"to": {"data": "4"}
}
```
