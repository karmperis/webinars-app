--This query shows a list of active or deleted webinars organized by active, inactive or deleted users.
--For each record it displays:

--ΣΕΜΙΝΑΡΙΟ — the title of the webinar
--ΚΑΤΑΣΤΑΣΗ ΣΕΜΙΝΑΡΙΟΥ — indicates if the webinar is active (ΕΝΕΡΓΟ) or deleted (ΔΙΕΓΡΑΜΜΕΝΟ)
--ΔΙΟΡΓΑΝΩΤΗΣ — the username of the user who created/organizes the webinar
--ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ — the first name of the organizer
--ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ — the last name of the organizer
--ΚΑΤΑΣΤΑΣΗ ΧΡΗΣΤΗ — indicates if the organizer is active (ΕΝΕΡΓΟΣ), inactive (ΑΝΕΝΕΡΓΟΣ), or deleted (ΔΙΕΓΡΑΜΜΕΝΟΣ)

--Example result:
--ΣΕΜΙΝΑΡΙΟ                          ΚΑΤΑΣΤΑΣΗ ΣΕΜΙΝΑΡΙΟΥ  ΔΙΟΡΓΑΝΩΤΗΣ              ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ  ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ  ΚΑΤΑΣΤΑΣΗ ΧΡΗΣΤΗ
--Εισαγωγή στη Java                  ΔΙΕΓΡΑΜΜΕΝΟ           k.papadopoulos@test.gr   ΚΩΣΤΑΣ            ΠΑΠΑΔΟΠΟΥΛΟΣ        ΕΝΕΡΓΟΣ
--Εισαγωγή στην SQL                  ΕΝΕΡΓΟ                m.pappa@test.gr          ΜΑΡΙΑ             ΠΑΠΠΑ               ΑΝΕΝΕΡΓΟΣ
--Εισαγωγή στη C#                    ΔΙΕΓΡΑΜΜΕΝΟ           n.alexiou@test.gr        ΝΙΚΟΣ             ΑΛΕΞΙΟΥ             ΔΙΕΓΡΑΜΜΕΝΟΣ

SELECT w.title      AS 'ΣΕΜΙΝΑΡΙΟ',
       CASE
           WHEN w.deleted_at IS NOT NULL THEN N'ΔΙΕΓΡΑΜΜΕΝΟ'
           ELSE N'ΕΝΕΡΓΟ'
           END      AS 'ΚΑΤΑΣΤΑΣΗ ΣΕΜΙΝΑΡΙΟΥ',
       u.username   AS 'ΔΙΟΡΓΑΝΩΤΗΣ',
       ud.firstname AS 'ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ',
       ud.lastname  AS 'ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ',
       CASE
           WHEN u.deleted_at IS NOT NULL THEN N'ΔΙΕΓΡΑΜΜΕΝΟΣ'
           WHEN u.active = 0 THEN N'ΑΝΕΝΕΡΓΟΣ'
           ELSE N'ΕΝΕΡΓΟΣ'
           END      AS 'ΚΑΤΑΣΤΑΣΗ ΧΡΗΣΤΗ'
FROM webinars w
         INNER JOIN
     users u ON w.user_id = u.id
         INNER JOIN
     users_details ud ON ud.user_id = u.id
WHERE w.deleted_at IS NOT NULL
   OR u.active = 0
   OR u.deleted_at IS NOT NULL
ORDER BY w.deleted_at DESC,
         u.username;