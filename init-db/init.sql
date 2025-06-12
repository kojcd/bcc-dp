-- Create database if it doesn't exist
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_database
      WHERE  datname = 'bcc_db') THEN
      CREATE DATABASE bcc_db;
   END IF;
END
$do$;

-- Connect to the database
\c bcc_db;

-- Create users if they don't exist
DO
$do$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'movies_user') THEN
      CREATE USER movies_user WITH PASSWORD 'movies_pass';
   END IF;
   IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'actors_user') THEN
      CREATE USER actors_user WITH PASSWORD 'actors_pass';
   END IF;
END
$do$;

-- Create separate schemas for each service
CREATE SCHEMA IF NOT EXISTS movies;
CREATE SCHEMA IF NOT EXISTS actors;

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS actors.actor_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

-- Grant schema privileges to movies_user
GRANT USAGE, CREATE ON SCHEMA movies TO movies_user;
GRANT USAGE ON SCHEMA actors TO movies_user;
ALTER SCHEMA movies OWNER TO movies_user;

-- Grant schema privileges to actors_user
GRANT USAGE, CREATE ON SCHEMA actors TO actors_user;
GRANT USAGE ON SCHEMA movies TO actors_user;
ALTER SCHEMA actors OWNER TO actors_user;

-- Grant sequence privileges
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA actors TO actors_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA actors TO movies_user;

-- Grant table privileges for movies schema
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA movies TO movies_user;
GRANT SELECT ON ALL TABLES IN SCHEMA movies TO actors_user;

-- Grant table privileges for actors schema
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA actors TO actors_user;
GRANT SELECT ON ALL TABLES IN SCHEMA actors TO movies_user;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA movies GRANT ALL PRIVILEGES ON TABLES TO movies_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA movies GRANT SELECT ON TABLES TO actors_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA movies GRANT ALL PRIVILEGES ON SEQUENCES TO movies_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA movies GRANT USAGE ON SEQUENCES TO actors_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA actors GRANT ALL PRIVILEGES ON TABLES TO actors_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA actors GRANT SELECT ON TABLES TO movies_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA actors GRANT ALL PRIVILEGES ON SEQUENCES TO actors_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA actors GRANT USAGE ON SEQUENCES TO movies_user;

-- Set default schema for users
ALTER USER movies_user SET search_path TO movies;
ALTER USER actors_user SET search_path TO actors;

-- Grant additional permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA movies TO movies_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA actors TO actors_user;

-- Grant usage on all sequences
GRANT USAGE ON ALL SEQUENCES IN SCHEMA movies TO movies_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA actors TO actors_user; 