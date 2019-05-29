
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN details_cause VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN details_cause;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN details_description VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN details_description;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN details_origin VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN details_origin;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN details_reason_code VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN details_reason_code;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN details_scheme VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN details_scheme;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_mandate VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_mandate;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_new_customer_bank_account VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_new_customer_bank_account;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_new_mandate VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_new_mandate;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_organisation VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_organisation;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_parent_event VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_parent_event;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_payment VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_payment;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_payout VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_payout;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_previous_customer_bank_account VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_previous_customer_bank_account;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_refund VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_refund;
--changeset uk.gov.pay:alter_table_gocardless_events
ALTER TABLE gocardless_events ADD COLUMN links_subscription VARCHAR(255);
--rollback ALTER TABLE gocardless_events DROP COLUMN links_subscription;

--changeset uk.gov.pay:alter_table_gocardless_events
CREATE INDEX created_at_idx ON gocardless_events(created_at);
--rollback drop index created_at_idx;
--changeset uk.gov.pay:alter_table_gocardless_events
CREATE INDEX links_mandate_idx ON gocardless_events(links_mandate);
--rollback drop index links_mandate_idx;
--changeset uk.gov.pay:alter_table_gocardless_events
CREATE INDEX links_payment_idx ON gocardless_events(links_payment);
--rollback drop index links_payment_idx;
