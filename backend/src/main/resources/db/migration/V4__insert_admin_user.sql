--V4__insert_admin_user.sql
--SQL Server 2022 (16.0.1175)
--Collation: GREEK_CI_AS

/*
============================================================================
Seed Admin User and Profile Details
============================================================================
The password is: Password123!
*/
IF NOT EXISTS
    (SELECT 1
     FROM users
     WHERE username = 'admin')
    BEGIN
        /*
        ============================================================================
        Insert the default ADMIN account into users table
        ============================================================================
       */
        INSERT INTO users (username, password, active, role_id, uuid)
        VALUES ('admin', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 1, NEWID());

        DECLARE @AdminId INT;
        SET @AdminId = SCOPE_IDENTITY();

        /*
        ============================================================================
        Insert corresponding profile details into users_details table
        ============================================================================
        */
        INSERT INTO users_details (user_id, firstname, lastname, phone_number)
        VALUES (@AdminId, 'Nikos', 'Karmperis', '+306900000000');
    END;