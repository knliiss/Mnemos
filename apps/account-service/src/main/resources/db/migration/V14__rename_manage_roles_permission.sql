UPDATE role_permissions
SET permission = 'MANAGE_BASE_ROLE'
WHERE permission = 'MANAGE_ROLES';
