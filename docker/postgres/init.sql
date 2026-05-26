-- Cria os dois bancos (um por bounded context).
CREATE DATABASE identity_db;
CREATE DATABASE satellite_db;
GRANT ALL PRIVILEGES ON DATABASE identity_db TO orbittapi;
GRANT ALL PRIVILEGES ON DATABASE satellite_db TO orbittapi;
