CREATE TABLE tier_bracket(
bracket_id UUID PRIMARY KEY,
definition_id UUID  NOT NULL,
from_amount DECIMAL(19,4) NOT NULL,
to_amount DECIMAL(19,4) NULL,
rate DECIMAL(10,6) NOT NULL,
bracket_order INT NOT NULL,
CONSTRAINT fk_tier_bracket_definition
FOREIGN KEY (definition_id) REFERENCES fee_side_definition(definition_id) ON DELETE CASCADE
);
