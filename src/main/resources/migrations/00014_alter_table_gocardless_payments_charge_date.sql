--liquibase formatted sql

--changeset uk.gov.pay:alter_table-gocardless-payments-charge-date
ALTER TABLE gocardless_payments ADD COLUMN charge_date DATE NOT NULL DEFAULT NOW();
--rollback ALTER TABLE gocardless_payments DROP COLUMN charge_date;
