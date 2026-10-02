CREATE SEQUENCE ticket_code_seq
    AS BIGINT
    INCREMENT BY 1
    START WITH 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(50) NOT NULL,
    code BIGINT NOT NULL DEFAULT nextval('ticket_code_seq'),
    user_id UUID NOT NULL,
    support_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_tickets_code UNIQUE (code),
    CONSTRAINT fk_tickets_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_tickets_support
        FOREIGN KEY (support_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

ALTER SEQUENCE ticket_code_seq OWNED BY tickets.code;
