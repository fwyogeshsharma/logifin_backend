-- Migration to rename load_stages to loan_stages
-- This migration renames the load_stages table and all related references to loan_stages

-- Step 1: Drop foreign key constraint from contracts table
ALTER TABLE contracts
DROP CONSTRAINT IF EXISTS fk_load_stage;

-- Step 2: Drop indexes on load_stages table
DROP INDEX IF EXISTS idx_load_stage_name;
DROP INDEX IF EXISTS idx_load_stage_order;

-- Step 3: Drop index on contracts table
DROP INDEX IF EXISTS idx_contract_load_stage;

-- Step 4: Rename the load_stages table to loan_stages
ALTER TABLE load_stages
RENAME TO loan_stages;

-- Step 5: Rename the column in contracts table
ALTER TABLE contracts
RENAME COLUMN load_stage_id TO loan_stage_id;

-- Step 6: Recreate indexes on loan_stages table with new names
CREATE INDEX idx_loan_stage_name ON loan_stages(stage_name);
CREATE INDEX idx_loan_stage_order ON loan_stages(stage_order);

-- Step 7: Recreate index on contracts table with new name
CREATE INDEX idx_contract_loan_stage ON contracts(loan_stage_id);

-- Step 8: Recreate foreign key constraint with new name
ALTER TABLE contracts
ADD CONSTRAINT fk_loan_stage
FOREIGN KEY (loan_stage_id)
REFERENCES loan_stages(id)
ON DELETE SET NULL;

-- Step 9: Add comment to document the change
COMMENT ON TABLE loan_stages IS 'Loan stages master data (renamed from load_stages)';
