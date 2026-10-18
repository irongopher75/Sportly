# Task List - TurfMatch Feature Extensions

- [x] Create `PlayerInfo.java` data model & update `GameModel.java` with hourlyRate, durationHours, maxPlayers, pendingRequests, confirmedRoster, and archived flag.
- [x] Update `FirebaseGameRepository.java` with methods for request join, accept join request, reject join request, update payment status, and archive expired games.
- [x] Update `CreateGameActivity.java` and `activity_create_game.xml` to include inputs for hourly rate, duration, and max players with validation.
- [x] Update `GameDetailActivity.java` and `activity_game_detail.xml` to compute and display rental cost split (total & per-player), show join request status, host pending requests management (Accept/Reject), and host payment status toggles.
- [x] Integrate opportunistic `archiveIfExpired` checks in `HomeFragment.java` and `GameDetailActivity.java`.
- [x] Build and verify with `./gradlew assembleDebug`.
- [x] Create walkthrough artifact (`walkthrough.artifact.md`).
