--V2__insert_roles_capabilities.sql
--SQL Server 2022 (16.0.1175)
--Collation: GREEK_CI_AS

/*
============================================================================
Insert roles
============================================================================
*/
INSERT INTO roles(name)
VALUES ('ADMIN'),
       ('ORGANIZER'),
       ('PARTICIPANT');

/*
============================================================================
Insert capabilities
============================================================================
*/
INSERT INTO capabilities(name, description)
VALUES ('MANAGE_USERS', 'Allows full management of user accounts'),
       ('CREATE_WEBINAR', 'Allows the creation of new webinars'),
       ('EDIT_WEBINAR', 'Allows modifying existing webinar details'),
       ('DELETE_WEBINAR', 'Allows the deletion (soft-delete) of webinars'),
       ('ENROLL_IN_WEBINAR', 'Allows the user to enroll as a participant in a webinar'),
       ('VIEW_WEBINARS', 'Allows viewing the list of active webinars');