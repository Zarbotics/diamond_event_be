-- Make somebody an administrator.
--
-- WHY THIS IS A SQL FILE AND NOT A BUTTON
--
-- Because there is deliberately no way to do it from inside the application.
-- `/auth/signup` used to read `setTxtRole("ROLE_ADMIN")`, and that endpoint is
-- public of necessity — it is how a customer creates an account — so anybody
-- who posted an email and a password became an administrator of the whole
-- system. That was the single worst defect found in this codebase, and the fix
-- was to make signup always produce `ROLE_USER`.
--
-- Which leaves promotion as an operation with no endpoint, on purpose. Granting
-- administrator access is not a thing a web request should be able to do; it is
-- a thing somebody with the database password does, deliberately, to a named
-- person. That is this file.
--
-- HOW TO USE IT
--
--   psql -d diamond_ev -v email="'someone@example.com'" -f make-admin.sql
--
-- or just run the statement at the bottom with the address typed in.
--
-- THE PERSON HAS TO EXIST FIRST
--
-- There is no password to set here, because there is no good way to write one:
-- the column holds a bcrypt hash, and hand-making one is how installations end
-- up with a shared password nobody can rotate. So the person signs up normally
-- first — the portal's own Register screen, or `POST /auth/signup`, or
-- "Continue with Google" — which gives them a properly hashed credential and
-- the customer role, and this promotes the account they already have.
--
-- WHEN IT TAKES EFFECT
--
-- Immediately, on their next request. `JwtAuthenticationFilter` reads the role
-- from the database on every call rather than trusting the one in the token,
-- exactly so that a promotion does not need a new sign-in and — far more
-- importantly — a demotion cannot be outlived by a token minted before it.
--
-- TO UNDO IT
--
-- The same statement with 'ROLE_USER'. Nothing else records the role.

\set email :email

UPDATE user_master
SET txt_role = 'ROLE_ADMIN',
    -- An account that cannot sign in is not an administrator. A person who
    -- registered but never confirmed their address would otherwise be promoted
    -- into an account they still cannot open, and the failure would look like
    -- the promotion not having worked.
    bln_email_verified = true,
    bln_is_active = true,
    bln_is_approved = true,
    bln_is_deleted = false,
    updated_date = now()
WHERE txt_email = :email;

-- Says what happened, rather than reporting "UPDATE 0" and leaving you to work
-- out whether the address was wrong or the account was already an admin.
SELECT CASE
           WHEN NOT EXISTS (SELECT 1 FROM user_master WHERE txt_email = :email)
               THEN 'No account with that email. They need to sign up first.'
           ELSE 'Now an administrator: ' || :email
       END AS result;
