# Logifin User Journey - Visual Flow Diagrams

> **User Experience Flows**: Visual representation of how different users interact with the Logifin platform, showing screens, forms, and actions.

---

## Table of Contents
1. [Lender Complete Journey](#1-lender-complete-journey)
2. [Transporter Complete Journey](#2-transporter-complete-journey)
3. [User Login Flow](#3-user-login-flow)
4. [Trip Creation Flow](#4-trip-creation-flow)
5. [Lender - Browse & Finance Trips](#5-lender---browse--finance-trips)
6. [Transporter - Accept Lender](#6-transporter---accept-lender)
7. [Wallet Operations](#7-wallet-operations)
8. [Contract Management](#8-contract-management)

---

## 1. Lender Complete Journey

```mermaid
graph TB
    Start([👤 Lender User]) --> Login[🔐 Login Screen<br/>Email: lender@example.com<br/>Password: ****<br/>━━━━━━━━━<br/>🔘 Login Button]

    Login -->|Success| Dashboard[📊 Lender Dashboard<br/>━━━━━━━━━<br/>💰 My Wallet Balance: ₹50,000<br/>📈 Active Financing: 3<br/>⏳ Pending Interests: 5<br/>━━━━━━━━━<br/>🔘 Browse Trips<br/>🔘 My Interests<br/>🔘 My Wallet]

    Dashboard -->|Click Browse| TripList[📋 Available Trips List<br/>━━━━━━━━━<br/>☑️ Trip #101: Mumbai → Delhi<br/>   Amount: ₹10,000 | 500 km<br/>   Transporter: ABC Transport<br/><br/>☑️ Trip #102: Pune → Bangalore<br/>   Amount: ₹15,000 | 850 km<br/>   Transporter: XYZ Logistics<br/><br/>☑️ Trip #103: Chennai → Hyderabad<br/>   Amount: ₹8,000 | 630 km<br/>   Transporter: DEF Movers<br/>━━━━━━━━━<br/>Selected: 2 trips<br/>🔘 Mark Interest Button]

    TripList -->|Select multiple & Click| ConfirmInterest[✅ Confirm Interest<br/>━━━━━━━━━<br/>Selected Trips:<br/>• Trip #101 - ₹10,000<br/>• Trip #102 - ₹15,000<br/><br/>System will auto-find contracts<br/>with transporters & consigners<br/>━━━━━━━━━<br/>🔘 Confirm<br/>🔘 Cancel]

    ConfirmInterest -->|Confirm| Processing[⚙️ Processing...<br/>Finding contracts for each trip]

    Processing --> Result[📊 Results Screen<br/>━━━━━━━━━<br/>✅ Trip #101: SUCCESS<br/>   Contract ID: 45<br/>   Interest: 12.5% | 30 days<br/><br/>✅ Trip #102: SUCCESS<br/>   Contract ID: 45<br/>   Interest: 12.5% | 30 days<br/>━━━━━━━━━<br/>2 Successful | 0 Failed<br/>🔘 View My Interests<br/>🔘 Browse More]

    Result -->|Wait for transporter| Notification[🔔 Notification<br/>━━━━━━━━━<br/>✅ Transporter ACCEPTED<br/>your interest in Trip #101!<br/><br/>Contract activated<br/>Financing active<br/>━━━━━━━━━<br/>🔘 View Details]

    Result -->|Or| Rejected[🔔 Notification<br/>━━━━━━━━━<br/>❌ Transporter REJECTED<br/>your interest in Trip #102<br/><br/>Other lender selected<br/>━━━━━━━━━<br/>🔘 OK]

    Notification --> ViewDetails[📄 Trip Finance Details<br/>━━━━━━━━━<br/>Trip: #101 Mumbai → Delhi<br/>Status: ✅ ACCEPTED<br/><br/>Transporter: ABC Transport<br/>Principal: ₹10,000<br/>Interest Rate: 12.5%<br/>Maturity: 30 days<br/>Expected Return: ₹10,416<br/>━━━━━━━━━<br/>🔘 Track Trip<br/>🔘 Back to Dashboard]

    Dashboard -->|Click My Interests| InterestList[📊 My Interests<br/>━━━━━━━━━<br/>Filter: [All ▼] Status<br/><br/>⏳ PENDING (3)<br/>• Trip #105 - ₹12,000<br/>• Trip #108 - ₹20,000<br/>• Trip #110 - ₹7,500<br/><br/>✅ ACCEPTED (2)<br/>• Trip #101 - ₹10,000<br/>• Trip #107 - ₹15,000<br/><br/>❌ REJECTED (1)<br/>• Trip #102 - ₹15,000<br/>━━━━━━━━━<br/>🔘 Withdraw Interest]

    style Start fill:#4CAF50,color:#fff
    style Login fill:#2196F3,color:#fff
    style Dashboard fill:#FF9800,color:#fff
    style Notification fill:#4CAF50,color:#fff
    style Rejected fill:#f44336,color:#fff
```

---

## 2. Transporter Complete Journey

```mermaid
graph TB
    Start([👤 Transporter User]) --> Login[🔐 Login Screen<br/>Email: transporter@example.com<br/>Password: ****<br/>━━━━━━━━━<br/>🔘 Login Button]

    Login -->|Success| Dashboard[📊 Transporter Dashboard<br/>━━━━━━━━━<br/>🚛 Active Trips: 5<br/>💰 Pending Financing: 3<br/>🔔 New Interests: 7<br/>━━━━━━━━━<br/>🔘 Create Trip<br/>🔘 My Trips<br/>🔘 Finance Requests]

    Dashboard -->|Click Create| TripForm[📝 Create New Trip Form<br/>━━━━━━━━━<br/>📍 Pickup Location:<br/>[Mumbai, Maharashtra____]<br/><br/>📍 Destination:<br/>[Delhi, NCR___________]<br/><br/>👤 Consigner/Sender:<br/>[Select Consigner ▼]<br/>Selected: ABC Company<br/><br/>💰 Estimated Amount:<br/>[₹ 15,000___________]<br/><br/>📦 Cargo Details:<br/>[Electronics_________]<br/><br/>📏 Distance: 1,400 km<br/>━━━━━━━━━<br/>🔘 Create Trip<br/>🔘 Cancel]

    TripForm -->|Submit| TripCreated[✅ Trip Created<br/>━━━━━━━━━<br/>Trip #150<br/>Mumbai → Delhi<br/><br/>Amount: ₹15,000<br/>Status: Awaiting Finance<br/><br/>Visible to lenders<br/>━━━━━━━━━<br/>🔘 View Trip<br/>🔘 Create Another]

    TripCreated -->|Wait| LenderInterest[🔔 Notification<br/>━━━━━━━━━<br/>3 Lenders interested<br/>in Trip #150!<br/><br/>View their offers<br/>━━━━━━━━━<br/>🔘 View Lenders]

    Dashboard -->|Click Finance Requests| InterestList[📊 Trip #150 - Lender Interests<br/>━━━━━━━━━<br/>3 Lenders interested:<br/><br/>┌─────────────────────┐<br/>│ 👤 Lender: XYZ Finance     │<br/>│ 🏢 Company: XYZ Capital    │<br/>│ 💰 Interest: 11.0%         │<br/>│ 📅 Maturity: 45 days       │<br/>│ 📋 Contract: #50           │<br/>│ 🔘 Accept | 🔘 Reject      │<br/>└─────────────────────┘<br/><br/>┌─────────────────────┐<br/>│ 👤 Lender: ABC Finance     │<br/>│ 🏢 Company: ABC Ltd        │<br/>│ 💰 Interest: 12.5%         │<br/>│ 📅 Maturity: 30 days       │<br/>│ 📋 Contract: #45           │<br/>│ 🔘 Accept | 🔘 Reject      │<br/>└─────────────────────┘<br/><br/>┌─────────────────────┐<br/>│ 👤 Lender: PQR Finance     │<br/>│ 🏢 Company: PQR Group      │<br/>│ 💰 Interest: 13.0%         │<br/>│ 📅 Maturity: 30 days       │<br/>│ 📋 Contract: #52           │<br/>│ 🔘 Accept | 🔘 Reject      │<br/>└─────────────────────┘]

    InterestList -->|Click Accept| ConfirmAccept[⚠️ Confirm Selection<br/>━━━━━━━━━<br/>Accept lender?<br/><br/>👤 XYZ Finance<br/>💰 Interest: 11.0%<br/>📅 Maturity: 45 days<br/><br/>This will:<br/>• Link trip to Contract #50<br/>• Auto-reject other lenders<br/>• Activate financing<br/>━━━━━━━━━<br/>🔘 Confirm Accept<br/>🔘 Cancel]

    ConfirmAccept -->|Confirm| Accepted[✅ Lender Accepted!<br/>━━━━━━━━━<br/>Trip #150 financed by<br/>XYZ Finance<br/><br/>Contract: #50<br/>Interest: 11.0%<br/>Maturity: 45 days<br/><br/>Other lenders notified<br/>━━━━━━━━━<br/>🔘 View Trip Details<br/>🔘 Back to Dashboard]

    Accepted --> TripDetails[📄 Trip Details<br/>━━━━━━━━━<br/>Trip #150<br/>Mumbai → Delhi<br/><br/>Status: ✅ FINANCED<br/><br/>💰 Amount: ₹15,000<br/>📊 Interest: 11.0%<br/>📅 Maturity: 45 days<br/>🏦 Lender: XYZ Finance<br/>📋 Contract: #50<br/><br/>━━━━━━━━━<br/>🔘 Start Trip<br/>🔘 Back]

    style Start fill:#2196F3,color:#fff
    style Login fill:#2196F3,color:#fff
    style Dashboard fill:#FF9800,color:#fff
    style TripCreated fill:#4CAF50,color:#fff
    style Accepted fill:#4CAF50,color:#fff
```

---

## 3. User Login Flow

```mermaid
graph LR
    User([👤 User]) --> LoginPage[🔐 Login Page<br/>━━━━━━━━━<br/>📧 Email:<br/>[________________]<br/><br/>🔒 Password:<br/>[****************]<br/><br/>━━━━━━━━━<br/>🔘 Login<br/>🔗 Register<br/>🔗 Forgot Password]

    LoginPage -->|Enter Credentials| Validate{Validating...}

    Validate -->|✅ Success| Token[🎫 JWT Token Generated<br/>Stored in browser]

    Token --> RoleCheck{Check User Role}

    RoleCheck -->|LENDER| LenderDash[📊 Lender Dashboard<br/>━━━━━━━━━<br/>💰 Wallet: ₹50,000<br/>📈 Active: 3<br/>⏳ Pending: 5<br/>━━━━━━━━━<br/>🔘 Browse Trips<br/>🔘 My Interests<br/>🔘 My Wallet<br/>🔘 Logout]

    RoleCheck -->|TRANSPORTER| TransDash[📊 Transporter Dashboard<br/>━━━━━━━━━<br/>🚛 Trips: 5<br/>💰 Financing: 3<br/>🔔 Interests: 7<br/>━━━━━━━━━<br/>🔘 Create Trip<br/>🔘 My Trips<br/>🔘 Finance Requests<br/>🔘 Logout]

    RoleCheck -->|ADMIN| AdminDash[📊 Admin Dashboard<br/>━━━━━━━━━<br/>👥 Users: 150<br/>📋 Contracts: 45<br/>💳 Wallets: 89<br/>━━━━━━━━━<br/>🔘 Manage Users<br/>🔘 Manage Contracts<br/>🔘 Manage Wallets<br/>🔘 Logout]

    Validate -->|❌ Failed| Error[❌ Error Message<br/>━━━━━━━━━<br/>Invalid credentials<br/>Please try again<br/>━━━━━━━━━<br/>🔘 Try Again]

    Error --> LoginPage

    style User fill:#4CAF50,color:#fff
    style Token fill:#4CAF50,color:#fff
    style LenderDash fill:#FF9800,color:#fff
    style TransDash fill:#2196F3,color:#fff
    style AdminDash fill:#9C27B0,color:#fff
    style Error fill:#f44336,color:#fff
```

---

## 4. Trip Creation Flow

```mermaid
graph TB
    Start([🚛 Transporter]) --> ClickCreate[Click 'Create Trip' Button]

    ClickCreate --> Form1[📝 Trip Form - Step 1<br/>━━━━━━━━━<br/>📍 Route Information<br/><br/>From (Pickup):<br/>[Select City ▼____]<br/>→ Mumbai, Maharashtra<br/><br/>To (Destination):<br/>[Select City ▼____]<br/>→ Delhi, NCR<br/><br/>📏 Distance: Auto-calculated<br/>1,400 km<br/>━━━━━━━━━<br/>🔘 Next Step]

    Form1 --> Form2[📝 Trip Form - Step 2<br/>━━━━━━━━━<br/>👤 Parties & Amount<br/><br/>Consigner/Sender:<br/>[Search Company ▼]<br/>→ ABC Pvt Ltd<br/><br/>💰 Loan Amount Needed:<br/>[₹ 15,000_______]<br/><br/>Estimated cargo value:<br/>[₹ 50,000_______]<br/>━━━━━━━━━<br/>🔘 Next Step<br/>🔘 Previous]

    Form2 --> Form3[📝 Trip Form - Step 3<br/>━━━━━━━━━<br/>📦 Cargo Details<br/><br/>Cargo Type:<br/>[Electronics_____]<br/><br/>Weight:<br/>[500 kg_________]<br/><br/>📅 Expected Start:<br/>[2025-12-25_____]<br/><br/>📅 Expected Delivery:<br/>[2025-12-27_____]<br/>━━━━━━━━━<br/>🔘 Review<br/>🔘 Previous]

    Form3 --> Review[📋 Review Trip Details<br/>━━━━━━━━━<br/>📍 Route:<br/>Mumbai → Delhi (1,400 km)<br/><br/>👤 Consigner: ABC Pvt Ltd<br/>💰 Amount: ₹15,000<br/>📦 Cargo: Electronics (500 kg)<br/>📅 Start: 2025-12-25<br/>📅 Delivery: 2025-12-27<br/><br/>━━━━━━━━━<br/>🔘 Create Trip<br/>🔘 Edit<br/>🔘 Cancel]

    Review -->|Click Create| Saving[💾 Saving Trip...]

    Saving --> Success[✅ Trip Created Successfully!<br/>━━━━━━━━━<br/>Trip #150<br/>Mumbai → Delhi<br/><br/>Status: Awaiting Financing<br/><br/>Your trip is now visible<br/>to lenders<br/>━━━━━━━━━<br/>🔘 View Trip<br/>🔘 Create Another<br/>🔘 Dashboard]

    Success --> Visible[👀 Trip Now Visible to Lenders<br/>━━━━━━━━━<br/>Lenders can see:<br/>• Route & Distance<br/>• Amount needed<br/>• Your company details<br/>• Cargo information<br/><br/>Waiting for lender interest...]

    style Start fill:#2196F3,color:#fff
    style Success fill:#4CAF50,color:#fff
    style Visible fill:#FF9800,color:#fff
```

---

## 5. Lender - Browse & Finance Trips

```mermaid
graph TB
    Start([💰 Lender Dashboard]) --> Browse[Click 'Browse Trips']

    Browse --> Filters[🔍 Trip Search & Filters<br/>━━━━━━━━━<br/>📍 Route:<br/>[Any ▼] → [Any ▼]<br/><br/>💰 Amount Range:<br/>Min: [₹ 5,000__]<br/>Max: [₹ 50,000_]<br/><br/>📅 Date Range:<br/>[2025-12-20 to 2025-12-31]<br/><br/>🏢 Transporter:<br/>[All Companies ▼]<br/>━━━━━━━━━<br/>🔘 Apply Filters<br/>🔘 Clear]

    Filters --> TripList[📋 Available Trips List<br/>━━━━━━━━━<br/>Showing 15 trips<br/><br/>┌───────────────────┐<br/>│ ☐ Trip #101              │<br/>│ 📍 Mumbai → Delhi        │<br/>│ 💰 ₹10,000 | 📏 1,400 km │<br/>│ 🚛 ABC Transport         │<br/>│ 📅 Start: 2025-12-25     │<br/>│ ℹ️ View Details          │<br/>└───────────────────┘<br/><br/>┌───────────────────┐<br/>│ ☐ Trip #102              │<br/>│ 📍 Pune → Bangalore      │<br/>│ 💰 ₹15,000 | 📏 850 km   │<br/>│ 🚛 XYZ Logistics         │<br/>│ 📅 Start: 2025-12-26     │<br/>│ ℹ️ View Details          │<br/>└───────────────────┘<br/><br/>┌───────────────────┐<br/>│ ☑️ Trip #103             │<br/>│ 📍 Chennai → Hyderabad   │<br/>│ 💰 ₹8,000 | 📏 630 km    │<br/>│ 🚛 DEF Movers            │<br/>│ 📅 Start: 2025-12-27     │<br/>│ ℹ️ View Details          │<br/>└───────────────────┘<br/>━━━━━━━━━<br/>Selected: 1 trip<br/>🔘 Mark Interest in Selected]

    TripList -->|Select multiple trips| SelectMultiple[✅ Multiple Selection<br/>━━━━━━━━━<br/>You've selected:<br/><br/>☑️ Trip #101 - ₹10,000<br/>☑️ Trip #103 - ₹8,000<br/>☑️ Trip #105 - ₹12,000<br/><br/>Total: 3 trips<br/>━━━━━━━━━<br/>🔘 Mark Interest<br/>🔘 Clear Selection]

    SelectMultiple -->|Click Mark Interest| ContractCheck[⚙️ Processing Request<br/>━━━━━━━━━<br/>Checking contracts...<br/><br/>For each trip, finding<br/>active contract between:<br/>• You (Lender)<br/>• Trip's Transporter<br/>• Trip's Consigner<br/>━━━━━━━━━<br/>Please wait...]

    ContractCheck --> Results[📊 Batch Results<br/>━━━━━━━━━<br/>Processing: 3 trips<br/><br/>✅ Trip #101: SUCCESS<br/>   Contract #45 found<br/>   Interest: 12.5% | 30 days<br/><br/>✅ Trip #103: SUCCESS<br/>   Contract #47 found<br/>   Interest: 11.0% | 45 days<br/><br/>❌ Trip #105: FAILED<br/>   No contract with<br/>   transporter & consigner<br/>━━━━━━━━━<br/>Success: 2 | Failed: 1<br/>🔘 View My Interests<br/>🔘 Browse More]

    Results --> MyInterests[📊 My Interests Dashboard<br/>━━━━━━━━━<br/>Filter: [Pending ▼]<br/><br/>⏳ PENDING (5 trips)<br/>━━━━━━━━━<br/>Trip #101<br/>Mumbai → Delhi<br/>💰 ₹10,000 | 📊 12.5%<br/>Status: Waiting for transporter<br/>🔘 Withdraw Interest<br/>━━━━━━━━━<br/>Trip #103<br/>Chennai → Hyderabad<br/>💰 ₹8,000 | 📊 11.0%<br/>Status: Waiting for transporter<br/>🔘 Withdraw Interest<br/>━━━━━━━━━]

    style Start fill:#4CAF50,color:#fff
    style Results fill:#FF9800,color:#fff
    style MyInterests fill:#2196F3,color:#fff
```

---

## 6. Transporter - Accept Lender

```mermaid
graph TB
    Start([🔔 Notification Received]) --> ViewNotif[📱 Notification<br/>━━━━━━━━━<br/>3 Lenders interested<br/>in Trip #150!<br/>━━━━━━━━━<br/>🔘 View Lenders]

    ViewNotif --> LenderList[📊 Lender Comparison View<br/>━━━━━━━━━<br/>Trip #150: Mumbai → Delhi<br/>Amount Needed: ₹15,000<br/>━━━━━━━━━<br/><br/>Compare Lenders:<br/><br/>┏━━━━━━━━━━━━━━━━━━━┓<br/>┃ 🏆 BEST OFFER          ┃<br/>┃ 👤 XYZ Finance         ┃<br/>┃ 💰 Interest: 11.0%     ┃<br/>┃ 📅 Maturity: 45 days   ┃<br/>┃ 💵 Return: ₹15,550     ┃<br/>┃ 🔘 Accept This         ┃<br/>┗━━━━━━━━━━━━━━━━━━━┛<br/><br/>┌─────────────────────┐<br/>│ 👤 ABC Finance         │<br/>│ 💰 Interest: 12.5%     │<br/>│ 📅 Maturity: 30 days   │<br/>│ 💵 Return: ₹15,625     │<br/>│ 🔘 Accept              │<br/>└─────────────────────┘<br/><br/>┌─────────────────────┐<br/>│ 👤 PQR Finance         │<br/>│ 💰 Interest: 13.0%     │<br/>│ 📅 Maturity: 30 days   │<br/>│ 💵 Return: ₹15,650     │<br/>│ 🔘 Accept              │<br/>└─────────────────────┘]

    LenderList -->|Click on XYZ| ViewDetails[📄 Lender Full Details<br/>━━━━━━━━━<br/>👤 Lender Information<br/>Name: XYZ Finance<br/>Company: XYZ Capital Ltd<br/>Rating: ⭐⭐⭐⭐⭐<br/><br/>📋 Contract Details<br/>Contract ID: #50<br/>Interest Rate: 11.0%<br/>Maturity Period: 45 days<br/>LTV Ratio: 75%<br/>Loan Percentage: 80%<br/>Penalty Ratio: 2.0%<br/><br/>💰 Financial Breakdown<br/>Principal: ₹15,000<br/>Interest (11%): ₹550<br/>Total Repayment: ₹15,550<br/>━━━━━━━━━<br/>🔘 Accept Lender<br/>🔘 Reject<br/>🔘 Back]

    ViewDetails -->|Click Accept| ConfirmDialog[⚠️ Confirmation Dialog<br/>━━━━━━━━━<br/>Accept XYZ Finance?<br/><br/>This action will:<br/><br/>✅ Link Trip #150 to Contract #50<br/>✅ Set interest rate to 11.0%<br/>✅ Set maturity to 45 days<br/>❌ Auto-reject 2 other lenders<br/>🔒 Cannot be undone<br/>━━━━━━━━━<br/>Are you sure?<br/>🔘 Yes, Accept<br/>🔘 No, Cancel]

    ConfirmDialog -->|Yes| Processing[⚙️ Processing...<br/>━━━━━━━━━<br/>✓ Accepting lender<br/>✓ Linking contract<br/>✓ Updating trip<br/>✓ Rejecting others<br/>✓ Sending notifications]

    Processing --> Success[✅ Lender Accepted!<br/>━━━━━━━━━<br/>🎉 Success!<br/><br/>Trip #150 is now financed<br/>by XYZ Finance<br/><br/>📋 Contract: #50<br/>💰 Amount: ₹15,000<br/>📊 Interest: 11.0%<br/>📅 Maturity: 45 days<br/><br/>Notifications sent to:<br/>✓ XYZ Finance (accepted)<br/>✓ ABC Finance (rejected)<br/>✓ PQR Finance (rejected)<br/>━━━━━━━━━<br/>🔘 View Trip<br/>🔘 Start Trip<br/>🔘 Dashboard]

    Success --> TripActive[🚛 Trip #150 - FINANCED<br/>━━━━━━━━━<br/>Status: Ready to Start<br/><br/>Route: Mumbai → Delhi<br/>💰 Financed: ₹15,000<br/>🏦 Lender: XYZ Finance<br/>📊 Interest: 11.0%<br/>📅 Maturity: 45 days<br/>━━━━━━━━━<br/>Next Steps:<br/>🔘 Start Trip<br/>🔘 Upload Documents<br/>🔘 Track Progress]

    style Start fill:#FF9800,color:#fff
    style ViewDetails fill:#2196F3,color:#fff
    style Success fill:#4CAF50,color:#fff
    style TripActive fill:#4CAF50,color:#fff
```

---

## 7. Wallet Operations

```mermaid
graph TB
    Start([👤 User]) --> WalletDash[💳 My Wallet Dashboard<br/>━━━━━━━━━<br/>💰 Current Balance:<br/>₹ 50,000.00<br/><br/>Recent Transactions:<br/>━━━━━━━━━<br/>🔘 View Statement<br/>🔘 Request Transfer<br/>🔘 Transaction History]

    WalletDash -->|Admin/Trust Account| TransferReq[💸 Manual Transfer Request<br/>━━━━━━━━━<br/>From Wallet:<br/>[My Wallet ▼]<br/>Balance: ₹50,000<br/><br/>To Wallet:<br/>[Search User ▼____]<br/>→ Transporter ABC<br/><br/>💰 Amount:<br/>[₹ 10,000________]<br/><br/>📝 Purpose:<br/>[Trip #150 disbursement]<br/><br/>📎 Attach Proof:<br/>[📁 Choose File____]<br/>━━━━━━━━━<br/>🔘 Submit Request<br/>🔘 Cancel]

    TransferReq -->|Submit| ConfirmTransfer[⚠️ Confirm Transfer<br/>━━━━━━━━━<br/>Transfer Details:<br/><br/>From: My Wallet<br/>To: Transporter ABC<br/>Amount: ₹10,000<br/><br/>Your new balance:<br/>₹40,000<br/><br/>This action is final<br/>━━━━━━━━━<br/>🔘 Confirm Transfer<br/>🔘 Cancel]

    ConfirmTransfer -->|Confirm| Processing[⚙️ Processing Transfer...<br/>━━━━━━━━━<br/>✓ Validating balance<br/>✓ Locking wallets<br/>✓ Creating entries<br/>✓ Updating ledger<br/>✓ Uploading proof]

    Processing --> TransferSuccess[✅ Transfer Successful!<br/>━━━━━━━━━<br/>₹10,000 transferred<br/>to Transporter ABC<br/><br/>Transaction ID: #TXN12345<br/><br/>💰 New Balance: ₹40,000<br/>━━━━━━━━━<br/>🔘 View Receipt<br/>🔘 Back to Wallet]

    WalletDash -->|Click Statement| StatementView[📊 Wallet Statement<br/>━━━━━━━━━<br/>Period: [Last 30 Days ▼]<br/><br/>Opening Balance: ₹25,000<br/>━━━━━━━━━<br/>Date       | Description       | Debit    | Credit   | Balance<br/>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━<br/>Dec 15 | Deposit          | -        | ₹30,000  | ₹55,000<br/>Dec 16 | Transfer Out     | ₹5,000   | -        | ₹50,000<br/>Dec 19 | Transfer Out     | ₹10,000  | -        | ₹40,000<br/>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━<br/>Total Credits: ₹30,000<br/>Total Debits: ₹15,000<br/>Closing Balance: ₹40,000<br/>━━━━━━━━━<br/>🔘 Download PDF<br/>🔘 Print<br/>🔘 Back]

    StatementView -->|Click Transaction| TxnDetails[📄 Transaction Details<br/>━━━━━━━━━<br/>Transaction #TXN12345<br/><br/>Date: 2025-12-19 14:30:00<br/>Type: Manual Transfer<br/><br/>From: My Wallet<br/>To: Transporter ABC<br/>Amount: ₹10,000<br/><br/>Status: ✅ Completed<br/><br/>Purpose:<br/>Trip #150 disbursement<br/><br/>Proof Document:<br/>📎 invoice.pdf<br/>🔘 View Document<br/>━━━━━━━━━<br/>🔘 Back]

    style Start fill:#4CAF50,color:#fff
    style TransferSuccess fill:#4CAF50,color:#fff
    style WalletDash fill:#FF9800,color:#fff
```

---

## 8. Contract Management

```mermaid
graph TB
    Start([👤 Admin User]) --> ContractDash[📋 Contract Management<br/>━━━━━━━━━<br/>Total Contracts: 45<br/>Active: 38 | Expired: 7<br/>━━━━━━━━━<br/>🔘 Create Contract<br/>🔘 View All Contracts<br/>🔘 Search Contracts]

    ContractDash -->|Click Create| Form1[📝 New Contract - Step 1<br/>━━━━━━━━━<br/>👥 Contract Parties<br/><br/>Lender:<br/>[Search Lender ▼___]<br/>→ XYZ Finance<br/><br/>Transporter:<br/>[Search Transporter ▼]<br/>→ ABC Transport<br/><br/>Consigner:<br/>[Search Consigner ▼_]<br/>→ DEF Company<br/>━━━━━━━━━<br/>🔘 Next Step]

    Form1 --> Form2[📝 New Contract - Step 2<br/>━━━━━━━━━<br/>📊 Financial Terms<br/><br/>💰 Interest Rate (%):<br/>[11.5____________]<br/><br/>📅 Maturity Days:<br/>[45_____________]<br/><br/>📈 LTV Ratio (%):<br/>[75_____________]<br/><br/>💵 Loan Percentage (%):<br/>[80_____________]<br/><br/>⚠️ Penalty Ratio:<br/>[2.0____________]<br/>━━━━━━━━━<br/>🔘 Next Step<br/>🔘 Previous]

    Form2 --> Form3[📝 New Contract - Step 3<br/>━━━━━━━━━<br/>📅 Validity<br/><br/>Start Date:<br/>[2025-12-20_____]<br/><br/>Expiry Date:<br/>[2026-12-20_____]<br/><br/>Valid for: 365 days<br/>━━━━━━━━━<br/>Status:<br/>[Active ▼______]<br/>━━━━━━━━━<br/>🔘 Review<br/>🔘 Previous]

    Form3 --> Review[📋 Review Contract<br/>━━━━━━━━━<br/>Parties:<br/>👤 Lender: XYZ Finance<br/>🚛 Transporter: ABC Transport<br/>🏢 Consigner: DEF Company<br/><br/>Financial Terms:<br/>💰 Interest: 11.5%<br/>📅 Maturity: 45 days<br/>📈 LTV: 75%<br/>💵 Loan: 80%<br/>⚠️ Penalty: 2.0x<br/><br/>Validity:<br/>📅 2025-12-20 to 2026-12-20<br/>━━━━━━━━━<br/>🔘 Create Contract<br/>🔘 Edit<br/>🔘 Cancel]

    Review -->|Create| Success[✅ Contract Created!<br/>━━━━━━━━━<br/>Contract ID: #55<br/>Status: Active<br/><br/>All 3 parties notified<br/><br/>Contract can now be used<br/>for trip financing<br/>━━━━━━━━━<br/>🔘 View Contract<br/>🔘 Create Another<br/>🔘 Dashboard]

    ContractDash -->|View All| ContractList[📊 All Contracts<br/>━━━━━━━━━<br/>Filter: [Active ▼]<br/><br/>┌─────────────────────┐<br/>│ 📋 Contract #55          │<br/>│ Status: ✅ Active        │<br/>│ 👤 XYZ Finance           │<br/>│ 🚛 ABC Transport         │<br/>│ 🏢 DEF Company           │<br/>│ 💰 11.5% | 📅 45 days   │<br/>│ ⏰ Expires: 2026-12-20  │<br/>│ 🔘 View Details          │<br/>└─────────────────────┘<br/><br/>┌─────────────────────┐<br/>│ 📋 Contract #54          │<br/>│ Status: ✅ Active        │<br/>│ 👤 ABC Finance           │<br/>│ 🚛 XYZ Logistics         │<br/>│ 🏢 PQR Industries        │<br/>│ 💰 12.5% | 📅 30 days   │<br/>│ ⏰ Expires: 2026-06-30  │<br/>│ 🔘 View Details          │<br/>└─────────────────────┘]

    ContractList -->|Click View| ContractDetails[📄 Contract Details<br/>━━━━━━━━━<br/>Contract #55<br/>Status: ✅ Active<br/><br/>━━━━━━━━━<br/>Parties:<br/>👤 Lender: XYZ Finance<br/>   Email: xyz@finance.com<br/>   Phone: +91-9876543210<br/><br/>🚛 Transporter: ABC Transport<br/>   Email: abc@transport.com<br/>   Phone: +91-9876543211<br/><br/>🏢 Consigner: DEF Company<br/>   Email: def@company.com<br/>   Phone: +91-9876543212<br/>━━━━━━━━━<br/>Financial Terms:<br/>💰 Interest Rate: 11.5%<br/>📅 Maturity: 45 days<br/>📈 LTV: 75%<br/>💵 Loan %: 80%<br/>⚠️ Penalty: 2.0x<br/>━━━━━━━━━<br/>Validity:<br/>Start: 2025-12-20<br/>Expiry: 2026-12-20<br/>━━━━━━━━━<br/>Usage:<br/>🚛 Used in 12 trips<br/>💰 Total financed: ₹1,80,000<br/>━━━━━━━━━<br/>🔘 Edit Contract<br/>🔘 Deactivate<br/>🔘 Back]

    style Start fill:#9C27B0,color:#fff
    style Success fill:#4CAF50,color:#fff
    style ContractDash fill:#FF9800,color:#fff
```

---

## Visual Legend

### Icons Used
- 👤 User/Person
- 🔐 Login/Security
- 📊 Dashboard
- 💰 Money/Finance
- 🚛 Truck/Transport
- 📍 Location/GPS
- 📋 List/Document
- ✅ Success/Confirmed
- ❌ Error/Rejected
- ⏳ Pending/Waiting
- 🔔 Notification
- 📝 Form/Input
- 💳 Wallet/Payment
- 📄 Details/Document
- 🔍 Search/Filter
- ⚙️ Processing
- 📱 Mobile/Alert
- 🏢 Company/Business
- 📅 Calendar/Date
- 📈 Growth/Analytics
- 🔘 Button/Action
- ☑️ Checkbox Selected
- ☐ Checkbox Unselected

### Status Colors
- 🟢 Green - Success, Active, Approved
- 🔵 Blue - Information, Pending
- 🟠 Orange - Warning, In Progress
- 🔴 Red - Error, Rejected, Critical
- 🟣 Purple - Admin, System

---

## How to Use These Diagrams

### For New Team Members
Start with diagrams in this order:
1. **User Login Flow** - Understand authentication
2. **Lender Complete Journey** - See lender perspective
3. **Transporter Complete Journey** - See transporter perspective
4. **Trip Creation Flow** - Understand trip creation
5. **Lender - Browse & Finance** - Understand financing flow

### For UI/UX Design
These diagrams show:
- Screen layouts and forms
- Button placements
- Navigation flows
- User interactions
- Data displayed on each screen

### For Developers
Use these to understand:
- User workflows
- Form validation requirements
- Screen transitions
- Data needed on each page
- Success/error states

### For Business Stakeholders
Focus on:
- User journey completeness
- Business logic flow
- User decision points
- System responses

---

**Last Updated**: 2025-12-19
**Version**: 1.0
**Related Files**: PROJECT_CONTEXT.md, PROJECT_DIAGRAMS.md
