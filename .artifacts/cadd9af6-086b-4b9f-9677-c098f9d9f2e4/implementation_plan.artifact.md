# Dual-Role System: Player vs. Turf Owner Implementation Plan

Implement a major architectural update introducing dual sign-in roles (**Player** vs. **Turf Owner**), direct booking confirmation with turf owners, and payment tracking for player fee contributions.

## User Review Required

> [!IMPORTANT]
> - **Dual Roles**: Users select whether they are a **Player** or a **Turf Owner** during signup.
> - **Turf Owner Confirmation**: When a player creates a match at a turf, the match status is set to `turfApproved: false`. The Turf Owner must confirm the booking directly from their dashboard.
> - **Payment Contribution Tracking**: Both the Turf Owner and the match host can track player fee contributions and toggle player payment status (`paid: true/false`).

## Proposed Changes

### Data Models (`com.turfmatch.app.model`)
- **[MODIFY]** `UserProfile.java`:
  - Add `userType` (`"PLAYER"` or `"TURF_OWNER"`) and `ownedTurfName` (string).
- **[MODIFY]** `GameModel.java`:
  - Add `turfApproved` (boolean) and `turfOwnerId` (string).

### Authentication (`com.turfmatch.app.auth`)
- **[MODIFY]** `activity_signup.xml` & `SignupActivity.java`:
  - Add Role Selection (`RadioGroup`: Player vs Turf Owner).
  - If "Turf Owner" is selected, dynamically show an input field for `ownedTurfName`.
  - Save `userType` and `ownedTurfName` into Firebase user profile.
- **[MODIFY]** `LoginActivity.java`:
  - Route users to `MainActivity` (Player Dashboard) or `TurfOwnerDashboardActivity` based on `userType`.

### Data Access Layer (`com.turfmatch.app.repository`)
- **[MODIFY]** `FirebaseGameRepository.java`:
  - Add `confirmTurfBooking(gameId, callback)` and `rejectTurfBooking(gameId, callback)`.
  - Add `getTurfOwnerBookings(ownedTurfName, callback)` to load all match bookings submitted to a specific turf owner's turf.

### Turf Owner Dashboard (`com.turfmatch.app.ui.turfowner`)
- **[NEW]** `TurfOwnerDashboardActivity.java` & `activity_turf_owner_dashboard.xml`:
  - Dashboard for Turf Owners displaying:
    1. **Pending Match Bookings**: Matches created at their turf awaiting owner confirmation (**Confirm** / **Decline** buttons).
    2. **Confirmed Matches & Roster Fee Tracking**: Confirmed games with full player rosters, allowing the turf owner to toggle individual player payment statuses (**Mark Paid** / **Mark Unpaid**).
    3. Logout and profile management.

### Game Detail & Player Feed (`com.turfmatch.app.ui.detail`, `HomeFragment`)
- **[MODIFY]** `GameDetailActivity.java` & `activity_game_detail.xml`:
  - Display Turf Confirmation Status badge (`Pending Turf Approval` vs `Turf Booking Confirmed ✓`).
  - If the logged-in user is the Turf Owner of that turf, display Turf Owner action controls (**Confirm Match**, **Mark Player Payments**).

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation.

### Manual Verification
- Deploy to emulator / device:
  1. **Turf Owner Signup**: Register as a Turf Owner (e.g. for "Borivali Station Turf").
  2. **Player Signup & Match Creation**: Register as a Player, create a match at "Borivali Station Turf" -> status shows "Pending Turf Approval".
  3. **Turf Owner Confirmation**: Log in as Turf Owner -> view pending booking -> click **Confirm Booking**.
  4. **Fee Contribution Tracking**: Turf Owner opens confirmed match -> toggles player payment status to "Paid".
