# visitera

generated using Luminus version "3.42"

FIXME

## Prerequisites

1. [Leiningen]
2. [Datomic]

[leiningen]: https://github.com/technomancy/leiningen
[datomic]: https://my.datomic.com/downloads/free

## Running

1. Run datomic

   - `cd {datomic-folder}`
   - `bin/transactor config/samples/free-transactor-template.properties`
   - _Optional gui console_: `bin/console -p 8080 dev datomic:free://localhost:4334`

2. Start a web server:

   - `lein repl`
   - `(start)`

3. Start clientYou will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

   - ` lein figwheel`
   - Go to `localhost:3001` in your browserrun 

## License

Copyright © 2019 Aliaksandr SushkevichFIXME
<!--stackedit_data:
eyJoaXN0b3J5IjpbLTE4NjE3MzY5ODVdfQ==
-->