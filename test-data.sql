--insert data into data-base system tables for testing purposes.

--users-data-exmpl
INSERT INTO users (created_at, email, enabled, password, role, status, username) VALUES
('2026-03-15 10:20:00.00000', 'alex.dev@gmail.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'ADMIN', 'ACTIVE', 'AlexDev'),
('2026-03-18 14:45:12.12345', 'marta_security@outlook.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'ACTIVE', 'MartaSec'),
('2026-03-20 09:15:30.55555', 'johndoe88@yahoo.com', false, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'BANNED', 'JohnDoe88'),
('2026-03-25 18:30:00.00000', 'kristina_k@protonmail.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'ACTIVE', 'KristiK'),
('2026-03-28 11:05:45.99999', 'support_tech@company.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'ADMIN', 'ACTIVE', 'TechSupport'),
('2026-04-01 22:12:05.11111', 'hacker_test@test.io', false, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'PENDING', 'Tester99'),
('2026-04-02 08:40:22.44444', 'olga_v@mail.ru', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'ACTIVE', 'OlgaV'),
('2026-04-03 15:55:55.77777', 'boris_the_blade@gmail.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'INACTIVE', 'BorisBlade'),
('2026-04-03 23:50:10.88888', 'newbie_user@icloud.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'USER', 'ACTIVE', 'Newbie'),
('2026-04-04 12:00:01.00000', 'admin_root@service.com', true, '$2a$10$ExR8zS4.IpxV5/8O8L9RPe8vFsh6vBqL3m6.1D.n0H5n5u5u5u5u.', 'ADMIN', 'ACTIVE', 'SuperUser');

--events-data-exmpl
INSERT INTO access_events (created_at, duration_ms, ip_address, status, username_or_email) VALUES
('2026-04-04 09:15:22.123456', 45, '192.168.1.45', 'FAILED', 'admin'),
('2026-04-04 09:15:25.654321', 42, '192.168.1.45', 'FAILED', 'admin'),
('2026-04-04 09:15:29.001223', 38, '192.168.1.45', 'FAILED', 'admin'),
('2026-04-04 10:30:05.000000', 124, '172.20.10.2', 'SUCCESS', 'AlexDev'),
('2026-04-04 11:45:12.999888', 95, '85.214.10.11', 'SUCCESS', 'MartaSec'),
('2026-04-04 12:10:45.123000', 156, '93.184.216.34', 'SUCCESS', 'KristiK'),
('2026-04-04 03:00:01.555444', 312, '45.12.33.101', 'FAILED', 'root'),
('2026-04-04 03:05:10.222333', 280, '45.12.33.101', 'FAILED', 'SuperUser'),
('2026-04-04 14:10:00.111222', 78, '0:0:0:0:0:0:1', 'SUCCESS', 'heivind23@gmail.com'),
('2026-04-04 14:25:30.333444', 82, '0:0:0:0:0:0:1', 'SUCCESS', 'heivind23@gmail.com'),
('2026-04-04 14:35:15.555666', 110, '192.168.1.10', 'SUCCESS', 'OlgaV'),
('2026-04-04 14:40:05.777888', 65, '172.20.10.2', 'SUCCESS', 'AlexDev');