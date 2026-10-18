# TurfMatch Feature Extensions - Walkthrough

The TurfMatch project has been successfully extended with three robust, client-side Java features using Firebase Realtime Database for data persistence.

## Summary of New Features

### 1. Hourly Rental Cost Split
- **GameModel & CreateGameActivity**: Added `hourlyRate` (double), `durationHours` (double), and `maxPlayers` (int) inputs with validation.
- **GameDetailActivity & Cost Calculation**: Automatically computes total cost (`hourlyRate * durationHours`) and per-player cost (`totalCost / confirmedRoster.size()`, with division-by-zero guards).
- **Host Payment Toggles**: Host-visible checkboxes per confirmed player to toggle paid/unpaid status (`paid: boolean`) via partial updates (`updateRosterPaymentStatus`). Real-time visual payment status indicators (✓ Paid / ✗ Unpaid).

### 2. Host Approval / Rejection for Join Requests
- **Roster Refactoring**: Replaced flat user list with structured maps (`pendingRequests` and `confirmedRoster`).
- **Join Request Workflow**: Players tap "Request to Join Match", sending a request to `pendingRequests` (with "Join Request Pending" / "Join Request Sent" status).
- **Host Management Section**: When the logged-in user is the match host, a management panel displays:
  - **Pending Requests**: Lists requesting players with **Accept** and **Reject** buttons. Accepting checks `confirmedRoster.size() < maxPlayers` (preventing overbooking) before promoting the player.
  - **Confirmed Roster**: Lists confirmed players with payment checkboxes.
- **Partial Update Repository Methods**: `requestJoinGame`, `acceptJoinRequest`, and `rejectJoinRequest` manipulate sub-nodes without overwriting the entire game record.

### 3. Post-Event Cleanup & Match-History Archival
- **Opportunistic Archival (`archiveIfExpired`)**: Checks scheduled match date and time against the current device time whenever live games are loaded in `HomeFragment` or viewed in `GameDetailActivity`.
- **Permanent Archival**: For expired matches:
  - Writes condensed records into `matchHistory/{userId}/{gameId}` for each confirmed player containing turf name, sport, date, competitive level, final per-player cost, and paid status.
  - Sets `archived: true` on the live game node to remove it from the active feed query while preserving permanent history.

## Build & Verification
- Compiled successfully with `./gradlew assembleDebug` (0 errors).
