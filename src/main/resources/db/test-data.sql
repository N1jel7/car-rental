-- Full reset of demo data. Safe to re-run.
TRUNCATE TABLE reviews, bookings, car_images, cars, users RESTART IDENTITY CASCADE;

INSERT INTO users (email, password_hash, full_name, phone, role, locale)
VALUES ('admin@carrental.com',
        '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6',
        'Администратор',
        '+375291234567',
        'ADMIN',
        'ru');

INSERT INTO users (email, password_hash, full_name, phone, role, locale)
VALUES ('user@carrental.com',
        '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6',
        'Иван Петров',
        '+375297654321',
        'USER',
        'ru');

-- Extra reviewers/renters, so ratings and "my bookings" have real variety.
-- Same password hash as above for every test account.
INSERT INTO users (email, password_hash, full_name, phone, role, locale)
VALUES
    ('maria@carrental.com',  '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Мария Иванова',   '+375291112233', 'USER', 'ru'),
    ('dmitry@carrental.com', '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Дмитрий Соколов', '+375292223344', 'USER', 'ru'),
    ('elena@carrental.com',  '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Елена Кузнецова', '+375293334455', 'USER', 'ru'),
    ('alexey@carrental.com', '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Алексей Смирнов', '+375294445566', 'USER', 'ru'),
    ('olga@carrental.com',   '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Ольга Новикова',  '+375295556677', 'USER', 'ru'),
    ('sergey@carrental.com', '$2a$12$q2.IkRuWvHCUY0IjfeW0QO3iRoGvonD3lQ80DMCihqYL7YjokbTT6', 'Сергей Волков',   '+375296667788', 'USER', 'ru');

-- Explicit ids so car_images below can reference them directly.
INSERT INTO cars (id, make, model, year, price_per_day, status, description)
VALUES
    (1,  'Volkswagen',       'Tiguan',      2022, 200.00,  'AVAILABLE',
         'Комфортный и быстрый кроссовер бизнес-класса. Автоматическая коробка передач, климат-контроль, панорама.'),
    (2,  'BMW',          'X5',         2023, 150.00, 'AVAILABLE',
         'Премиальный внедорожник. Полный привод, панорамная крыша.'),
    (3,  'Volkswagen',   'Polo',       2021, 45.00,  'AVAILABLE',
         'Экономичный городской автомобиль. Механическая коробка передач.'),
    (4,  'Audi',         'A4',         2022, 95.00,  'AVAILABLE',
         'Спортивный седан с отличной управляемостью и полным приводом quattro.'),
    (5,  'Mercedes-Benz','E-Class',    2023, 160.00, 'AVAILABLE',
         'Флагманский седан бизнес-класса. Максимальный комфорт для дальних поездок.'),
    (6,  'Honda',        'Civic',      2021, 60.00,  'AVAILABLE',
         'Надёжный компактный седан с низким расходом топлива.'),
    (7,  'Hyundai',      'Solaris',    2020, 40.00,  'AVAILABLE',
         'Один из самых популярных бюджетных седанов. Идеален для города.'),
    (8,  'Kia',          'Rio',        2022, 42.00,  'AVAILABLE',
         'Компактный и манёвренный автомобиль для ежедневных поездок.'),
    (9,  'Skoda',        'Octavia',    2021, 65.00,  'AVAILABLE',
         'Просторный лифтбек с большим багажником, отличное соотношение цены и качества.'),
    (10, 'Ford',         'Focus',      2020, 50.00,  'AVAILABLE',
         'Динамичный хэтчбек с отзывчивым рулевым управлением.'),
    (11, 'Nissan',       'Qashqai',    2022, 80.00,  'AVAILABLE',
         'Кроссовер с высокой посадкой и современной мультимедиа системой.'),
    (12, 'Mazda',        '6',          2021, 70.00,  'AVAILABLE',
         'Стильный седан с выразительным дизайном и мощным двигателем.'),
    (13, 'Renault',      'Duster',     2020, 55.00,  'AVAILABLE',
         'Внедорожник для любых дорог. Просторный салон, доступная цена.'),
    (14, 'Lexus',        'RX',         2023, 180.00, 'AVAILABLE',
         'Люксовый кроссовер с гибридной силовой установкой и тихим салоном.'),
    (15, 'Chevrolet',    'Cruze',      2019, 38.00,  'UNAVAILABLE',
         'Седан на плановом техобслуживании, временно недоступен для бронирования.'),
    (16, 'Peugeot',      '308',        2021, 58.00,  'AVAILABLE',
         'Компактный хэтчбек с оригинальным интерьером i-Cockpit.'),
    (17, 'Opel',         'Astra',      2020, 48.00,  'AVAILABLE',
         'Универсальный хэтчбек с удобной подвеской для городских дорог.'),
    (18, 'Volvo',        'XC60',       2022, 140.00, 'AVAILABLE',
         'Безопасный премиальный кроссовер с богатой комплектацией.'),
    (19, 'Subaru',       'Forester',   2021, 90.00,  'AVAILABLE',
         'Внедорожник с постоянным полным приводом symmetrical AWD.'),
    (20, 'Mitsubishi',   'Outlander',  2020, 85.00,  'BOOKED',
         'Семиместный кроссовер, сейчас забронирован другим клиентом.'),
    (21,  'Toyota',       'Camry',      2022, 75.00,  'AVAILABLE',
     'Комфортный седан бизнес-класса. Автоматическая коробка передач, климат-контроль.');

