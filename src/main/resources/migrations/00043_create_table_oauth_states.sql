--liquibase formatted sql

--changeset uk.gov.pay:add_table-oauth_states
CREATE TABLE oauth_states (
  id BIGSERIAL PRIMARY KEY,
  gateway_account_external_id VARCHAR(26) NOT NULL,
  state VARCHAR(26) NOT NULL,
  active BOOLEAN DEFAULT TRUE,
  version INTEGER DEFAULT 0 NOT NULL
);
--rollback drop table oauth_states;

--changeset uk.gov.pay:add_oauth_states_gateway_accounts_fk
ALTER TABLE oauth_states ADD CONSTRAINT oauth_states_gateway_accounts_fk FOREIGN KEY (gateway_account_external_id) REFERENCES gateway_accounts (external_id);
--rollback drop constraint oauth_states_gateway_accounts_fk;