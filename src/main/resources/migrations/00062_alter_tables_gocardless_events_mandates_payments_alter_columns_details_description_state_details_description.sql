--liquibase formatted sql

--changeset uk.gov.pay:alter_table_gocardless_events_alter_column_details_description
ALTER TABLE gocardless_events ALTER COLUMN details_description SET DATA TYPE TEXT;
--rollback ALTER TABLE gocardless_events ALTER COLUMN details_description SET DATA TYPE VARCHAR(255);

--changeset uk.gov.pay:alter_table_mandates_alter_column_state_details_description
ALTER TABLE mandates ALTER COLUMN state_details_description SET DATA TYPE TEXT;
--rollback ALTER TABLE mandates ALTER COLUMN state_details_description SET DATA TYPE VARCHAR(255);

--changeset uk.gov.pay:alter_table_payments_alter_column_state_details_description
ALTER TABLE payments ALTER COLUMN state_details_description SET DATA TYPE TEXT;
--rollback ALTER TABLE payments ALTER COLUMN state_details_description SET DATA TYPE VARCHAR(255);
