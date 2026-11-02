# ⚡ RecallRush: Gamified Spaced-Repetition Study Platform

Anki (smart revision) + Duolingo (XP, levels, streaks) mix. Flashcards create chey, study chey,
algorithm nuvvu marchipoye mundu malli revise cheyyistundi.

## Run cheyyadam
Requirements: Java 17+ and Maven.

```
mvn spring-boot:run
```

Browser lo: http://localhost:8080  (Register -> sample "Java Basics" deck automatic ga vastundi)
Database data `./data/` folder lo save avthundi. H2 console: http://localhost:8080/h2-console
(JDBC URL: `jdbc:h2:file:./data/recalldb`, user: `sa`, password: khali)

## Flow (gurtu pettukovali)
```
Browser (app.js) -> [JWT Filter] -> Controller -> Service -> Repository -> Database
   customer          security       waiter        chef      store room     godown
```

## Project structure
```
com.hima.recallrush
 ├── RecallRushApplication     start button
 ├── model/       User, Deck, Card, ReviewLog          (Entities = tables)
 ├── repository/  *Repository                          (DB access, no SQL needed)
 ├── dto/         Dtos (records)                       (request/response shapes)
 ├── security/    JwtUtil, JwtAuthFilter, SecurityConfig
 ├── service/     AuthService, DeckService, StudyService, StatsService, Sm2, Gamification
 ├── controller/  Auth, Deck, Study, Stats controllers (REST APIs)
 └── exception/   GlobalExceptionHandler
resources/static/  index.html + style.css + app.js     (Frontend)
```

## Topics covered (viva ki ready ga undandi)
| Topic | Ekkada |
|---|---|
| REST API (GET/POST/DELETE) | controller/* |
| Layered architecture | controller -> service -> repository |
| JPA relationships (OneToMany / ManyToOne) | User-Deck-Card |
| Spring Data derived queries + custom JPQL (GROUP BY) | repository/* |
| JWT authentication, stateless sessions | security/* |
| BCrypt password hashing | SecurityConfig, AuthService |
| Authorization (user can only touch own data) | `findByIdAndOwnerId`, `findByIdAndDeckOwnerId` |
| DTOs with Java records + Bean Validation | dto/Dtos.java |
| Global exception handling | exception/* |
| `@Transactional` + Hibernate dirty checking | StudyService.review |
| Dependency Injection (constructor) | anni services/controllers |
| Algorithm: SM-2 spaced repetition | service/Sm2.java |
| Gamification (XP, levels, streak logic) | Gamification, StudyService |
| Frontend: fetch API, DOM, localStorage, XSS-safe rendering | app.js |

## SM-2 in 1 minute
```
Rating <3 (forgot): repetitions = 0, interval = 1 day
1st correct: 1 day -> 2nd correct: 6 days -> then: interval x easeFactor
easeFactor += 0.1 - (5-q)*(0.08 + (5-q)*0.02)     (min 1.3)
Example (ease 2.5):  1 -> 6 -> 15 -> 38 -> 94 days
```

## API endpoints
| Method | URL | Auth | Pani |
|---|---|---|---|
| POST | /api/auth/register | no | account create |
| POST | /api/auth/login | no | token |
| GET/POST | /api/decks | yes | list / create deck |
| DELETE | /api/decks/{id} | yes | delete deck |
| GET/POST | /api/decks/{id}/cards | yes | list / add cards |
| DELETE | /api/cards/{id} | yes | delete card |
| GET | /api/study/due?deckId= | yes | due cards |
| POST | /api/study/review/{cardId} | yes | rate a card (quality 0-5) |
| GET | /api/stats | yes | numbers + heatmap |
| GET | /api/leaderboard | yes | top 10 by XP |

## PostgreSQL ki marchadam
1. `pom.xml`: H2 dependency teesesi PostgreSQL dependency uncomment
2. `application.properties`: PostgreSQL lines uncomment, H2 lines comment
3. pgAdmin lo `recallrush` database create

## Extend cheyyadaniki ideas (extra marks ki)
- Deck sharing / public decks (ManyToMany)
- CSV import of cards
- Badges (7-day streak, 100 reviews)
- Daily reminder email with `@Scheduled`
- AI-generated cards from notes (Ollama / Claude API)
- Unit tests for `Sm2` (pure function, test cheyyadam chala easy)
