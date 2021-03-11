CREATE TABLE Result (
    id SERIAL PRIMARY KEY,

    numberOfPatients INTEGER DEFAULT 0 NOT NULL,
    clientId VARCHAR DEFAULT '' NOT NULL,
    queryId VARCHAR DEFAULT '' NOT NULL
);

CREATE TABLE Query (
    id SERIAL PRIMARY KEY,

    queryId VARCHAR DEFAULT '' NOT NULL,
    structuredQuery TEXT DEFAULT '' NOT NULL
);
