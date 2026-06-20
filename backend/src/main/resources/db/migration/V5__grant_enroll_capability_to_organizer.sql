/*
============================================================================
Assign enroll capability to ORGANIZER
============================================================================
*/

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ORGANIZER'
  AND c.name = 'ENROLL_IN_WEBINAR';