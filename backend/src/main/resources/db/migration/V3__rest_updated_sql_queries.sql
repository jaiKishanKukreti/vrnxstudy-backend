-- =========================================================
-- 3. MASTER QUESTION BANK
-- =========================================================

CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    explanation TEXT,
    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    marks NUMERIC(7,2) NOT NULL DEFAULT 1.00,
    negative_marks NUMERIC(7,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_questions_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE RESTRICT,

    CONSTRAINT chk_question_type
        CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE')),

    CONSTRAINT chk_question_difficulty
        CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),

    CONSTRAINT chk_question_marks
        CHECK (marks >= 0 AND negative_marks >= 0),

    CONSTRAINT chk_question_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),

    CONSTRAINT chk_question_deleted
        CHECK (deleted_at IS NULL OR deleted_at >= created_at)
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id UUID NOT NULL,
    option_key VARCHAR(10) NOT NULL,
    option_text TEXT NOT NULL,
    display_order INT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_options_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,

    CONSTRAINT chk_option_order
        CHECK (display_order > 0),

    CONSTRAINT uq_question_option_key
        UNIQUE (question_id, option_key),

    CONSTRAINT uq_question_option_order
        UNIQUE (question_id, display_order),

    -- Composite key supports snapshot ownership validation.
    CONSTRAINT uq_option_id_question
        UNIQUE (id, question_id)
);

-- =========================================================
-- 4. TEST BUILDER & PUBLISHED SNAPSHOTS
-- =========================================================

CREATE TABLE tests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    test_type VARCHAR(20) NOT NULL,
    exam_id UUID,
    subject_id UUID,
    chapter_id UUID,
    duration_minutes INT NOT NULL,
    max_attempts INT NOT NULL DEFAULT 1,
    negative_marking_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tests_exam
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE RESTRICT,

    CONSTRAINT fk_tests_subject
        FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT,

    CONSTRAINT fk_tests_chapter
        FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE RESTRICT,

    CONSTRAINT fk_tests_creator
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,

    CONSTRAINT chk_test_type
        CHECK (test_type IN ('CHAPTER', 'SUBJECT', 'GLOBAL')),

    CONSTRAINT chk_test_scope
        CHECK (
            (test_type = 'CHAPTER'
                AND exam_id IS NOT NULL
                AND subject_id IS NOT NULL
                AND chapter_id IS NOT NULL)
            OR
            (test_type = 'SUBJECT'
                AND exam_id IS NOT NULL
                AND subject_id IS NOT NULL
                AND chapter_id IS NULL)
            OR
            (test_type = 'GLOBAL'
                AND exam_id IS NULL
                AND subject_id IS NULL
                AND chapter_id IS NULL)
        ),

    CONSTRAINT chk_test_duration
        CHECK (duration_minutes > 0),

    CONSTRAINT chk_test_max_attempts
        CHECK (max_attempts > 0),

    CONSTRAINT chk_test_status
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE test_questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id UUID NOT NULL,
    question_id UUID NOT NULL,
    display_order INT NOT NULL,
    question_text_snapshot TEXT NOT NULL,
    explanation_snapshot TEXT,
    question_type_snapshot VARCHAR(30) NOT NULL,
    marks NUMERIC(7,2) NOT NULL,
    negative_marks NUMERIC(7,2) NOT NULL,

    CONSTRAINT fk_test_questions_test
        FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,

    CONSTRAINT fk_test_questions_source
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT,

    CONSTRAINT chk_snapshot_question_type
        CHECK (question_type_snapshot IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE')),

    CONSTRAINT chk_snapshot_marks
        CHECK (marks >= 0 AND negative_marks >= 0),

    CONSTRAINT chk_test_question_order
        CHECK (display_order > 0),

    CONSTRAINT uq_test_question
        UNIQUE (test_id, question_id),

    CONSTRAINT uq_test_question_order
        UNIQUE (test_id, display_order),

    -- Composite keys enforce attempt/test consistency.
    CONSTRAINT uq_test_question_id_test
        UNIQUE (id, test_id),

    CONSTRAINT uq_test_question_id
        UNIQUE (id)
);

