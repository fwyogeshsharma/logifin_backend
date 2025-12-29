# Database Migration Consolidation

## Date: 2025-12-19

This directory contains the original 30 migration files (V1 through V30) that were consolidated into a single baseline migration.

## What Happened

All 30 individual migration files were merged into:
**`V1__init_complete_schema.sql`**

This consolidated migration file contains:
- All DDL statements (CREATE TABLE, ALTER TABLE, CREATE INDEX) in proper dependency order
- All DML statements (INSERT INTO) for master data
- Complete database schema with all relationships and constraints

## Migration File Backup

The following 30 migration files are preserved here:

1. V1__init.sql - Initial database schema
2. V2__add_audit_tables.sql - Audit logging tables
3. V3__add_user_roles.sql - User roles system
4. V4__add_password_reset_tokens.sql - Password reset functionality
5. V5__add_super_admin_alok.sql - Super admin user (alok@faberwork.com)
6. V6__add_shipper_role.sql - Shipper role addition
7. V7__create_company_table.sql - Company/organization table
8. V8__add_company_id_to_users.sql - Link users to companies
9. V9__rename_logo_to_logo_base64.sql - Logo field update
10. V10__create_company_admins_table.sql - Company admin management
11. V11__create_trips_table.sql - Core trips functionality
12. V12__add_transporter_role.sql - Transporter role
13. V13__create_trip_documents_table.sql - Trip document storage
14. V14__create_document_types_table.sql - Document type master data
15. V15__create_trip_bids_table.sql - Bidding system
16. V16__normalize_trip_documents.sql - Document normalization
17. V17__rename_final_invoice_to_truck_invoice.sql - Invoice renaming
18. V18__rename_truck_invoice_to_final_invoice.sql - Invoice renaming
19. V19__rename_advance_invoice_to_truck_invoice.sql - Invoice renaming
20. V20__create_contract_module_tables.sql - Contract system
21. V21__rename_load_stages_to_loan_stages.sql - Stage renaming
22. V22__create_wallet_system_tables.sql - Digital wallet system
23. V23__insert_default_wallets.sql - Default wallet creation
24. V24__add_actual_transfer_date_to_transactions.sql - Transaction tracking
25. V25__create_configurations_table.sql - System configuration
26. V26__add_contract_trip_relationship.sql - Link contracts to trips
27. V27__add_platform_fee_tracking.sql - Fee tracking
28. V28__insert_jaipur_golden_company.sql - Initial company data
29. V29__update_trips_sender_transporter_to_user_references.sql - Trip refactoring
30. V30__create_super_admin_wallet.sql - Super admin wallet creation

## Database Schema Summary

### Tables Created (22 total):
- **User Management**: users, user_roles, password_reset_tokens
- **Company Management**: companies, company_admins
- **Trips & Logistics**: trips, trip_documents, trip_bids, trip_financials, document_types
- **Contracts**: contracts, contract_parties, contract_types, loan_stages
- **Wallet System**: wallets, transactions, transaction_entries, manual_transfer_requests, transaction_documents
- **System**: audit_log, configurations

### Master Data Inserted:
- **7 User Roles**: LENDER, TRANSPORTER, TRUST_ACCOUNT, CSR, ADMIN, SUPER_ADMIN, SHIPPER
- **3 Users**: admin@logifin.com, test@logifin.com, alok@faberwork.com (all with wallets)
- **1 Company**: Jaipur Golden
- **5 Loan Stages**: PENDING, BILTY_UPLOADED, TRUCK_INVOICE_UPLOADED, POD_UPLOADED, FINAL_INVOICE
- **5 Contract Types**: 1-5 party contracts with Logifin
- **5 Document Types**: E-Way Bill, Bilty, Truck Invoice, POD, Final Invoice
- **3 System Configurations**: portal_service_charge, interest_calculation_method, allow_negative_balance

## Future Migrations

Going forward, all new database changes should be added as V2, V3, V4, etc. migrations.

The consolidated V1 migration serves as the baseline schema for:
- Fresh installations
- New development environments
- Production deployment

## Verification

All migrations were successfully tested:
- ✅ All 22 tables created
- ✅ All foreign key relationships established
- ✅ All indexes created
- ✅ All master data inserted
- ✅ Application starts successfully

## Rollback

If needed, these original migration files can be restored to the `db/migration` directory.
However, ensure you:
1. Drop the current database schema
2. Remove the consolidated V1__init_complete_schema.sql file
3. Copy all 30 files back to the migration directory
4. Run migrations from scratch
