# ⚡ RecallRush: Gamified Spaced-Repetition Study Platform

A full-stack web application that combines **Anki-style spaced repetition** (smart revision scheduling)
with **Duolingo-style gamification** (XP, levels, daily streaks, leaderboard).
Users create flashcard decks and study them, and the **SM-2 algorithm** schedules each card to reappear
just before the user would forget it.

**Tech stack:** Java 17, Spring Boot 3, Spring Security + JWT, Spring Data JPA (Hibernate), H2 / PostgreSQL, HTML + CSS + vanilla JavaScript.

---

## Features
- User registration and login with JWT authentication and BCrypt password hashing
- Create decks and flashcards; each user can only access their own data
- Study sessions driven by the SM-2 spaced repetition algorithm
- Gamification: XP per review, levels, daily streak
- Statistics dashboard: total cards, cards due, accuracy, 90-day activity heatmap
- Global leaderboard (top 10 by XP)
- A sample "Java Basics" deck is created automatically for every new user

## How to run
Requirements: **Java 17+** and **Maven**.

```
mvn spring-boot:run
```

Open http://localhost:8080, click **Create account**, and start studying.

- Data is stored in the `./data/` folder, so it persists across restarts.
- Database console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/recalldb`, user `sa`, empty password)
- Optional: set the `JWT_SECRET` environment variable (at least 32 characters) to override the development signing key.

## Architecture
```
Browser (app.js) -> JWT Filter -> Controller -> Service -> Repository -> Database
```

| Layer | Responsibility |
|---|---|
| Controller | Receives HTTP requests, validates input, returns JSON |
| Service | Business logic (SM-2, XP, streaks, ownership checks) |
| Repository | Database access through Spring Data JPA |
| Security | JWT filter authenticates each request |

## Project structure
```
com.hima.recallrush
 ├── RecallRushApplication      application entry point
 ├── model/       User, Deck, Card, ReviewLog           JPA entities (database tables)
 ├── repository/  *Repository                           data access interfaces
 ├── dto/         Dtos                                  request/response records
 ├── security/    JwtUtil, JwtAuthFilter, SecurityConfig
 ├── service/     AuthService, DeckService, StudyService, StatsService, Sm2, Gamification
 ├── controller/  Auth, Deck, Study, Stats controllers  REST endpoints
 └── exception/   GlobalExceptionHandler
resources/
 ├── application.properties
 └── static/      index.html, style.css, app.js         frontend
```

## Database design
```
User (1) ---- (many) Deck (1) ---- (many) Card
ReviewLog: standalone history table (userId, cardId, quality, date, xp)
```

## The SM-2 algorithm
After each review the user rates recall quality from 0 to 5.
```
quality < 3 (forgotten): repetitions = 0, interval = 1 day
first success:           interval = 1 day
second success:          interval = 6 days
afterwards:              interval = previous interval x easeFactor
easeFactor += 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)      (never below 1.3)
```
Example with a starting ease factor of 2.5: reviews occur after 1, 6, 15, 38, 94 days.

## Gamification rules
| Rating | Button | XP |
|---|---|---|
| 1 | Again | 2 |
| 3 | Hard | 10 |
| 4 | Good | 15 |
| 5 | Easy | 20 |

- Level = 1 + floor(sqrt(XP / 100))
- Streak increases by 1 for each consecutive day with at least one review; it resets after a missed day.

## REST API
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/auth/register | No | Create an account and receive a token |
| POST | /api/auth/login | No | Log in and receive a token |
| GET | /api/decks | Yes | List the user's decks |
| POST | /api/decks | Yes | Create a deck |
| DELETE | /api/decks/{id} | Yes | Delete a deck and its cards |
| GET | /api/decks/{id}/cards | Yes | List the cards in a deck |
| POST | /api/decks/{id}/cards | Yes | Add a card |
| DELETE | /api/cards/{id} | Yes | Delete a card |
| GET | /api/study/due?deckId= | Yes | Cards due for review |
| POST | /api/study/review/{cardId} | Yes | Submit a review rating (0-5) |
| GET | /api/stats | Yes | Dashboard statistics and heatmap data |
| GET | /api/leaderboard | Yes | Top 10 users by XP |

Authenticated endpoints expect the header `Authorization: Bearer <token>`.

## Key concepts demonstrated
- Layered architecture and dependency injection
- JPA entity relationships (`@OneToMany`, `@ManyToOne`), derived queries and custom JPQL with `GROUP BY`
- Stateless authentication with JWT; password hashing with BCrypt
- Authorization: every query is scoped to the authenticated user
- DTOs (Java records) and Bean Validation
- Global exception handling with `@RestControllerAdvice`
- Transactions (`@Transactional`) and Hibernate dirty checking
- Frontend: Fetch API, DOM manipulation, `localStorage`, XSS-safe rendering

## Switching to PostgreSQL
1. In `pom.xml`, remove the H2 dependency and uncomment the PostgreSQL dependency.
2. In `application.properties`, comment out the H2 lines and uncomment the PostgreSQL lines.
3. Create an empty database named `recallrush`.

## Possible extensions
- Public or shared decks
- CSV import of cards
- Achievement badges
- Scheduled email reminders (`@Scheduled`)
- Unit tests for `Sm2` (a pure function, easy to test)
