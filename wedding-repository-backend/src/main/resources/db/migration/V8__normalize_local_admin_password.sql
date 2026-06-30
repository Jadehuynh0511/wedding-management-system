-- V8 makes the local development admin account usable for manual authentication checks.
-- This seed password is only for local/dev environments created from the shared Flyway setup.
-- {
--   "username": "admin",
--   "password": "admin123!"
-- }

update users
set
    password_hash = '$2a$10$0oJHZKjyF7S.nnmY8pLC..DtI3HkMfvE0qdWCmDnBaUuvskTcVZLu'
where
    username = 'admin';