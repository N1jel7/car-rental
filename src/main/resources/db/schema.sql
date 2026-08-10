CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(100)        NOT NULL UNIQUE,
    password_hash VARCHAR(60)         NOT NULL,
    full_name     VARCHAR(100)        NOT NULL,
    phone         VARCHAR(20),
    avatar_path   VARCHAR(255),
    role          VARCHAR(10)         NOT NULL DEFAULT 'USER',
    locale        VARCHAR(5)          NOT NULL DEFAULT 'ru',
    created_at    TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE cars
(
    id            BIGSERIAL PRIMARY KEY,
    make          VARCHAR(50)         NOT NULL,
    model         VARCHAR(50)         NOT NULL,
    year          SMALLINT            NOT NULL,
    price_per_day DECIMAL(10, 2)      NOT NULL,
    status        VARCHAR(15)         NOT NULL DEFAULT 'AVAILABLE',
    description   TEXT,
    created_at    TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_cars_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'UNAVAILABLE')),
    CONSTRAINT chk_cars_year   CHECK (year >= 1900 AND year <= EXTRACT(YEAR FROM NOW()) + 1),
    CONSTRAINT chk_cars_price  CHECK (price_per_day > 0)
);

CREATE TABLE car_images
(
    id          BIGSERIAL PRIMARY KEY,
    car_id      BIGINT              NOT NULL,
    file_path   VARCHAR(255)        NOT NULL,
    is_primary  BOOLEAN             NOT NULL DEFAULT false,
    uploaded_at TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_car_images_car
        FOREIGN KEY (car_id) REFERENCES cars (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_car_images_primary
    ON car_images (car_id)
    WHERE is_primary = true;

CREATE TABLE bookings
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT              NOT NULL,
    car_id      BIGINT              NOT NULL,
    date_from   DATE                NOT NULL,
    date_to     DATE                NOT NULL,
    total_price DECIMAL(10, 2)      NOT NULL,
    status      VARCHAR(15)         NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_car
        FOREIGN KEY (car_id) REFERENCES cars (id)
            ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_bookings_dates
        CHECK (date_to > date_from),
    CONSTRAINT chk_bookings_price
        CHECK (total_price > 0)
);

CREATE TABLE reviews
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT              NOT NULL,
    car_id     BIGINT              NOT NULL,
    booking_id BIGINT              NOT NULL UNIQUE,
    rating     SMALLINT            NOT NULL,
    comment    TEXT,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_car
        FOREIGN KEY (car_id) REFERENCES cars (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_reviews_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
            ON DELETE RESTRICT,
    CONSTRAINT chk_reviews_rating
        CHECK (rating >= 1 AND rating <= 5)
);

CREATE INDEX idx_bookings_user_id   ON bookings (user_id);
CREATE INDEX idx_bookings_car_id    ON bookings (car_id);
CREATE INDEX idx_bookings_status    ON bookings (status);
CREATE INDEX idx_reviews_car_id     ON reviews (car_id);
CREATE INDEX idx_car_images_car_id  ON car_images (car_id);
