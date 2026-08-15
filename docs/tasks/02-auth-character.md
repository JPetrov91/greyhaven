TASK 2 — Authentication and Character Creation
Goal

Implement the complete account and character onboarding flow.

Backend:

Account
Character

Implement:

register
login
logout
current user
character creation
current character

Rules:

unique email;
unique character name;
up to three characters per account; the server tracks the selected active character;
secure password hashing;
session authentication;
HttpOnly cookie;
CSRF protection.

Character starts with defined MVP default stats and 100 gold.

Frontend:

Implement:

registration page
login page
character creation page
authenticated routing
character summary panel

Tests:

registration
duplicate email
authentication
unauthenticated API access
character creation
duplicate character name
second character rejection

Do not implement world or combat yet.

Do not proceed automatically.