/*
============================================================================
Dummy data
============================================================================
*/

/*
============================================================================
Organizers and participants (Users)
============================================================================
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

-- Participant 1: n.alexiou@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('n.alexiou@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 3);
DECLARE @Part1Id BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@Part1Id, N'ΝΙΚΟΣ', N'ΑΛΕΞΙΟΥ', '+306900000003');

-- Participant 2: g.panagoulis@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('g.panagoulis@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 3);
DECLARE @Part2Id BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@Part2Id, N'ΓΕΩΡΓΙΟΣ', N'ΠΑΝΑΓΟΥΛΗΣ', '+306900000004');

-- Inactive Organizer: inactive.organizer@test.gr
INSERT INTO users (username, password, active, role_id)
VALUES ('inactive.organizer@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 0, 2);
DECLARE @InactiveOrgId BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@InactiveOrgId, N'ΑΝΕΝΕΡΓΟΣ', N'ΔΙΟΡΓΑΝΩΤΗΣ', '+306900000005');

-- Deleted Organizer: deleted.organizer@test.gr
INSERT INTO users (username, password, active, role_id, deleted_at)
VALUES ('deleted.organizer@test.gr', '$2a$10$dgxkyBEChDsvFm44UEHvZuZep27auxuWvdVjZrWPgewe9PY57obvy', 1, 2, GETDATE());
DECLARE @DeletedOrgId BIGINT = SCOPE_IDENTITY();
INSERT INTO users_details (user_id, firstname, lastname, phone_number)
VALUES (@DeletedOrgId, N'ΔΙΕΓΡΑΜΜΕΝΟΣ', N'ΔΙΟΡΓΑΝΩΤΗΣ', '+306900000006');

/*
============================================================================
Webinars
============================================================================
*/
-- Webinars for Papadopoulos
INSERT INTO webinars (title, description, scheduled_date, duration, user_id)
VALUES (N'Εισαγωγή στη Java', 'Java basics', DATEADD(day, 10, GETDATE()), 60, @Org1Id),
       (N'Εισαγωγή στο Spring Boot', 'Spring context', DATEADD(day, 12, GETDATE()), 120, @Org1Id),
       (N'Προηγμένη Μηχανική Δεδομένων', 'Advanced DB', DATEADD(day, 15, GETDATE()), 180, @Org1Id),
       (N'Αρχιτεκτονική REST API', 'REST API architecture', DATEADD(day, 18, GETDATE()), 90, @Org1Id);

-- Webinars for Pappa
INSERT INTO webinars (title, description, scheduled_date, duration, user_id)
VALUES (N'Εισαγωγή στην SQL', 'SQL queries', DATEADD(day, 20, GETDATE()), 90, @Org2Id),
       (N'Το μέλλον της Τεχνητής Νοημοσύνης', 'AI Trends', DATEADD(day, 25, GETDATE()), 45, @Org2Id),
       (N'Βασικές Αρχές Βάσεων Δεδομένων', 'Database fundamentals', DATEADD(day, 28, GETDATE()), 75, @Org2Id),
       (N'Ανάλυση Δεδομένων με SQL', 'SQL analytics', DATEADD(day, 30, GETDATE()), 90, @Org2Id);

-- Deleted webinar by active organizer
INSERT INTO webinars (title, description, scheduled_date, duration, user_id, deleted_at)
VALUES (N'Εισαγωγή στη C#', 'C# basics', DATEADD(day, 5, GETDATE()), 60, @Org1Id, GETDATE());

-- Active webinar by inactive organizer
INSERT INTO webinars (title, description, scheduled_date, duration, user_id)
VALUES (N'Σεμινάριο Ανενεργού Διοργανωτή', 'Inactive organizer', DATEADD(day, 35, GETDATE()), 60, @InactiveOrgId);

-- Deleted webinar by deleted organizer
INSERT INTO webinars (title, description, scheduled_date, duration, user_id, deleted_at)
VALUES (N'Σεμινάριο Διαγραμμένου Διοργανωτή', 'Deleted organizer', DATEADD(day, 40, GETDATE()), 60, @DeletedOrgId,
        GETDATE());

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
DECLARE @RestApiId BIGINT = (SELECT id
                             FROM webinars
                             WHERE title = N'Αρχιτεκτονική REST API'
                               AND user_id = @Org1Id);

DECLARE @SQLId BIGINT = (SELECT id
                         FROM webinars
                         WHERE title = N'Εισαγωγή στην SQL'
                           AND user_id = @Org2Id);
DECLARE @AIId BIGINT = (SELECT id
                        FROM webinars
                        WHERE title = N'Το μέλλον της Τεχνητής Νοημοσύνης'
                          AND user_id = @Org2Id);
DECLARE @DbFundamentalsId BIGINT = (SELECT id
                                    FROM webinars
                                    WHERE title = N'Βασικές Αρχές Βάσεων Δεδομένων'
                                      AND user_id = @Org2Id);
DECLARE @SqlAnalyticsId BIGINT = (SELECT id
                                  FROM webinars
                                  WHERE title = N'Ανάλυση Δεδομένων με SQL'
                                    AND user_id = @Org2Id);

-- Nikos Alexiou enrolls in Papadopoulos webinars
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Part1Id, @JavaId),
       (@Part1Id, @SpringId),
       (@Part1Id, @AdvancedDBId),
       (@Part1Id, @RestApiId);

-- Georgios Panagoulis enrolls in Pappa webinars
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Part2Id, @SQLId),
       (@Part2Id, @AIId),
       (@Part2Id, @DbFundamentalsId),
       (@Part2Id, @SqlAnalyticsId);

-- Cross enrollments for report data
INSERT INTO users_webinars (user_id, webinar_id)
VALUES (@Part1Id, @SQLId),
       (@Part1Id, @AIId),
       (@Part2Id, @SpringId),
       (@Part2Id, @AdvancedDBId);