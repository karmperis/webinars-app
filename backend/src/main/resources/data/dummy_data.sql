/*
============================================================================
Dummy data
============================================================================
*/

/*
============================================================================
Organizers (Users)
============================================================================
The password for all organizers is the same: Password123!
*/
-- Organizer 1: k.papadopoulos@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('k.papadopoulos@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 2);
DECLARE @Org1Id BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@Org1Id, N'ΚΩΣΤΑΣ', N'ΠΑΠΑΔΟΠΟΥΛΟΣ', '+306900000001');

-- Organizer 2: m.pappa@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('m.pappa@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 2);
DECLARE @Org2Id BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@Org2Id, N'ΜΑΡΙΑ', N'ΠΑΠΠΑ', '+306900000002');

-- Organizer 3: n.alexiou@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('n.alexiou@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 2);
DECLARE @Org3Id BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@Org3Id, N'ΝΙΚΟΣ', N'ΑΛΕΞΙΟΥ', '+306900000003');

/*
============================================================================
Webinars
============================================================================
*/
-- Webinars for Papadopoulos
INSERT INTO webinars (title, description, scheduled_date, duration, user_id)
VALUES (N'Εισαγωγή στη Java', 'Java basics', DATEADD(day, 10, GETDATE()), 60, @Org1Id),
       (N'Εισαγωγή στο Spring Boot', 'Spring context', DATEADD(day, 12, GETDATE()), 120, @Org1Id),
       (N'Προηγμένη Μηχανική Δεδομένων', 'Advanced DB', DATEADD(day, 15, GETDATE()), 180, @Org1Id);

-- Webinar for Pappa
INSERT INTO webinars (title, description, scheduled_date, duration, user_id)
VALUES (N'Εισαγωγή στην SQL', 'SQL queries', DATEADD(day, 20, GETDATE()), 90, @Org2Id),
       (N'Το μέλλον της Τεχνητής Νοημοσύνης', 'AI Trends', DATEADD(day, 25, GETDATE()), 45, @Org2Id);

-- Webinar for Alexiou (to be deleted)
INSERT INTO webinars (title, description, scheduled_date, duration, user_id, deleted_at)
VALUES (N'Εισαγωγή στη C#', 'C# basics', DATEADD(day, 5, GETDATE()), 60, @Org3Id, GETDATE());

-- Insert one more deleted for Papadopoulos
UPDATE webinars
SET deleted_at = GETDATE()
WHERE title = N'Εισαγωγή στη Java'
  AND user_id = @Org1Id;

/*
============================================================================
Enrollments (Users in Webinars)
============================================================================
*/

DECLARE @JavaId BIGINT = (SELECT id
                          FROM webinars
                          WHERE title = N'Εισαγωγή στη Java'
                            AND user_id = @Org1Id);
DECLARE @SpringId BIGINT = (SELECT id
                            FROM webinars
                            WHERE title = N'Εισαγωγή στο Spring Boot'
                              AND user_id = @Org1Id);
DECLARE @AdvancedDBId BIGINT = (SELECT id
                                FROM webinars
                                WHERE title = N'Προηγμένη Μηχανική Δεδομένων'
                                  AND user_id = @Org1Id);

DECLARE @SQLId BIGINT = (SELECT id
                         FROM webinars
                         WHERE title = N'Εισαγωγή στην SQL'
                           AND user_id = @Org2Id);
DECLARE @AIId BIGINT = (SELECT id
                        FROM webinars
                        WHERE title = N'Το μέλλον της Τεχνητής Νοημοσύνης'
                          AND user_id = @Org2Id);

--Enrollments: Alexiou (Org3Id) signs up for Papadopoulos's (Org1Id) webinars
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Org3Id, @JavaId),
       (@Org3Id, @SpringId),
       (@Org3Id, @AdvancedDBId);

--Enrollments: Papadopoulos (Org1Id) signs up for Pappa's (Org2Id) webinars
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Org1Id, @SQLId),
       (@Org1Id, @AIId);

--Enrollments: Pappa (Org2Id) signs up for Papadopoulos's (Org1Id) webinars
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Org2Id, @SpringId),
       (@Org2Id, @AdvancedDBId);