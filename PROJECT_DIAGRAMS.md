# Logifin Project - Visual Diagrams

> **Note**: These diagrams use Mermaid syntax and render automatically on GitHub, VS Code (with Mermaid extension), and many markdown viewers.

> **Looking for User Interface Flows?** Check out [USER_JOURNEY_DIAGRAMS.md](USER_JOURNEY_DIAGRAMS.md) for detailed user journey diagrams with forms, buttons, and screen layouts!

---

## Table of Contents
1. [System Architecture](#1-system-architecture)
2. [Authentication Flow](#2-authentication-flow)
3. [Trip Financing Flow](#3-trip-financing-flow-main-business-logic)
4. [Database Entity Relationships](#4-database-entity-relationships)
5. [API Request Flow](#5-api-request-flow)
6. [Contract Discovery Process](#6-contract-discovery-process)
7. [Wallet Transaction Flow](#7-wallet-transaction-flow)

---

## 1. System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Frontend/API Client]
    end

    subgraph "Docker Environment"
        subgraph "Application Container"
            API[Spring Boot API<br/>Port: 8080]
            Security[Spring Security<br/>JWT Auth]
            Service[Service Layer<br/>Business Logic]
            Repository[Repository Layer<br/>JPA/Hibernate]
        end

        subgraph "Database Container"
            PostgreSQL[(PostgreSQL<br/>Port: 5432)]
            Flyway[Flyway Migrations<br/>Auto-run on startup]
        end

        subgraph "Management Tools"
            pgAdmin[pgAdmin<br/>Port: 5050]
        end
    end

    Client -->|HTTP/HTTPS| API
    API --> Security
    Security --> Service
    Service --> Repository
    Repository --> PostgreSQL
    Flyway -.->|Manages Schema| PostgreSQL
    pgAdmin -.->|Manage/Monitor| PostgreSQL

    style API fill:#4CAF50
    style PostgreSQL fill:#336791
    style Security fill:#FF9800
    style pgAdmin fill:#2196F3
```

---

## 2. Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Controller
    participant AuthService
    participant JwtUtil
    participant UserRepo
    participant DB

    User->>Controller: POST /api/v1/auth/login
    Note over User,Controller: {email, password}

    Controller->>AuthService: authenticate(credentials)
    AuthService->>UserRepo: findByEmail(email)
    UserRepo->>DB: SELECT * FROM users WHERE email=?
    DB-->>UserRepo: User entity
    UserRepo-->>AuthService: User

    AuthService->>AuthService: Verify password

    alt Password Valid
        AuthService->>JwtUtil: generateToken(user)
        JwtUtil-->>AuthService: JWT Token
        AuthService-->>Controller: LoginResponse + Token
        Controller-->>User: 200 OK + JWT Token
        Note over User: Store token for future requests
    else Password Invalid
        AuthService-->>Controller: BadCredentialsException
        Controller-->>User: 401 Unauthorized
    end

    User->>Controller: GET /api/v1/users<br/>Header: Authorization: Bearer {token}
    Controller->>Security: Validate JWT
    Security->>JwtUtil: validateToken(token)

    alt Token Valid
        JwtUtil-->>Security: Valid + UserDetails
        Security->>Controller: Proceed with request
        Controller->>UserRepo: Fetch data
        Controller-->>User: 200 OK + Data
    else Token Invalid
        JwtUtil-->>Security: Invalid
        Security-->>User: 401 Unauthorized
    end
```

---

## 3. Trip Financing Flow (Main Business Logic)

```mermaid
graph TB
    Start([Start]) --> TransporterCreatesTrip[Transporter Creates Trip<br/>with Pickup, Destination, Amount]

    TransporterCreatesTrip --> TripInDB[(Trip saved in DB<br/>Status: Available)]

    TripInDB --> LenderBrowses[Lender browses available trips]

    LenderBrowses --> LenderSelects[Lender selects MULTIPLE trips<br/>e.g., Trip 101, 102, 103]

    LenderSelects --> BatchAPI[API: POST /lenders/finance-interests<br/>Body: tripIds: 101, 102, 103]

    BatchAPI --> ProcessEachTrip{Process each trip<br/>individually}

    ProcessEachTrip --> FindContract[Auto-find Contract between:<br/>1. Lender ID from Auth<br/>2. Trip's Transporter<br/>3. Trip's Consigner]

    FindContract --> ContractExists{Contract<br/>exists?}

    ContractExists -->|Yes| ContractValid{Contract<br/>ACTIVE &<br/>not expired?}
    ContractExists -->|No| MarkFailed1[Mark trip as FAILED<br/>Error: No contract found]

    ContractValid -->|Yes| CheckDuplicate{Already<br/>marked interest?}
    ContractValid -->|No| MarkFailed2[Mark trip as FAILED<br/>Error: Contract expired]

    CheckDuplicate -->|No| CreateProposal[Create TripFinanceProposal<br/>Status: PENDING]
    CheckDuplicate -->|Yes| MarkFailed3[Mark trip as FAILED<br/>Error: Already interested]

    CreateProposal --> MarkSuccess[Mark trip as SUCCESS<br/>Save proposal ID]

    MarkFailed1 --> NextTrip{More trips?}
    MarkFailed2 --> NextTrip
    MarkFailed3 --> NextTrip
    MarkSuccess --> NextTrip

    NextTrip -->|Yes| ProcessEachTrip
    NextTrip -->|No| ReturnBatchResponse[Return Batch Response<br/>successCount, failureCount<br/>per-trip results]

    ReturnBatchResponse --> TransporterViews[Transporter views trip<br/>sees interested lenders]

    TransporterViews --> TransporterSeesDetails[Sees for each lender:<br/>- Name, Company<br/>- Contract terms<br/>- Interest rate, Maturity days]

    TransporterSeesDetails --> TransporterChooses{Transporter<br/>chooses lender}

    TransporterChooses -->|Accept| AcceptLender[API: POST /{id}/accept]
    TransporterChooses -->|Reject| RejectLender[API: POST /{id}/reject]

    AcceptLender --> UpdateProposal[Update proposal to ACCEPTED]
    UpdateProposal --> LinkTripToContract[Link Trip to Contract<br/>Copy interest rate, maturity days]
    LinkTripToContract --> AutoReject[Auto-REJECT all other<br/>PENDING proposals for this trip]
    AutoReject --> TripFinanced[Trip is now FINANCED<br/>Ready for execution]

    RejectLender --> RejectSingle[Update single proposal to REJECTED<br/>Others remain PENDING]

    TripFinanced --> End([End])
    RejectSingle --> End

    style Start fill:#4CAF50
    style End fill:#4CAF50
    style CreateProposal fill:#2196F3
    style TripFinanced fill:#FF9800
    style MarkFailed1 fill:#f44336
    style MarkFailed2 fill:#f44336
    style MarkFailed3 fill:#f44336
```

---

## 4. Database Entity Relationships

```mermaid
erDiagram
    USER ||--o{ TRIP : creates
    USER ||--o{ TRIP : "is sender/consigner"
    USER ||--o{ CONTRACT_PARTY : "party to"
    USER ||--o{ TRIP_FINANCE_PROPOSAL : "lender in"
    USER }o--|| COMPANY : "belongs to"
    USER }o--o{ ROLE : "has roles"

    COMPANY ||--o{ USER : "has employees"

    CONTRACT ||--o{ CONTRACT_PARTY : "has parties"
    CONTRACT ||--o{ TRIP_FINANCE_PROPOSAL : "used in"
    CONTRACT ||--o{ TRIP : "finances"

    CONTRACT_PARTY }o--|| CONTRACT : "for contract"
    CONTRACT_PARTY }o--|| USER : "is user"

    TRIP ||--o{ TRIP_FINANCE_PROPOSAL : "has proposals"
    TRIP }o--|| CONTRACT : "financed by"
    TRIP }o--|| USER : "created by (transporter)"
    TRIP }o--|| USER : "sender (consigner)"

    TRIP_FINANCE_PROPOSAL }o--|| TRIP : "for trip"
    TRIP_FINANCE_PROPOSAL }o--|| USER : "from lender"
    TRIP_FINANCE_PROPOSAL }o--|| CONTRACT : "uses contract"

    USER {
        bigint id PK
        string firstName
        string lastName
        string email UK
        string password
        boolean active
        bigint company_id FK
        timestamp createdAt
        timestamp updatedAt
    }

    COMPANY {
        bigint id PK
        string name
        string gstin
        string pan
        boolean active
    }

    ROLE {
        bigint id PK
        string roleName UK
        string description
    }

    CONTRACT {
        bigint id PK
        decimal interestRate
        int maturityDays
        decimal loanPercent
        decimal ltv
        decimal penaltyRatio
        date expiryDate
        string status
    }

    CONTRACT_PARTY {
        bigint id PK
        bigint contract_id FK
        bigint user_id FK
        string partyType
        timestamp signedAt
    }

    TRIP {
        bigint id PK
        string pickup
        string destination
        decimal loanAmount
        decimal interestRate
        int maturityDays
        bigint sender_id FK
        bigint created_by_user_id FK
        bigint contract_id FK
    }

    TRIP_FINANCE_PROPOSAL {
        bigint id PK
        bigint trip_id FK
        bigint lender_id FK
        bigint contract_id FK
        string status
        timestamp proposedAt
        timestamp respondedAt
    }
```

---

## 5. API Request Flow

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Security[JWT Filter]
    participant Service
    participant Repository
    participant Database

    Client->>Controller: HTTP Request + JWT Token
    Controller->>Security: Extract & Validate Token

    alt Token Valid
        Security->>Security: Extract User Details
        Security->>Controller: Authentication Object

        Controller->>Controller: Validate Request Body

        alt Validation Passed
            Controller->>Service: Call Business Logic
            Service->>Service: Apply Business Rules
            Service->>Repository: Data Access
            Repository->>Database: SQL Query
            Database-->>Repository: Result Set
            Repository-->>Service: Entity/Entities
            Service->>Service: Map to DTO
            Service-->>Controller: DTO Response
            Controller-->>Client: 200 OK + Data
        else Validation Failed
            Controller-->>Client: 400 Bad Request
        end

    else Token Invalid
        Security-->>Client: 401 Unauthorized
    end
```

---

## 6. Contract Discovery Process

```mermaid
flowchart TD
    Start([Lender marks interest in Trip]) --> GetLenderId[Get Lender ID from JWT Auth]

    GetLenderId --> GetTrip[Fetch Trip from Database]

    GetTrip --> ExtractUsers[Extract from Trip:<br/>- Transporter = trip.createdByUser<br/>- Consigner = trip.sender]

    ExtractUsers --> QueryContracts[Query ContractParty table:<br/>Find contracts where ALL THREE users<br/>are parties]

    QueryContracts --> SQL["SQL: SELECT DISTINCT c FROM Contract c<br/>JOIN ContractParty cp1 (lender)<br/>JOIN ContractParty cp2 (transporter)<br/>JOIN ContractParty cp3 (consigner)<br/>WHERE c.status = 'ACTIVE'<br/>AND c.expiryDate > TODAY<br/>ORDER BY c.expiryDate DESC"]

    SQL --> ContractsFound{Contracts<br/>found?}

    ContractsFound -->|Yes| SelectFirst[Select first contract<br/>Furthest expiry date]
    ContractsFound -->|No| ErrorNoContract[Error: No active contract found<br/>between lender, transporter, consigner]

    SelectFirst --> ValidateContract{Contract<br/>valid?}

    ValidateContract -->|Yes| CreateProposal[Create TripFinanceProposal<br/>with contract_id]
    ValidateContract -->|No| ErrorInvalid[Error: Contract validation failed]

    CreateProposal --> Success([Success: Interest marked])
    ErrorNoContract --> Failure([Failure: Skip this trip])
    ErrorInvalid --> Failure

    style Start fill:#4CAF50
    style Success fill:#4CAF50
    style Failure fill:#f44336
    style CreateProposal fill:#2196F3
    style SQL fill:#FFF9C4
```

---

## 7. Wallet Transaction Flow

```mermaid
stateDiagram-v2
    [*] --> WalletCreated: Admin creates wallet for user

    WalletCreated --> HasBalance: Lender deposits funds

    HasBalance --> PendingTransfer: Manual transfer request<br/>(Admin/Trust Account role)

    PendingTransfer --> BalanceLocked: Transaction begins<br/>(Pessimistic lock on wallet)

    BalanceLocked --> ValidateBalance: Check sufficient balance

    ValidateBalance --> CreateEntries: Create double-entry records<br/>(Debit + Credit)

    CreateEntries --> UpdateWallet: Update wallet version<br/>(Optimistic lock check)

    UpdateWallet --> CommitTransaction: Commit DB transaction

    CommitTransaction --> HasBalance: Transaction successful

    ValidateBalance --> RollbackTransaction: Insufficient balance
    RollbackTransaction --> HasBalance: Transaction failed

    UpdateWallet --> RollbackTransaction: Concurrent modification detected

    HasBalance --> GenerateStatement: User requests statement

    GenerateStatement --> CalculateBalance: Calculate from ledger entries<br/>(No stored balance)

    CalculateBalance --> ReturnStatement: Return transactions + balance

    ReturnStatement --> HasBalance

    note right of CreateEntries
        Double-Entry Bookkeeping:
        - Debit source wallet
        - Credit destination wallet
        - Debits = Credits (balanced)
    end note

    note right of CalculateBalance
        Balance = SUM(credits) - SUM(debits)
        Real-time calculation
        No stored balance field
    end note
```

---

## 8. User Role Access Matrix

```mermaid
graph LR
    subgraph "Roles"
        LENDER[ROLE_LENDER]
        TRANSPORTER[ROLE_TRANSPORTER]
        TRUST[ROLE_TRUST_ACCOUNT]
        CSR[ROLE_CSR]
        ADMIN[ROLE_ADMIN]
        SUPER[ROLE_SUPER_ADMIN]
    end

    subgraph "Finance Operations"
        MarkInterest[Mark Interest in Trips]
        ViewMyInterests[View My Interests]
        AcceptLender[Accept/Reject Lenders]
        ViewTripInterests[View Trip Interests]
    end

    subgraph "Wallet Operations"
        CreateWallet[Create Wallet]
        ManualTransfer[Manual Transfer]
        ViewStatement[View Statement]
    end

    subgraph "User Management"
        ViewUsers[View Users]
        CreateUsers[Create Users]
        UpdateUsers[Update Users]
        DeleteUsers[Delete Users]
    end

    subgraph "System Management"
        ManageRoles[Manage Roles]
        SystemConfig[System Configuration]
    end

    LENDER --> MarkInterest
    LENDER --> ViewMyInterests

    TRANSPORTER --> AcceptLender
    TRANSPORTER --> ViewTripInterests

    TRUST --> ManualTransfer
    TRUST --> ViewStatement
    ADMIN --> CreateWallet
    SUPER --> CreateWallet

    CSR --> ViewUsers
    ADMIN --> ViewUsers
    SUPER --> ViewUsers

    ADMIN --> CreateUsers
    ADMIN --> UpdateUsers
    SUPER --> CreateUsers
    SUPER --> UpdateUsers
    SUPER --> DeleteUsers

    SUPER --> ManageRoles
    SUPER --> SystemConfig

    style LENDER fill:#4CAF50
    style TRANSPORTER fill:#2196F3
    style TRUST fill:#FF9800
    style ADMIN fill:#9C27B0
    style SUPER fill:#f44336
```

---

## 9. Trip Lifecycle States

```mermaid
stateDiagram-v2
    [*] --> Created: Transporter creates trip

    Created --> AwaitingFinancing: Trip available for financing

    AwaitingFinancing --> InterestMarked: Lender(s) mark interest

    InterestMarked --> Financed: Transporter accepts lender

    note right of Financed
        Actions on acceptance:
        1. Link trip to contract
        2. Copy interest rate & maturity
        3. Auto-reject other proposals
    end note

    Financed --> InProgress: Trip execution begins

    InProgress --> Completed: Trip delivered

    Completed --> AwaitingRepayment: Waiting for shipper payment

    AwaitingRepayment --> Repaid: Payment received

    Repaid --> Settled: Lender receives principal + interest

    Settled --> [*]

    AwaitingFinancing --> Cancelled: Cancelled before financing
    InterestMarked --> Cancelled: All lenders rejected

    Cancelled --> [*]

    note left of InterestMarked
        Multiple lenders can mark
        interest simultaneously
        Status: PENDING
    end note
```

---

## 10. Deployment Architecture

```mermaid
graph TB
    subgraph "Development Environment"
        DevMachine[Developer Machine]

        subgraph "Docker Compose"
            AppContainer[logifin-backend<br/>Java 8 + Spring Boot]
            DBContainer[logifin-postgres<br/>PostgreSQL 15]
            AdminContainer[logifin-pgadmin<br/>pgAdmin 4]
        end

        EnvFile[.env file<br/>Configuration]
        DockerCompose[docker-compose.yml]
    end

    subgraph "Version Control"
        Git[Git Repository]
        Migrations[Flyway Migrations<br/>db/migration/]
    end

    subgraph "Production Environment"
        ProdServer[Production Server]

        subgraph "Docker Prod"
            ProdApp[Backend Container]
            ProdDB[(PostgreSQL)]
        end

        ProdEnv[.env.prod<br/>Production Config]
    end

    DevMachine -->|docker compose up --build| DockerCompose
    DockerCompose --> AppContainer
    DockerCompose --> DBContainer
    DockerCompose --> AdminContainer

    EnvFile -.->|Environment Variables| AppContainer
    EnvFile -.->|DB Credentials| DBContainer

    AppContainer -->|Port 8080| DevMachine
    DBContainer -->|Port 5432| AppContainer
    AdminContainer -->|Port 5050| DevMachine

    DevMachine -->|git push| Git
    Git --> Migrations

    Git -->|git pull| ProdServer
    ProdServer -->|docker compose up -d --build| ProdApp
    ProdApp --> ProdDB
    ProdEnv -.->|Production Settings| ProdApp

    Migrations -.->|Auto-applied on startup| AppContainer
    Migrations -.->|Auto-applied on startup| ProdApp

    style AppContainer fill:#4CAF50
    style ProdApp fill:#4CAF50
    style DBContainer fill:#336791
    style ProdDB fill:#336791
    style AdminContainer fill:#2196F3
```

---

## How to View These Diagrams

### Option 1: GitHub (Automatic)
- Push this file to GitHub
- Diagrams render automatically in the web interface

### Option 2: VS Code
- Install "Markdown Preview Mermaid Support" extension
- Open this file and click "Preview" button
- Diagrams render in preview pane

### Option 3: Online Mermaid Editors
- Copy diagram code to: https://mermaid.live/
- Edit and export as PNG/SVG if needed

### Option 4: IntelliJ IDEA / WebStorm
- Built-in Mermaid support in markdown preview
- Just open and view this file

---

## Diagram Syntax Reference

All diagrams use Mermaid syntax:
- `graph TB` = Top to Bottom flowchart
- `sequenceDiagram` = Sequence/timing diagram
- `erDiagram` = Entity Relationship diagram
- `stateDiagram-v2` = State machine diagram
- `flowchart TD` = Modern flowchart syntax

---

**Last Updated**: 2025-12-19
**Version**: 1.0
**Related Files**: PROJECT_CONTEXT.md, README.md
