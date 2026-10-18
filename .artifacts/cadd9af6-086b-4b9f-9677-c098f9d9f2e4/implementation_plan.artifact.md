# TurfMatch Feature Extension Implementation Plan

Extend the TurfMatch Android project with three advanced features: Hourly Rental Cost Split, Host Approval/Rejection for Join Requests, and Post-Event Cleanup with Match-History Archival.

## User Review Required

> [!IMPORTANT]
> - **Data Model Refactoring**: The game roster is transitioning from a flat list of user IDs to structured maps (`pendingRequests` and `confirmedRoster`) supporting host approval workflow, max player limits, rental cost splits, and paid status toggles.
> - **No Cloud Functions / WorkManager**: All checks and archival actions execute opportunistically during normal app navigation (feed load, game detail view) using client-side Java logic and Firebase RTDB partial updates.

## Proposed Changes

### Data Model & Repository (`com.turfmatch.app.model`, `repository`)
- **[MODIFY]** `GameModel.java`:
  - Add `hourlyRate` (double), `durationHours` (double), `maxPlayers` (int).
  - Replace flat `List<String> roster` with `Map<String, PlayerInfo> pendingRequests` and `Map<String, PlayerInfo> confirmedRoster`.
- **[NEW]** `PlayerInfo.java`: Helper model representing user info (userId, name, paid status).
- **[MODIFY]** `FirebaseGameRepository.java`:
  - Add methods for requesting to join (`requestJoinGame`), accepting request (`acceptJoinRequest`), rejecting request (`rejectJoinRequest`), updating payment status (`updateRosterPaymentStatus`), and archiving expired games (`archiveIfExpired`).

### Match Creation (`com.turfmatch.app.ui.create`)
- **[MODIFY]** `CreateGameActivity.java` & `activity_create_game.xml`:
  - Add inputs for Hourly Rate ($), Duration Hours (e.g., 1, 1.5, 2), and Max Players (integer validation).
  - Pass new fields to `GameModel` creation.

### Game Detail & Host Workflow (`com.turfmatch.app.ui.detail`)
- **[MODIFY]** `GameDetailActivity.java` & `activity_game_detail.xml`:
  - Compute and display Total Cost (`hourlyRate * durationHours`) and Per-Player Cost (`totalCost / confirmedRoster.size()`, with division-by-zero guard).
  - Display "Join Request Sent" confirmation Toast upon joining.
  - If user is host: display "Pending Requests" list with **Accept** and **Reject** buttons.
  - If user is host: display `confirmedRoster` list with "Mark as Paid" checkboxes updating payment status in real-time.

### Post-Event Cleanup & Match History Archival (`HomeFragment`, `FirebaseGameRepository`)
- **[MODIFY]** `HomeFragment.java` & `FirebaseGameRepository.java`:
  - Trigger `archiveIfExpired` opportunistically when loading the live games feed and when opening `GameDetailActivity`.
  - Archive expired games by writing condensed records to `matchHistory/{userId}/{gameId}` for all confirmed players and setting `archived: true` on the live game node.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation and resource linking.

### Manual Verification
- Deploy to emulator / device:
  1. Create a game with hourly rate, duration, and max players.
  2. Join the game from another account -> verify pending request is created and toast says request sent.
  3. Log in as host -> view pending requests, accept or reject.
  4. Verify cost split calculation and host "Mark as Paid" toggle.
  5. Check match history archival for expired matches.
