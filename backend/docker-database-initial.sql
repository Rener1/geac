CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE categories
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE locations
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    street          VARCHAR(150) NOT NULL,
    number          VARCHAR(20),
    neighborhood    VARCHAR(100) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(2)   NOT NULL,
    zip_code        VARCHAR(10)  NOT NULL,
    reference_point TEXT,
    capacity        INTEGER      NOT NULL
);

CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    user_type     VARCHAR(20),
    created_at    TIMESTAMP        DEFAULT NOW()
);

CREATE TABLE requirements (
                              id SERIAL PRIMARY KEY,
                              description VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE tags
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE events
(
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organizer_id   UUID      NOT NULL REFERENCES users (id),
    category_id    INTEGER      NOT NULL REFERENCES categories (id),
    location_id    INTEGER REFERENCES locations (id),

    title          VARCHAR(200) NOT NULL,
    description    TEXT         NOT NULL,
    online_link    VARCHAR(255),

    start_time     TIMESTAMP    NOT NULL,
    end_time       TIMESTAMP    NOT NULL,
    workload_hours INTEGER      NOT NULL,
    max_capacity   INTEGER      NOT NULL,
    requirement_id INTEGER NOT NULL REFERENCES requirements(id),

    status         VARCHAR(20),
    created_at     TIMESTAMP        DEFAULT NOW()
);

-- SÓ AGORA CRIAMOS EVENT_TAGS
CREATE TABLE event_tags
(
    event_id UUID    NOT NULL REFERENCES events (id),
    tag_id   INTEGER NOT NULL REFERENCES tags (id),
    PRIMARY KEY (event_id, tag_id)
);

-- ==========================================
-- DADOS INICIAIS (SEED) PARA TESTES DE EVENTO
-- ==========================================

INSERT INTO categories (name, description)
VALUES ('Hackathon', 'Maratona de programação imersiva');

INSERT INTO locations (name, street, number, neighborhood, city, state, zip_code, reference_point, capacity)
VALUES ('Laboratório de Informática 01', 'Rua das Flores', '123', 'Centro', 'Surubim', 'PE', '55750-000', 'Prédio Principal', 40);

INSERT INTO requirements (description)
VALUES ('Trazer notebook próprio com carregador');

INSERT INTO tags (name)
VALUES ('Tecnologia');