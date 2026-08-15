-- Email uniqueness must be case-insensitive at the database level, matching the
-- character-name rule in V2. Application code normalizes email to lower case, but
-- the database must not depend on that being the only write path.

ALTER TABLE accounts DROP CONSTRAINT uq_accounts_email;

CREATE UNIQUE INDEX uq_accounts_email_lower ON accounts (LOWER(email));
