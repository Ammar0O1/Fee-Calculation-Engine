CREATE TABLE fee_transaction_log(
log_id UUID PRIMARY KEY,
transaction_id VARCHAR(100) NOT NULL,
transaction_amount DECIMAL(19,4) NOT NULL,
source_currency VARCHAR(3) NOT NULL,
destination_currency VARCHAR(3) NOT NULL,
transaction_type VARCHAR(30) NOT NULL,
sender_user_id VARCHAR(100) NOT NULL,
sender_user_type VARCHAR(30) NOT NULL,
sender_matched_rule_id UUID NULL,
sender_raw_fee DECIMAL (19,4),
sender_final_fee DECIMAL (19,4),
sender_fee_currency VARCHAR (3),
sender_cap_applied VARCHAR(20),
sender_waived BOOLEAN,
receiver_user_id VARCHAR(100) NOT NULL,
receiver_user_type VARCHAR(30)NOT NULL,
receiver_matched_rule_id UUID NULL,
receiver_raw_fee DECIMAL(19,4),
receiver_final_fee DECIMAL(19,4),
receiver_fee_currency VARCHAR(3),
receiver_cap_applied VARCHAR(20),
receiver_waived BOOLEAN,
calculated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_fee_transaction_log_transaction_id ON fee_transaction_log(transaction_id);
CREATE INDEX idx_fee_transaction_log_sender_user_id ON fee_transaction_log(sender_user_id);
CREATE INDEX idx_fee_transaction_log_calculated_at ON fee_transaction_log(calculated_at);