##### Core ideas is when you have a fixed inventory (flight seats) and there is contention happening for its booking(all users trying to book a seat), you have following options to use the database locking

Approach 1:
```
SELECT * FROM flight WHERE trip = 1 and user_id is null ORDER BY id LIMIT 1;
UPDATE flight SET user_id = ? WHERE trip = 1 and user_id is null and id = ?
```

Approach 2:
```
SELECT * FROM flight WHERE trip = 1 and user_id is null ORDER BY id LIMIT 1 FOR UPDATE;
UPDATE flight SET user_id = ? WHERE trip = 1 and user_id is null and id = ?
```

Approach 3:
```
SELECT * FROM flight WHERE trip = 1 and user_id is null ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED;
UPDATE flight SET user_id = ? WHERE trip = 1 and user_id is null and id = ?
```

See **sample_output.txt**
