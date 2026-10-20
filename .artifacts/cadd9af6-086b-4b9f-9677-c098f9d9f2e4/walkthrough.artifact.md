# Dual-Role System (Player vs. Turf Owner) - Walkthrough & Deliverables

TurfMatch has been updated with a complete **Dual-Role Architecture**, enabling distinct experiences for **Players** and **Turf Owners**, along with direct turf booking confirmations and player fee contribution tracking.

## What Was Implemented

### 1. Dual Role Selection & User Profiles
- **Signup Role Selection (`SignupActivity`)**: Users choose between registering as a **Player** or a **Turf Owner** during account creation.
- **Turf Owner Profile (`UserProfile`)**: When "Turf Owner" is selected, an input field prompts for their managed turf name (e.g., "Borivali Station Turf").
- **Role-Based Login Routing (`LoginActivity`)**:
  - **Players** are routed to `MainActivity` (Pickup Game Feed, Create Match, Map, Profile).
  - **Turf Owners** are routed directly to `TurfOwnerDashboardActivity` (Turf Owner Portal).

### 2. Direct Booking Confirmation with Turf Owners
- **`GameModel` Turf Approval**: New matches created by players start with `turfApproved: false`.
- **Game Detail Status**: `GameDetailActivity` displays a status badge (`Turf Booking: Pending Turf Owner Approval` vs `Confirmed by Turf Owner ✓`).
- **Turf Owner Actions**: Turf Owners can review pending match bookings submitted to their turf and click **Confirm Booking** or **Decline Booking**.

### 3. Player Fee Contribution Tracking
- **Turf Owner Portal (`TurfOwnerDashboardActivity`)**: Displays all confirmed match bookings for the owner's turf along with the confirmed player roster.
- **Payment Status Toggles**: Turf Owners (as well as match hosts) can mark individual player fee contributions as **Paid** / **Unpaid** (`paid: boolean`) using checkboxes that update Firebase in real-time.
- **Visual Status Badges**: Confirmed player lists highlight paid statuses (`PAID ✓` in green vs `UNPAID ✗` in red).

## Build & Deployment Verification
- Build verified with `./gradlew assembleDebug` (BUILD SUCCESSFUL).
- Deployed and launched on Android emulator (`emulator-5554`).
