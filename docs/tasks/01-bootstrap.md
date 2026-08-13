TASK 1 — Project Bootstrap and Architecture
Goal

Create a production-quality foundation for the application without implementing game mechanics yet.

Backend

Create Spring Boot project using:

Java 25
Spring Boot 4.1
Maven
PostgreSQL
Flyway
Spring Security
Spring Data JPA
Validation
Actuator
Testcontainers

Create initial modules:

account
character
world
combat
item
inventory
expedition
market
activity
chat
shared

Create Docker Compose PostgreSQL configuration.

Create:

application.yml
application-local.yml
application-test.yml

Configure Flyway.

Create global API exception handling.

Create health endpoint.

Frontend

Create:

React
TypeScript
Vite
React Router
TanStack Query

Implement basic application shell.

Create routes:

/login
/register
/game

Create main three-column game layout.

No game mechanics yet.

Required output

After implementation:

run backend tests;
run frontend build;
provide a concise summary;
list important architectural decisions;
list all commands required to run the project.

Do not proceed to Task 2 automatically.