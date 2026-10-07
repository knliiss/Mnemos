CREATE TABLE bot_agent_group_state (
    agent_id UUID PRIMARY KEY REFERENCES bot_agent_instances(id) ON DELETE CASCADE,
    payload JSONB NOT NULL DEFAULT '{"groups":[],"assignments":{}}'::jsonb,
    revision BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
