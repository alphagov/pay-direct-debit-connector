--liquibase formatted sql

--changeset uk.gov.pay:alter_table_mandates_state_details
ALTER TABLE mandates ADD COLUMN state_details VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN state_details;

--changeset uk.gov.pay:alter_table_mandates_state_details_description
ALTER TABLE mandates ADD COLUMN state_details_description VARCHAR(255);
--rollback ALTER TABLE mandates DROP COLUMN state_details_description;

--changeset uk.gov.pay:alter_table_payments_state_details
ALTER TABLE payments ADD COLUMN state_details VARCHAR(255);
--rollback ALTER TABLE payments DROP COLUMN state_details;

--changeset uk.gov.pay:alter_table_payments_state_details_description
ALTER TABLE payments ADD COLUMN state_details_description VARCHAR(255);
--rollback ALTER TABLE payments DROP COLUMN state_details_description;