SELECT setval('cars_id_seq', (SELECT MAX(id) FROM cars));

INSERT INTO car_images (car_id, file_path, is_primary)
VALUES
    (1,  'cars/volkswagen-tiguan-1.jpg',        true),
    (2,  'cars/bmw-x5-1.jpg',              true),
    (2,  'cars/bmw-x5-2.jpg',              false),
    (3,  'cars/vw-polo-1.jpg',             true),
    (3,  'cars/vw-polo-2.jpg',             false),
    (4,  'cars/audi-a4-1.jpg',             true),
    (4,  'cars/audi-a4-2.jpg',             false),
    (5,  'cars/mercedes-eclass-1.jpg',     true),
    (5,  'cars/mercedes-eclass-2.jpg',     false),
    (6,  'cars/honda-civic-1.jpg',         true),
    (6,  'cars/honda-civic-2.jpg',         false),
    (7,  'cars/hyundai-solaris-1.jpg',     true),
    (7,  'cars/hyundai-solaris-2.jpg',     false),
    (8,  'cars/kia-rio-1.jpg',             true),
    (8,  'cars/kia-rio-2.jpg',             false),
    (9,  'cars/skoda-octavia-1.jpg',       true),
    (9,  'cars/skoda-octavia-2.jpg',       false),
    (10, 'cars/ford-focus-1.jpg',          true),
    (10, 'cars/ford-focus-2.jpg',          false),
    (11, 'cars/nissan-qashqai-1.jpg',      true),
    (11, 'cars/nissan-qashqai-2.jpg',      false),
    (12, 'cars/mazda-6-1.jpg',             true),
    (12, 'cars/mazda-6-2.jpg',             false),
    (13, 'cars/renault-duster-1.jpg',      true),
    (13, 'cars/renault-duster-2.jpg',      false),
    (14, 'cars/lexus-rx-1.jpg',            true),
    (14, 'cars/lexus-rx-2.jpg',            false),
    (15, 'cars/chevrolet-cruze-1.jpg',     true),
    (15, 'cars/chevrolet-cruze-2.jpg',     false),
    (16, 'cars/peugeot-308-1.jpg',         true),
    (16, 'cars/peugeot-308-2.jpg',         false),
    (17, 'cars/opel-astra-1.jpg',          true),
    (17, 'cars/opel-astra-2.jpg',          false),
    (18, 'cars/volvo-xc60-1.jpg',          true),
    (18, 'cars/volvo-xc60-2.jpg',          false),
    (19, 'cars/subaru-forester-1.jpg',     true),
    (19, 'cars/subaru-forester-2.jpg',     false),
    (20, 'cars/mitsubishi-outlander-1.jpg',true),
    (20, 'cars/mitsubishi-outlander-2.jpg',false),
    (21,  'cars/toyota-camry-1.jpg',        true),
    (21,  'cars/toyota-camry-2.jpg',        false);

