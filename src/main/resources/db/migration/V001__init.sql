CREATE TABLE Result (
    id SERIAL PRIMARY KEY,

    numberOfPatients INTEGER DEFAULT 0 NOT NULL,
    clientId VARCHAR DEFAULT '' NOT NULL,
    queryId VARCHAR DEFAULT '' NOT NULL
);

CREATE TABLE Query (
    id SERIAL PRIMARY KEY,

    queryId VARCHAR DEFAULT '' NOT NULL
);

CREATE TABLE MEDIA_TYPE_CONTENT (
    queryId VARCHAR DEFAULT '' NOT NULL,
    content TEXT DEFAULT '' NOT NULL
);
