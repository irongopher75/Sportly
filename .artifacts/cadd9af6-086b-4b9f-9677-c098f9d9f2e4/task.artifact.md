# Task List - Dual Role System (Player vs Turf Owner)

- [x] Update `UserProfile.java` with `userType` and `ownedTurfName`.
- [x] Update `GameModel.java` with `turfApproved` and `turfOwnerId`.
- [x] Update `SignupActivity.java` & `activity_signup.xml` with role selection (Player / Turf Owner) and conditional turf name field.
- [x] Update `LoginActivity.java` to perform role-based routing (Player -> MainActivity, Turf Owner -> TurfOwnerDashboardActivity).
- [x] Update `FirebaseGameRepository.java` with `confirmTurfBooking`, `rejectTurfBooking`, and `getTurfOwnerBookings`.
- [x] Create `TurfOwnerDashboardActivity.java` & `activity_turf_owner_dashboard.xml` for Turf Owners to confirm bookings and manage player fee contributions.
- [x] Update `GameDetailActivity.java` & `activity_game_detail.xml` to display turf approval status and allow Turf Owners to confirm bookings and toggle player payment statuses.
- [x] Verify build with `./gradlew assembleDebug`.
- [x] Deploy to emulator.
- [x] Create `walkthrough.artifact.md`.
