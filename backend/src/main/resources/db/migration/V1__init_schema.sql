CREATE TABLE trends (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(500)  NOT NULL,
    source     VARCHAR(50)   NOT NULL,
    score      INTEGER       NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE posts (
    id         BIGSERIAL PRIMARY KEY,
    trend_id   BIGINT        NOT NULL REFERENCES trends(id),
    content    TEXT          NOT NULL,
    status     VARCHAR(20)   NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE approvals (
    id        BIGSERIAL PRIMARY KEY,
    post_id   BIGINT       NOT NULL UNIQUE REFERENCES posts(id),
    decision  VARCHAR(20)  NOT NULL,
    timestamp TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_posts_status    ON posts(status);
CREATE INDEX idx_posts_trend_id  ON posts(trend_id);
CREATE INDEX idx_trends_source   ON trends(source);
