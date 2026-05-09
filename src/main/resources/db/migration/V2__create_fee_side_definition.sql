CREATE TABLE fee_side_definition (
definition_id UUID PRIMARY KEY,
rule_id UUID NOT NULL,
side VARCHAR(8) NOT NULL,
calculation_mode VARCHAR(20) NOT NULL,
flat_amount DECIMAL(19,4),
percentage DECIMAL(10,6),
min_cap DECIMAL(19,4),
max_cap DECIMAL(19,4),
CONSTRAINT fk_fee_side_definition_rule
FOREIGN KEY (rule_id) REFERENCES fee_rule(rule_id) ON DELETE CASCADE,
CONSTRAINT uk_rule_side UNIQUE (rule_id, side)
);