-- Users: 1 admin, 2 Иван Петров, 3 Мария, 4 Дмитрий, 5 Елена, 6 Алексей, 7 Ольга, 8 Сергей.
-- Explicit ids so reviews below can reference bookings directly.
INSERT INTO bookings (id, user_id, car_id, date_from, date_to, total_price, status)
VALUES
    -- Upcoming, not yet confirmed
    (1,  3, 2,  '2026-09-05', '2026-09-08', 450.00, 'PENDING'),
    (2,  4, 7,  '2026-09-10', '2026-09-13', 120.00, 'PENDING'),
    (3,  2, 12, '2026-09-15', '2026-09-17', 140.00, 'PENDING'),

    -- Upcoming, confirmed by admin
    (4,  5, 5,  '2026-09-01', '2026-09-04', 480.00, 'CONFIRMED'),
    (5,  6, 9,  '2026-09-20', '2026-09-23', 195.00, 'CONFIRMED'),
    (6,  2, 5,  '2026-09-25', '2026-09-27', 320.00, 'CONFIRMED'),

    -- Cancelled by renter
    (7,  7, 6,  '2026-07-01', '2026-07-04', 180.00, 'CANCELLED'),
    (8,  8, 11, '2026-07-10', '2026-07-12', 160.00, 'CANCELLED'),
    (9,  2, 11, '2026-07-15', '2026-07-17', 160.00, 'CANCELLED'),

    -- Completed — VW Tiguan (car 1): 9 reviews, all 5.0 (enough to test review pagination)
    (10, 3, 1,  '2026-06-01', '2026-06-03', 400.00, 'COMPLETED'),
    (11, 4, 1,  '2026-06-05', '2026-06-07', 400.00, 'COMPLETED'),
    (12, 5, 1,  '2026-06-10', '2026-06-12', 400.00, 'COMPLETED'),
    (31, 6, 1,  '2026-06-15', '2026-06-17', 400.00, 'COMPLETED'),
    (32, 7, 1,  '2026-06-18', '2026-06-20', 400.00, 'COMPLETED'),
    (33, 8, 1,  '2026-06-21', '2026-06-23', 400.00, 'COMPLETED'),
    (34, 3, 1,  '2026-06-24', '2026-06-26', 400.00, 'COMPLETED'),
    (35, 4, 1,  '2026-06-27', '2026-06-29', 400.00, 'COMPLETED'),
    (36, 5, 1,  '2026-06-30', '2026-07-02', 400.00, 'COMPLETED'),

    -- Completed — BMW X5 (car 2): reviews 5, 4, 4, 3
    (13, 6, 2,  '2026-06-01', '2026-06-03', 300.00, 'COMPLETED'),
    (14, 7, 2,  '2026-06-05', '2026-06-08', 450.00, 'COMPLETED'),
    (15, 8, 2,  '2026-06-10', '2026-06-12', 300.00, 'COMPLETED'),
    (16, 3, 2,  '2026-06-15', '2026-06-17', 300.00, 'COMPLETED'),

    -- Completed — Audi A4 (car 4): reviews 5, 5, 5
    (17, 4, 4,  '2026-06-01', '2026-06-04', 285.00, 'COMPLETED'),
    (18, 5, 4,  '2026-06-06', '2026-06-08', 190.00, 'COMPLETED'),
    (19, 6, 4,  '2026-06-10', '2026-06-13', 285.00, 'COMPLETED'),

    -- Completed — Honda Civic (car 6): reviews 3, 2, 4
    (20, 7, 6,  '2026-06-01', '2026-06-03', 120.00, 'COMPLETED'),
    (21, 8, 6,  '2026-06-05', '2026-06-07', 120.00, 'COMPLETED'),
    (22, 3, 6,  '2026-06-10', '2026-06-12', 120.00, 'COMPLETED'),

    -- Completed — Skoda Octavia (car 9): single review, 4
    (23, 4, 9,  '2026-06-01', '2026-06-03', 130.00, 'COMPLETED'),

    -- Completed — Toyota Camry (car 21): reviews 2, 3, 3, 4, 5 (tests review pagination too)
    (24, 5, 21, '2026-06-01', '2026-06-03', 150.00, 'COMPLETED'),
    (25, 6, 21, '2026-06-05', '2026-06-07', 150.00, 'COMPLETED'),
    (26, 7, 21, '2026-06-10', '2026-06-12', 150.00, 'COMPLETED'),
    (27, 8, 21, '2026-06-15', '2026-06-17', 150.00, 'COMPLETED'),
    (28, 3, 21, '2026-06-20', '2026-06-22', 150.00, 'COMPLETED'),

    -- Иван Петров (user@carrental.com, id 2) — one reviewed, one still reviewable via the UI
    (29, 2, 1,  '2026-05-01', '2026-05-03', 400.00, 'COMPLETED'),
    (30, 2, 3,  '2026-05-10', '2026-05-12', 90.00,  'COMPLETED');

SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));

INSERT INTO reviews (user_id, car_id, booking_id, rating, comment)
VALUES
    -- VW Tiguan
    (3, 1, 10, 5, 'Отличная поездка, машина в идеальном состоянии! Обязательно возьмём снова.'),
    (4, 1, 11, 5, 'Превосходный автомобиль, никаких нареканий. Всё понравилось.'),
    (5, 1, 12, 5, 'Салон в идеальном состоянии, комфортная подвеска, всё понравилось.'),
    (6, 1, 31, 5, 'Мощный и тихий кроссовер, поездка прошла идеально.'),
    (7, 1, 32, 5, 'Панорама, климат-контроль — всё работает превосходно.'),
    (8, 1, 33, 5, 'Лучшая машина, которую я арендовал, однозначно вернусь снова.'),
    (3, 1, 34, 5, 'Второй раз беру эту машину, и снова всё на высшем уровне.'),
    (4, 1, 35, 5, 'Полный привод отлично держит дорогу, никаких нареканий.'),
    (5, 1, 36, 5, 'Идеальное состояние салона и кузова, всё как в описании.'),

    -- BMW X5
    (6, 2, 13, 5, 'Мощный и комфортный внедорожник, поездка запомнилась надолго.'),
    (7, 2, 14, 4, 'Достойный вариант за свои деньги, но был небольшой скол на бампере.'),
    (8, 2, 15, 4, 'Хорошая управляемость, салон чистый, всё устроило.'),
    (3, 2, 16, 3, 'Нормально, но ожидал большего за такую цену.'),

    -- Audi A4
    (4, 4, 17, 5, 'Отличная управляемость и полный привод quattro прекрасно себя показал.'),
    (5, 4, 18, 5, 'Салон в идеальном состоянии, машина как новая.'),
    (6, 4, 19, 5, 'Лучшая поездка за последнее время, буду брать снова.'),

    -- Honda Civic
    (7, 6, 20, 3, 'Средний уровень комфорта, кондиционер работал слабо.'),
    (8, 6, 21, 2, 'Машина оказалась грязной внутри при получении, не самый приятный опыт.'),
    (3, 6, 22, 4, 'В целом неплохо, расход топлива порадовал.'),

    -- Skoda Octavia
    (4, 9, 23, 4, 'Просторный багажник, удобно для поездки с семьёй.'),

    -- Toyota Camry
    (5, 21, 24, 2, 'Были проблемы с подвеской, не самый приятный опыт.'),
    (6, 21, 25, 3, 'Нормальная машина, но кондиционер работал слабо.'),
    (7, 21, 26, 3, 'Средне, ничего особенного, но и нареканий серьёзных нет.'),
    (8, 21, 27, 4, 'Комфортный седан, всё понравилось, немного шумновата на трассе.'),
    (3, 21, 28, 5, 'Отличная машина, климат-контроль работает прекрасно.'),

    -- Иван Петров
    (2, 1, 29, 5, 'Брал уже второй раз, всё как всегда на высшем уровне.');
