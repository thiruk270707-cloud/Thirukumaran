# MomCare — Emergency Medical Support Platform
## Single-Folder Code Bundle

This folder (`/single_folder/`) contains the complete application in single-file formats:

1. **`index.html`** (Complete Single-File Web Application):
   - **Google Maps Navigation**: Generates turn-by-turn driving directions, hospital searches, and coordinate map links.
   - **Contacts API Integration**: Synchronizes contact addresses using the modern Web Contacts API (`navigator.contacts.select`) with street address geocoding, distance calculations, and quick-call functionality.
   - **Interactive Medical Radar**: HTML5 Canvas radar displaying live GPS location, concentric distance rings, dynamic sweep beam, and real-time markers for hospitals, clinics, and contact addresses.
   - **3D Emergency SOS Beacon**: Instant dispatch via SMS, WhatsApp, Google Maps link, and direct emergency call (112/102).
   - **Labor Telemetry Tracker**: High-precision contraction stopwatch with interval frequency logs and local persistence.
   - **Maternal Vitals Profile**: Due date, blood group, doctor contacts, allergies, and local storage state.
   - **Zero Dependencies**: Simply open `index.html` in any web browser on desktop, Android Chrome, or iOS Safari.

2. **`MomCareAppSingleFile.kt`** (Complete Android Compose Native Application):
   - Unified, single-file native Kotlin Jetpack Compose implementation.
   - Contains Room database entities, DAOs, repository, ViewModel, interactive Canvas radar, Contacts Provider resolver, Google Maps intents, and M3 UI components.

---
### Running the Applications

- **For Web (`index.html`)**: Double-click `index.html` to open in any browser or deploy to any web hosting service (Vercel, GitHub Pages, Firebase Hosting).
- **For Android**: The full project files are in `/app/src/main/java/com/example/` and compiled in the Android environment.
