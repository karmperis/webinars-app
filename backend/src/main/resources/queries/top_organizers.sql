--This query shows a list of active organizers who have created 4 or more webinars, along with their total webinar count and total duration, sorted by total duration in descending order.
--For each organizer it displays:

--ΔΙΟΡΓΑΝΩΤΗΣ — the username of the user who created/organizes the webinar
--ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ — the first name of the organizer
--ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ — the last name of the organizer
--ΠΛΗΘΟΣ ΣΕΜΙΝΑΡΙΩΝ — the total number of active webinars they organize
--ΣΥΝΟΛΙΚΗ ΔΙΑΡΚΕΙΑ — the sum of the duration in minutes of all their active webinars

--Example result:
--ΔΙΟΡΓΑΝΩΤΗΣ              ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ   ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ   ΠΛΗΘΟΣ ΣΕΜΙΝΑΡΙΩΝ   ΣΥΝΟΛΙΚΗ ΔΙΑΡΚΕΙΑ(ΛΕΠΤΑ)
--k.papadopoulos@test.gr   ΚΩΣΤΑΣ             ΠΑΠΑΔΟΠΟΥΛΟΣ         8                   480
--m.pappa@test.gr          ΜΑΡΙΑ              ΠΑΠΠΑ                5                   300

SELECT
    u.username AS 'ΔΙΟΡΓΑΝΩΤΗΣ',
    ud.firstname AS 'ΟΝΟΜΑ ΔΙΟΡΓΑΝΩΤΗ',
    ud.lastname AS 'ΕΠΩΝΥΜΟ ΔΙΟΡΓΑΝΩΤΗ',
    COUNT(w.id) AS 'ΠΛΗΘΟΣ ΣΕΜΙΝΑΡΙΩΝ',
    SUM(w.duration) AS 'ΣΥΝΟΛΙΚΗ ΔΙΑΡΚΕΙΑ'
FROM
    users u
        INNER JOIN
    users_details ud ON u.id = ud.user_id
        INNER JOIN
    webinars w ON w.user_id = u.id
WHERE
    w.deleted_at IS NULL
GROUP BY
    u.id,
    u.username,
    ud.firstname,
    ud.lastname
HAVING
    COUNT(w.id) >= 4
ORDER BY
    SUM(w.duration) DESC;