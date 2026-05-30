--This query shows a list of all active webinars with their organizer details and the total number of enrolled participants, sorted by popularity.
--For each active webinar it displays:

--ΣΕΜΙΝΑΡΙΟ — the title of the webinar
--ΔΙΟΡΓΑΝΩΤΗΣ — the username of the user who created/organizes the webinar
--ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ — the first name of the organizer
--ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ — the last name of the organizer
--ΣΥΜΜΕΤΕΧΟΝΤΕΣ — the total count of users enrolled in the webinar (including 0 for empty webinars)

--Example result:
--ΣΕΜΙΝΑΡΙΟ                           ΔΙΟΡΓΑΝΩΤΗΣ              ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ   ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ   ΣΥΜΜΕΤΕΧΟΝΤΕΣ
--Εισαγωγή στο Spring Boot            k.papadopoulos@test.gr   ΚΩΣΤΑΣ             ΠΑΠΑΔΟΠΟΥΛΟΣ         2
--Προηγμένη Μηχανική Δεδομένων        k.papadopoulos@test.gr   ΚΩΣΤΑΣ             ΠΑΠΑΔΟΠΟΥΛΟΣ         1
--Το μέλλον της Τεχνητής Νοημοσύνης   m.pappa@test.gr          ΜΑΡΙΑ              ΠΑΠΠΑ                0

SELECT w.title           AS 'ΣΕΜΙΝΑΡΙΟ',
       u.username        AS 'ΔΙΟΡΓΑΝΩΤΗΣ',
       ud.firstname      AS 'ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ',
       ud.lastname       AS 'ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ',
       COUNT(uw.user_id) AS 'ΣΥΜΜΕΤΕΧΟΝΤΕΣ'
FROM webinars w
         INNER JOIN
     users u ON w.user_id = u.id
         INNER JOIN
     users_details ud ON ud.user_id = u.id
         LEFT JOIN
     users_webinars uw ON uw.webinar_id = w.id
WHERE w.deleted_at IS NULL
GROUP BY w.id,
         w.title,
         u.username,
         ud.firstname,
         ud.lastname
ORDER BY COUNT(uw.user_id) DESC;