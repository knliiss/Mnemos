DELETE FROM role_permissions
WHERE permission IN (
    'MANAGE_BETS',
    'MANAGE_PERSONALIZATIONS'
);
