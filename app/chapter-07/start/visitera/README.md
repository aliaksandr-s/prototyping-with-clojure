# visitera

generated using Luminus version "3.42"

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

3. Start client:

   - `lein figwheel`
   - Go to `localhost:3001` in your browser

## License

Copyright © 2019 Aliaksandr Sushkevich
