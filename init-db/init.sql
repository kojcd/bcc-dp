-- Create schemas
CREATE SCHEMA IF NOT EXISTS movies AUTHORIZATION admin;
CREATE SCHEMA IF NOT EXISTS actors AUTHORIZATION admin;

-- Create service users
CREATE USER movies_user WITH PASSWORD 'movies_pass';
CREATE USER actors_user WITH PASSWORD 'actors_pass';

-- Grant access
GRANT USAGE ON SCHEMA movies TO movies_user;
GRANT USAGE ON SCHEMA actors TO actors_user;

-- Set default schema (search_path) for each user
ALTER ROLE movies_user SET search_path = movies;
ALTER ROLE actors_user SET search_path = actors;

-- Allow users to create tables etc.
GRANT CREATE ON SCHEMA movies TO movies_user;
GRANT CREATE ON SCHEMA actors TO actors_user;