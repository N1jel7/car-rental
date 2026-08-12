INSERT INTO users (email, password_hash, full_name, phone, role, locale)
VALUES ('admin@carrental.com',
        '$2a$12$placeholder_replace_with_real_bcrypt_hash',
        'Администратор',
        '+375291234567',
        'ADMIN',
        'ru');

INSERT INTO users (email, password_hash, full_name, phone, role, locale)
VALUES ('ivan@example.com',
        '$2a$12$placeholder_replace_with_real_bcrypt_hash',
        'Иван Петров',
        '+375297654321',
        'USER',
        'ru');

INSERT INTO cars (make, model, year, price_per_day, status, description)
VALUES ('Toyota', 'Camry', 2022, 75.00, 'AVAILABLE',
        'Комфортный седан бизнес-класса. Автоматическая коробка передач, климат-контроль.'),
       ('BMW', 'X5', 2023, 150.00, 'AVAILABLE',
        'Премиальный внедорожник. Полный привод, панорамная крыша.'),
       ('Volkswagen', 'Polo', 2021, 45.00, 'AVAILABLE',
        'Экономичный городской автомобиль. Механическая коробка передач.');