CREATE TABLE test_question_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_question_id UUID NOT NULL,
    source_option_id UUID,
    option_key VARCHAR(10) NOT NULL,
    option_text TEXT NOT NULL,
    display_order INT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_snapshot_options_question
        FOREIGN KEY (test_question_id)
        REFERENCES test_questions(id) ON DELETE CASCADE,

    -- Source can be absent if an original option is later deleted.
    CONSTRAINT fk_snapshot_source_option
        FOREIGN KEY (source_option_id)
        REFERENCES question_options(id) ON DELETE SET NULL,

    CONSTRAINT chk_snapshot_option_order
        CHECK (display_order > 0),

    CONSTRAINT uq_snapshot_option_key
        UNIQUE (test_question_id, option_key),

    CONSTRAINT uq_snapshot_option_order
        UNIQUE (test_question_id, display_order),

    CONSTRAINT uq_snapshot_option_id_question
        UNIQUE (id, test_question_id)
);

-- =========================================================
-- 5. TEST ATTEMPTS & STUDENT ANSWERS
-- =========================================================

CREATE TABLE test_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    test_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    score NUMERIC(9,2) NOT NULL DEFAULT 0.00,
    max_score NUMERIC(9,2) NOT NULL DEFAULT 0.00,
    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,
    unattempted_count INT NOT NULL DEFAULT 0,
    idempotency_key VARCHAR(255),
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMPTZ,
    deadline_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_attempts_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_attempts_test
        FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE RESTRICT,
    CONSTRAINT chk_attempt_status
        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT chk_attempt_scores
        CHECK (max_score >= 0),
    CONSTRAINT chk_attempt_counts
        CHECK (
            correct_count >= 0
            AND wrong_count >= 0
            AND unattempted_count >= 0
        ),
    CONSTRAINT chk_attempt_dates
        CHECK (deadline_at >= started_at),
    CONSTRAINT chk_attempt_submit_date
        CHECK (submitted_at IS NULL OR submitted_at >= started_at),
    CONSTRAINT uq_attempt_user_idempotency
        UNIQUE (user_id, idempotency_key)
);

CREATE TABLE attempt_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL,
    test_question_id UUID NOT NULL,

    -- JSON array of selected test_question_options.id values.
    -- Examples: [], ["uuid-1"], ["uuid-1","uuid-2"]
    selected_option_ids JSONB NOT NULL DEFAULT '[]'::jsonb,

    -- NULL until this answer has been graded.
    is_correct BOOLEAN,
    marks_earned NUMERIC(9,2) NOT NULL DEFAULT 0.00,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_answers_attempt
        FOREIGN KEY (attempt_id) REFERENCES test_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_test_question
        FOREIGN KEY (test_question_id) REFERENCES test_questions(id) ON DELETE RESTRICT,
    CONSTRAINT chk_selected_option_ids_array
        CHECK (jsonb_typeof(selected_option_ids) = 'array'),
    CONSTRAINT uq_answer_attempt_question
        UNIQUE (attempt_id, test_question_id)
);

-- =========================================================
-- 6. INDEXES FOR COMMON QUERIES
-- =========================================================

CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_subjects_exam_id ON subjects(exam_id);
CREATE INDEX idx_chapters_subject_id ON chapters(subject_id);
CREATE INDEX idx_notes_chapter_status ON chapter_notes(chapter_id, status);
CREATE INDEX idx_questions_chapter_status ON questions(chapter_id, status);
CREATE INDEX idx_question_options_question ON question_options(question_id);
CREATE INDEX idx_tests_status_type ON tests(status, test_type);
CREATE INDEX idx_tests_creator ON tests(created_by);
CREATE INDEX idx_test_questions_question ON test_questions(question_id);
CREATE INDEX idx_attempts_user_started ON test_attempts(user_id, started_at DESC);
CREATE INDEX idx_attempts_test ON test_attempts(test_id);
CREATE INDEX idx_attempt_answers_attempt ON attempt_answers(attempt_id);
CREATE INDEX idx_attempt_answers_question ON attempt_answers(test_question_id);

-- =========================================================
-- 7. INITIAL ROLES
-- Values must match chk_roles_name in Section 1.
-- =========================================================

INSERT INTO roles (name)
VALUES ('ROLE_STUDENT'), ('ROLE_ADMIN'), ('ROLE_SUPER_ADMIN')
ON CONFLICT (name) DO NOTHING;

