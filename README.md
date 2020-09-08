# Test Case for [HHH-14211](https://hibernate.atlassian.net/browse/HHH-14211)

This test illustrates the problematic Oid/LO data mapping that Hibernate uses for ``@Lob String`` fields in PostgreSQL.

For additional details see [HHH-14211](https://hibernate.atlassian.net/browse/HHH-14211). 

  * [Steps to reproduce](#steps-to-reproduce)
  * [Resulting schema (same in both cases)](#resulting-schema--same-in-both-cases-)
  * [Test results with current oid/lo mapping](#test-results-with-current-oid-lo-mapping)
      - [Execution time:](#execution-time-)
      - [Database result:](#database-result-)
  * [Test results with dialect using actual text mapping](#test-results-with-dialect-using-actual-text-mapping)
      - [Execution time:](#execution-time--1)
      - [Database result:](#database-result--1)
  * [Bonus round: `vacuumlo`](#bonus-round---vacuumlo-)
      - [Result with `text`:](#result-with--text--)
      - [Result with `oid/lo` using `text` column:](#result-with--oid-lo--using--text--column-)
      - [Result with `oid/lo` using proper `oid` column:](#result-with--oid-lo--using-proper--oid--column-)

## Steps to reproduce

1. Start a local PostgreSQL database: 
    ```
    $ docker run -it --rm --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:12.4
    ```
2. Switch into the project root and start it (you can configure your db connection in `src/main/resources/application.yml`):
    ```
    $ ./gradlew bootRun
    ```
3. Watch it do its thing
4. After it's done, check out the schema and some sample data (example results below):
    ```
    $ docker exec -it postgres psql -U postgres -d postgres
    
    \d+ test_entity
    select * from information_schema.triggers;
    
    select * from test_entity limit 10;
    ```
5. Stop/reset the database, change the DB dialect in `src/main/resources/application.yml` to use `text` mapping and do it all over.

## Resulting schema (same in both cases)

```
$ docker exec -it postgres psql -U postgres -d postgres
psql (12.4 (Debian 12.4-1.pgdg100+1))
Type "help" for help.

postgres=# \d+ test_entity
                                Table "public.test_entity"
 Column  |  Type  | Collation | Nullable | Default | Storage  | Stats target | Description
---------+--------+-----------+----------+---------+----------+--------------+-------------
 id      | bigint |           | not null |         | plain    |              |
 payload | text   |           |          |         | extended |              |
Indexes:
    "test_entity_pkey" PRIMARY KEY, btree (id)
Access method: heap

postgres=# select * from information_schema.triggers;
 trigger_catalog | trigger_schema | trigger_name | event_manipulation | event_object_catalog | event_object_schema | event_object_table | action_order | action_condition | action_statement | action_orientation | action_timing | action_reference_old_table | action_reference_new_table | action_reference_old_row | action_reference_new_row | created
-----------------+----------------+--------------+--------------------+----------------------+---------------------+--------------------+--------------+------------------+------------------+--------------------+---------------+----------------------------+----------------------------+--------------------------+--------------------------+---------
(0 rows)

```

Note that the `payload` column is of type `text`, even using storage type `extended` for TOAST support.

## Test results with current oid/lo mapping

#### Execution time:
```
Run 0:
    ORM write took 5545 ms
    ORM read took 14996 ms
        result: 320
    Native read took 16168 ms
        result: 320
... took 36713 ms
Run 1:
    ORM write took 6203 ms
    ORM read took 14796 ms
        result: 320
    Native read took 16720 ms
        result: 320
... took 37720 ms
Run 2:
    ORM write took 5398 ms
    ORM read took 16486 ms
        result: 320
    Native read took 13044 ms
        result: 320
... took 34929 ms
Run 3:
    ORM write took 5874 ms
    ORM read took 14854 ms
        result: 320
    Native read took 15098 ms
        result: 320
... took 35827 ms
Run 4:
    ORM write took 5758 ms
    ORM read took 15668 ms
        result: 320
    Native read took 13783 ms
        result: 320
... took 35210 ms

Total 180400 ms
Avg 36080 ms
```

Whoa, that's pretty slow for so little data...

#### Database result:
```
postgres=# select * from test_entity limit 10;
  id  | payload
------+---------
 4001 | 20402
 4002 | 20403
 4003 | 20404
 4004 | 20405
 4005 | 20406
 4006 | 20407
 4007 | 20408
 4008 | 20409
 4009 | 20410
 4010 | 20411
(10 rows)
```

Note that the payload column does not actually contain our text data, but just an OID pointing at the LO data.

You can read the actual data with

```
select convert_from(lo_get(payload::oid), 'UTF-8') from test_entity;
```

## Test results with dialect using actual text mapping

#### Execution time:
```
Run 0:
    ORM write took 258 ms
    ORM read took 54 ms
        result: 320
    Native read took 27 ms
        result: 320
... took 342 ms
Run 1:
    ORM write took 76 ms
    ORM read took 25 ms
        result: 320
    Native read took 20 ms
        result: 320
... took 123 ms
Run 2:
    ORM write took 58 ms
    ORM read took 27 ms
        result: 320
    Native read took 24 ms
        result: 320
... took 111 ms
Run 3:
    ORM write took 47 ms
    ORM read took 17 ms
        result: 320
    Native read took 16 ms
        result: 320
... took 81 ms
Run 4:
    ORM write took 52 ms
    ORM read took 26 ms
        result: 320
    Native read took 23 ms
        result: 320
... took 102 ms

Total 762 ms
Avg 152 ms
```

Yes, that is some okay performance...

#### Database result:
```
postgres=# select * from test_entity limit 10;
  id  |               payload
------+------------------------------------------
 4001 | Fu?Goh%,#CM... <snip actual text data>
 4002 | }l4z[4{Dy&X... <snip actual text data>
 4003 | di,bt:@+1z;... <snip actual text data>
 4004 | +iTsal< 4\7... <snip actual text data>
 4005 | (uyUwIg;"zR... <snip actual text data>
 4006 | U=C+(Au/+NJ... <snip actual text data>
 4007 | w'M.3/\r-y:... <snip actual text data>
 4008 | \,b9z^`WAFd... <snip actual text data>
 4009 | >+Yr)BfsA{u... <snip actual text data>
 4010 | 3ygC{gc%%ev... <snip actual text data>
(10 rows)
```

Note that the column contains the actual text data. Large text is handled by PostgreSQL's `TOAST` transparently and much more efficiently.

## Bonus round: `vacuumlo`

After both test runs, try doing a `vacuumlo` and subsequent query of our text:

```
$ docker exec -it postgres bash -i

$ vacuumlo -U postgres postgres
$ psql -U postgres postgres

postgres=# select payload from test_entity limit 10;

## Only for Oid/lo case - pick an <id> and get lo data:
postgres=# select convert_from(lo_get(<id>), 'UTF-8');
```

#### Result with `text`:

You'll get the text content unharmed

#### Result with `oid/lo` using `text` column:

The LO data was vacuumed by `vacuumlo` since there was no valid reference to it. Trying to access the LO data fails:

```
postgres=# select convert_from(lo_get(<id>), 'UTF-8');
ERROR:  large object <id> does not exist
```

#### Result with `oid/lo` using proper `oid` column: 

If you try the same, but first convert the `text` to oid, the vacuuming won't hurt it:

```
$ psql -U postgres postgres

postgres=# alter table test_entity alter payload set data type oid using payload::oid;
postgres=# \d

$ vacuumlo -U postgres postgres
$ psql -U postgres postgres

postgres=# select convert_from(lo_get(<id>), 'UTF-8');
<actual row data here>
```
