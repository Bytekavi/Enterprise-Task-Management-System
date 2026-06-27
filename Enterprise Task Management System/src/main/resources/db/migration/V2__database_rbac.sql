-- Defense in depth: these roles can be mapped to separate service identities in production.
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'task_readonly') THEN
        CREATE ROLE task_readonly NOLOGIN;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'task_writer') THEN
        CREATE ROLE task_writer NOLOGIN;
    END IF;
END $$;

GRANT USAGE ON SCHEMA public TO task_readonly, task_writer;
GRANT SELECT ON users, tasks TO task_readonly;
GRANT SELECT, INSERT, UPDATE, DELETE ON tasks TO task_writer;

ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks FORCE ROW LEVEL SECURITY;
CREATE POLICY task_owner_policy ON tasks
    USING (
        owner_id::text = current_setting('app.current_user_id', true)
        OR assignee_id::text = current_setting('app.current_user_id', true)
        OR current_setting('app.is_admin', true) = 'true'
    )
    WITH CHECK (
        owner_id::text = current_setting('app.current_user_id', true)
        OR current_setting('app.is_admin', true) = 'true'
    );
