--liquibase formatted sql

--changeset uk.gov.pay:alter_table-payers-bank-name
ALTER TABLE payers ADD COLUMN bank_name VARCHAR(255);
--rollback ALTER TABLE payers DROP COLUMN bank_name;
