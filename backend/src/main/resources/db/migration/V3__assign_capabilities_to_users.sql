--V3__assign_capabilities_to_users.sql
--SQL Server 2022 (16.0.1175)
--Collation: GREEK_CI_AS

/*
============================================================================
Assign capabilities to ADMIN (all capabilities)
============================================================================
*/
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ADMIN';

/*
============================================================================
Assign capabilities to ORGANIZER (Specific capabilities)
============================================================================
*/

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ORGANIZER'
  AND c.name IN ('CREATE_WEBINAR', 'EDIT_WEBINAR', 'DELETE_WEBINAR', 'VIEW_WEBINARS');

/*
============================================================================
Assign capabilities to PARTICIPANT (Specific capabilities)
============================================================================
*/
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'PARTICIPANT'
  AND c.name IN ('ENROLL_IN_WEBINAR', 'VIEW_WEBINARS');