# Vocabs Registry installation

These instructions are a work in progress!

Please also refer to `README.md` for the time being, for some of the
missing bits.

(e.g., TBD: reference to
https://github.com/au-research/ANDS-ResearchVocabularies-LDA
for LDA custom configuration.)

# Prerequisites

* JDK for Java 8
* Ant
* MySQL
* Solr
* Tomcat

# Locale setting for JDK

The Java source code has characters encoded in UTF-8. You must have a
locale setting in your environment that means that UTF-8 will be
understood by the JDK.

In the ANDS environment, the command `echo $LANG` prints:
en_AU.UTF-8

If, for you, the command `echo $LANG` prints an empty response, or a
response that does not include `UTF-8` in the variant (i.e., in what
is printed after the "."), set the `LANG` variable to include it in
the variant. For example (for Bourne shell/bash):

```
export LANG=en_AU.UTF-8
```

or

```
export LANG=C.UTF-8
```


# Configuration files

Configuration files for toolkit, registry, roles are in the directory
conf.


## Create Toolkit configuration

Create `conf/toolkit.properties`. You may base it on
`conf/toolkit.properties.sample`.

## Create Registry configuration

### Web application properties

Create `conf/registry.properties`. You may base it on
`conf/registry.properties.sample`.

You decide on the Registry database JDBC URL, username, and password
here, and you will need them in subsequent steps. They are referred to
in subsequent steps as `registry-url`, `registry-user`, and
`registry-password`.

### Liquibase properties

Create `conf/registry-liquibase.properties`. You may base it on
`registry-liquibase.properties.sample`.

## Create Roles configuration

Create `conf/roles.properties`. You may base it on
`conf/roles.properties.sample`.

# Create the Registry database

## Initialize the Registry database

Connect to MySQL. For the following steps, you may need to connect as
a database administrator user.

Create the `registry-user` database user, if it does not already exist.

```
CREATE DATABASE `vocabs_registry` DEFAULT CHARACTER SET utf8;
GRANT ALL PRIVILEGES ON `vocabs_registry`.* TO 'registry-user'@'localhost';
```

## Create Registry database tables

```
tools/dist/liquibase-3.5.3/liquibase --defaultsFile=conf/registry-liquibase.properties dropAll; ant -Dregistrydb-properties=conf/registry-liquibase.properties registry-database-update
```

# Build the Toolkit/Registry web application

`ant war`

# Deploy and start the Toolkit/Registry web application

Deploy the resulting `vocabtoolkit.war` into Tomcat.

You may choose to deploy it using a Tomcat context file in order to
select a convenient context path.

Whatever you decide, in the following, the context path is taken to be 
`http://localhost:8080/registry-context`.

Check the (Tomcat) log files to confirm that the web application is
running.

# Migrate any existing content from an "old-style" vocabs database

To proceed, the web application must have been successfully deployed.

Migrate the content from the "old-style" database into the new, blank
Registry database:

```
wget -O - http://localhost:8080/registry-context/registry/internal/migrateToolkitToRegistry
```

# Define PoolParty server for the Registry

Connect to the MySQL Registry database as the `registry-user` user.

Define the main PoolParty server as follows.
Replace the strings `hostname`, `username`, `password` with the
hostname, username, and password you specified in `conf/toolkit.properties`.

```
INSERT INTO poolparty_servers(id,api_url,username,password) VALUES (1,"hostname","username","password");
```

The Registry software does not currently make use of any additional
PoolParty servers you define in this way.

# Define subject resolver sources, and populate subject resolution

Connect to the MySQL Registry database as the `registry-user` user.

```
INSERT INTO subject_resolver_sources(source,iri) VALUES ('anzsrc-for', 'http://vocabs.ands.org.au/repository/api/sparql/anzsrc-for');
INSERT INTO subject_resolver_sources(source,iri) VALUES ('anzsrc-seo', 'http://vocabs.ands.org.au/repository/api/sparql/anzsrc-seo');
INSERT INTO subject_resolver_sources(source,iri) VALUES ('gcmd', 'http://vocabs.ands.org.au/repository/api/sparql/gcmd-sci');
```

Now run:

```
wget -O - http://localhost:8080/registry-context/registry/internal/populateSubjectResolver
```

# Create Solr collection

TBD

# Create PHP Client API

`ant php-client`