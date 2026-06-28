/*
============================================================================
Insert additional capabilities
============================================================================
*/
INSERT INTO capabilities(name, description)
VALUES ('MANAGE_ROLES', 'Allows full management of roles'),
       ('MANAGE_CAPABILITIES', 'Allows full management of capabilities'),
       ('MANAGE_WEBINARS', 'Allows full management of webinars'),
       ('VIEW_REPORTS', 'Allows viewing and generating system reports')

/*
============================================================================
Assign additional capabilities to ADMIN
============================================================================
*/
INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
         CROSS JOIN capabilities c
WHERE r.name = 'ADMIN'
  AND c.name IN ('MANAGE_ROLES', 'MANAGE_CAPABILITIES', 'MANAGE_WEBINARS', 'VIEW_REPORTS');