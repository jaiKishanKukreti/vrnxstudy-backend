## Database Design

VrnxStudy uses PostgreSQL to store user accounts, learning content, question banks, tests, and student attempts.

The schema separates reusable questions from published test questions. When a question is added to a test, its content and options can be stored as snapshots, helping preserve the version used in that test.

### Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        UUID id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR full_name
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    ROLES {
        UUID id PK
        VARCHAR name UK
    }

    USER_ROLES {
        UUID user_id PK, FK
        UUID role_id PK, FK
        TIMESTAMPTZ assigned_at
    }

    EXAMS {
        UUID id PK
        VARCHAR name
        TEXT description
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        TIMESTAMPTZ deleted_at
    }

    SUBJECTS {
        UUID id PK
        UUID exam_id FK
        VARCHAR name
        VARCHAR status
        TIMESTAMPTZ deleted_at
    }

    CHAPTERS {
        UUID id PK
        UUID subject_id FK
        VARCHAR name
        VARCHAR status
        TIMESTAMPTZ deleted_at
    }

    CHAPTER_NOTES {
        UUID id PK
        UUID chapter_id FK
        VARCHAR title
        TEXT content
        INT display_order
        VARCHAR status
        TIMESTAMPTZ deleted_at
    }

    QUESTIONS {
        UUID id PK
        UUID chapter_id FK
        VARCHAR question_type
        TEXT question_text
        TEXT explanation
        VARCHAR difficulty
        NUMERIC marks
        NUMERIC negative_marks
        VARCHAR status
        TIMESTAMPTZ deleted_at
    }

    QUESTION_OPTIONS {
        UUID id PK
        UUID question_id FK
        VARCHAR option_key
        TEXT option_text
        INT display_order
        BOOLEAN is_correct
    }

    TESTS {
        UUID id PK
        VARCHAR title
        VARCHAR test_type
        UUID exam_id FK
        UUID subject_id FK
        UUID chapter_id FK
        INT duration_minutes
        INT max_attempts
        BOOLEAN negative_marking_enabled
        VARCHAR status
        UUID created_by FK
    }

    TEST_QUESTIONS {
        UUID id PK
        UUID test_id FK
        UUID question_id FK
        INT display_order
        TEXT question_text_snapshot
        TEXT explanation_snapshot
        VARCHAR question_type_snapshot
        NUMERIC marks
        NUMERIC negative_marks
    }

    TEST_QUESTION_OPTIONS {
        UUID id PK
        UUID test_question_id FK
        UUID source_option_id FK
        VARCHAR option_key
        TEXT option_text
        INT display_order
        BOOLEAN is_correct
    }

    TEST_ATTEMPTS {
        UUID id PK
        UUID user_id FK
        UUID test_id FK
        VARCHAR status
        NUMERIC score
        NUMERIC max_score
        INT correct_count
        INT wrong_count
        INT unattempted_count
        VARCHAR idempotency_key
        TIMESTAMPTZ started_at
        TIMESTAMPTZ submitted_at
        TIMESTAMPTZ deadline_at
    }

    ATTEMPT_ANSWERS {
        UUID id PK
        UUID attempt_id FK
        UUID test_question_id FK
        JSONB selected_option_ids
        BOOLEAN is_correct
        NUMERIC marks_earned
        TIMESTAMPTZ saved_at
    }

    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned_to

    EXAMS ||--o{ SUBJECTS : contains
    SUBJECTS ||--o{ CHAPTERS : contains
    CHAPTERS ||--o{ CHAPTER_NOTES : has
    CHAPTERS ||--o{ QUESTIONS : contains

    QUESTIONS ||--o{ QUESTION_OPTIONS : has

    EXAMS o|--o{ TESTS : scopes
    SUBJECTS o|--o{ TESTS : scopes
    CHAPTERS o|--o{ TESTS : scopes
    USERS ||--o{ TESTS : creates

    TESTS ||--o{ TEST_QUESTIONS : contains
    QUESTIONS ||--o{ TEST_QUESTIONS : references

    TEST_QUESTIONS ||--o{ TEST_QUESTION_OPTIONS : snapshots
    QUESTION_OPTIONS o|--o{ TEST_QUESTION_OPTIONS : originates_from

    USERS ||--o{ TEST_ATTEMPTS : starts
    TESTS ||--o{ TEST_ATTEMPTS : receives

    TEST_ATTEMPTS ||--o{ ATTEMPT_ANSWERS : records
    TEST_QUESTIONS ||--o{ ATTEMPT_ANSWERS : answered_in
```

### How the schema is organized

**1. Users and roles**

- `users` stores user account information.
- `roles` defines the supported roles: `ROLE_STUDENT`, `ROLE_ADMIN`, and `ROLE_SUPER_ADMIN`.
- `user_roles` connects users and roles through a many-to-many relationship.

**2. Learning content**

The learning hierarchy is:

`EXAMS → SUBJECTS → CHAPTERS`

- `chapter_notes` stores notes associated with chapters.
- `questions` stores the reusable question bank.
- `question_options` stores the options belonging to each question.

**3. Test builder**

- `tests` defines a test, its scope, duration, attempt limit, and marking configuration.
- `test_questions` associates questions with tests and stores snapshots of question content and marking values.
- `test_question_options` stores the options associated with each test-question snapshot.

A question can be reused across different tests without requiring each test to depend entirely on the current question text.

**4. Student attempts**

- `test_attempts` tracks a user's attempt, including status, score, timing, and answer counts.
- `attempt_answers` stores the answer record for each test question within an attempt.
- `selected_option_ids` is a JSONB array containing selected option IDs.

The schema permits an answer to remain ungraded while `is_correct` is `NULL`.

### Database constraints and indexes

The schema uses several PostgreSQL features to protect data integrity:

- UUID primary keys generated with `gen_random_uuid()`.
- Foreign keys to enforce relationships between records.
- Unique constraints to prevent duplicate role assignments, question options, and test-question entries.
- Check constraints to validate statuses, difficulty levels, marking values, and test scope.
- Indexes on commonly queried foreign keys and status fields.
- Soft-delete timestamps on selected learning-content tables.
- Flyway migrations to manage schema changes.

The application uses Hibernate schema validation rather than relying on Hibernate to create or modify the database schema.

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

> **Note:** `attempt_answers.selected_option_ids` is a JSONB array, not a foreign-key relationship to `test_question_options`. Application-level validation is therefore important to ensure that selected options belong to the relevant test question.
