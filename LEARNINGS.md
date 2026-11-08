Day 1: Set up Java, Docker, Spring Boot, PostgreSQL.

Problem: app failed with TimeZone "Asia/Calcutta" error.

Fix: added VM option -Duser.timezone=Asia/Kolkata.



Day 2: Pushed project to GitHub.

Learned: Git saves versions on my laptop, GitHub stores them online. A commit is one saved snapshot with a message.



Day 2 (continued):

Problem: app failed with "Connection to localhost:5432 refused".

Cause: Docker Desktop and the database container were not running.

Fix: opened Docker Desktop, ran docker compose up -d, then started the app.

Routine: every session, open Docker Desktop, run docker compose up -d, then start the app.



Built: Show entity (table "shows") linked to Movie with @ManyToOne.

Learned: table name "shows" because "show" is a reserved word in PostgreSQL.

Learned: a foreign key (movie\_id) is a rule that a show must point to a real movie.

Learned: data survives restarts because of the Docker volume (pgdata).



Built: ShowSeat entity (table "show\_seats") linked to Show.

Learned: one-to-many. The foreign key goes on the "many" side (shows has movie\_id, show\_seats has show\_id).

Learned: the same seat label (A1) in two different shows is two different rows, because each row has its own show\_id.



Day 2: Pushed Show and ShowSeat to GitHub with my own commit.


Built: ShowRepository and ShowController (GET and POST /shows).

Learned: client sends movieId, controller looks up the Movie, so the foreign key is set correctly.

Tested: POST /shows returned the show with its movie nested inside.

Tested: movieId 99 returns 500 because orElseThrow fires and nothing is saved. TODO: return 404 instead.



Built: ShowSeatRepository, auto-creating 10 seats per show, GET /shows/{id}/seats.

Learned: @Transactional = all steps succeed or all are undone.

Learned: foreign keys are enforced by the database, not Spring. Spring's orElseThrow is only a friendly early check.



Problem: Postman showed ECONNREFUSED on port 8080.

Cause: Spring Boot app was stopped.

Fix: started the app from IntelliJ and waited for "Started TicketbookingApplication".

Built: ShowSeatRepository, auto-creating 10 seats per show, GET /shows/{id}/seats.

Learned: @Transactional = all steps succeed or all are undone.

Learned: foreign keys are enforced by the database, not Spring. orElseThrow is only a friendly early check.

Problem: ECONNREFUSED on 8080. Cause: app was stopped. Fix: start the app.

Problem: seats GET returned \[]. Cause: show was created before the seat code. Fix: create a new show.

