# Spring security using JWT token

This is a sample app that acts as authentication server (implements the userdetails service, Authnetication Provider etc) and a resource server (reads and validates the JWT)

The generated JWT is self contained, ie, has all required information to process teh request (from principal to roles/authorities)

Use bellow docker compose file to set up the DB. Then run the scripts to populate the schema with pre-caned data

```
#version: '3.3'
services:
  mysql:
    image: mysql:8.0
    restart: always
    environment:
      MYSQL_DATABASE: 'db'
      # So you don't have to use root, but you can if you like
      MYSQL_USER: 'user'
      # You can use whatever password you like
      MYSQL_PASSWORD: 'password'
      # Password for root access
      MYSQL_ROOT_PASSWORD: 'root'
    ports:
      # <Port exposed> : <MySQL Port running inside container>
      - '3306:3306'
    expose:
      # Opens port 3306 on the container
      - '3306'
      # Where our data will be persisted
      - my-db:/var/lib/mysql
# Names our volume
volumes:
  my-db:


```
