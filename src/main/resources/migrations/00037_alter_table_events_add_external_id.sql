--liquibase formatted sql

--changeset uk.gov.pay:alter_table_events_add_external_id
ALTER TABLE events DROP COLUMN IF EXISTS external_id;
ALTER TABLE events ADD COLUMN external_id VARCHAR(26);

--rollback ALTER TABLE events DROP COLUMN external_id;