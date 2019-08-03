# Log Custom JSONs Con Log4j2

El server se levanta en `localhost:8080`.

Cuando se le hace un post al endpoint `localhost:8080/` con un JSON
en el body lo loggea como JSON con el `CustomLayout`.

Cuando se le hace un post al endpoint `localhost:8080/json-layout`
con un JSON en el body lo loggea como JSON con el `JSONLayout`.