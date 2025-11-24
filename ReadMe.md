# **Milestone 1 \- DuelDex (Unit 7\)**

## **Table of Contents**

* Overview

* Product Spec

* Wireframes

---

## **Overview**

### **Description**

**DuelDex** is a Yu-Gi-Oh\! deck-building companion app that bridges the physical and digital collecting experience.  
 Users can browse the entire Yu-Gi-Oh\! card database using a public API and **scan their real cards with their phone camera** to mark them as *scanned/unlocked* in their collection.  
 Unscanned cards are visible but appear **grayed out**, while scanned ones appear in full color.

The app also includes **Deck Scanner Reminders**, which notify players when they haven’t scanned a new card recently, and a **Shops Tab**, which uses **GPS and Google Maps** to display nearby trading card shops where users can buy or trade cards.

---

### **App Evaluation**

**Category:** Gaming / Companion Tool / Collectible Utility

**Mobile:** Mobile-exclusive features include camera-based card recognition, local data persistence, push notifications for reminders, and GPS-based map navigation. These rely heavily on phone hardware and can’t be replicated easily on desktop.

**Story:** DuelDex connects the nostalgia of owning physical Yu-Gi-Oh\! cards with a modern digital interface. Players can visually build and track their collection while scanning real cards they own, turning collecting into an interactive experience. The addition of local shop discovery enhances the real-world connection.

**Market:** Yu-Gi-Oh\! players, collectors, and card shop customers. The app appeals to both competitive players and casual fans who want to manage, verify, and expand their decks conveniently.

**Habit:** Users open the app frequently to scan new cards after pack openings, review their collection, check reminders, or find local stores to expand their decks.

**Scope:**

* **V1:** API integration, card browsing, camera scan, grayed-out unlock system.

* **V2:** Deck Scanner Reminders and local data storage.

* **V3:** Shops tab with map and GPS integration.

* **V4:** deck-building tools and Firebase account sync.

---

## **Product Spec**

### **1\. User Features (Required and Optional)**

#### **Required Features**

* Users can browse **all Yu-Gi-Oh\! cards** through the public API.

* Users can **scan cards using their camera** to mark them as “scanned/unlocked.”

* Unscanned cards are **grayed out**, while scanned ones appear in full color.

* **Deck Scanner Reminder** sends push notifications when users haven’t scanned new cards.

* **Shops Tab** displays nearby card shops using Google Maps and the user’s GPS location.

* All progress (scanned cards, favorites, and shops) is **saved locally** on the device.

#### **Optional Features**

* Deck builder tool to create and save custom decks.

* Smart scan history (track when and where cards were scanned).

* Offline mode for card browsing and deck editing.

* Price tracking alerts for scanned cards.

* Account login with Firebase to sync decks across devices.

---

### **2\. Screen Archetypes**

#### **Login / Signup Screen**

* Allows users to sign in or create an account to save deck progress and preferences.

#### **Home / Card Browser Screen**

* Displays all Yu-Gi-Oh\! cards from the API in a grid layout.

* Unscanned cards appear **grayed out**; scanned cards are **highlighted**.

* Shows scan progress (e.g., “245 / 1000 cards scanned”).

#### **Camera / Scan Screen**

* Uses the phone's camera and OCR/image recognition to detect and verify cards.

* Successfully scanned cards are marked as “unlocked.”

#### **Card Detail Screen**

* Shows card image, ATK/DEF stats, type, and effect description.

* Displays whether the card is scanned/unlocked.

#### **Reminders Screen**

* Allows users to view and manage **Deck Scanner Reminders**.

* Toggle notifications on/off or view recent reminders.

#### **Shops Screen (New Feature)**

* Displays a **map** showing nearby trading card shops using Google Maps SDK.

* Uses **GPS** to find current location and plot shops nearby.

* Users can tap markers to view details and open Google Maps directions.

#### **Settings Screen**

* Contains app preferences: dark mode, reminders toggle, location permissions, and account info.

---

### **3\. Navigation**

#### **Tab Navigation (Tab to Screen)**

*  **Home (Cards)** – View all cards and unlock progress.

*  **Scan** – Scan real cards to unlock them.

*  **Shops** – View nearby card shops using the map.

*  **Reminders** – Manage Deck Scanner notifications.

*  **Settings** – Adjust preferences and account.

#### **Flow Navigation (Screen to Screen)**

* **Login → Home Screen**

* **Home → Card Detail → Back to Home**

* **Home → Scan → Confirm Unlock → Home**

* **Home → Shops → Marker → Shop Detail → Google Maps**

* **Home → Search** 

* **Settings → Home**

---

## **Wireframes**

<img width="599" height="373" alt="Wrieframe1" src="https://github.com/user-attachments/assets/a378dd53-d85b-437a-b45d-dc1dff8e41b3" />
<img width="596" height="400" alt="wireframe2" src="https://github.com/user-attachments/assets/ff6350bf-2e0a-4829-b0a0-439285b7b575" />
<img width="563" height="426" alt="Wrieframe3" src="https://github.com/user-attachments/assets/0e73cd52-d88b-4d86-937b-98e5352934f8" />
<img width="544" height="395" alt="wireframe4" src="https://github.com/user-attachments/assets/8c547b6e-92d9-4c4b-97c4-65003072e93b" />


 **Suggested layout ideas:**

* **Home:** Grid of all cards with lock/unlock overlay.

* **Scan:** Camera interface with “Capture” button.

* **Card Detail:** Full-size image \+ info.

* **Shops:** Map view centered on user with nearby shop markers.

* **Reminders:** Simple toggle list for notification settings.

* # Milestone 2 - Build Sprint 1 (Unit 8)

## GitHub Project board
<img src="https://i.imgur.com/npmHlLq.png" width=600>

## Issue cards

<img src="https://i.imgur.com/250W2e7.png" width=600>
<img src="https://i.imgur.com/8feDAeM.png" width=600>

## Issues worked on this sprint
1. Settings Screen
2. Implement bottom nav (Home, Scan, Settings)
3. Design card grid (locked/unlocked states) and Recycler View
4. Log in Screen
5. Fetch cards from API and save to database
- Build Progress
  
<img src="https://i.imgur.com/pzJYYY0.gif" width=600>

## Completed User Stories

- [x] User can browse all Yu-Gi-Oh! cards through the public API
- [x] User can register and log in in saved.
- [x] User can navigate using the bottom navigation bar (Home, Scan, Settings)  
- [ ] User can scan cards using their camera (in progress)  
- [ ] Shops Tab displays nearby card shops (upcoming)
  # Milestone 3 - Build Sprint 2 (Unit 9)
  ## GitHub Project board

  <img width="960" height="540" alt="milestone3projectboardPNG" src="https://github.com/user-attachments/assets/31fb3f57-ef93-403d-a58d-a447aa80df39" />

  ## Issue cards

  <img width="960" height="540" alt="issuecardsmilestone2" src="https://github.com/user-attachments/assets/bfde34b3-b5e6-4d8b-ba2f-555dec1776af" />

  ## Issues worked on this sprint
- [x] Maps fragment
- [x] Load Nearby shops
- [x] Camera Scanner
- [x] Request GPS permissions
- [x]  Add profile page/fragment
- [ ] Improve Syle of all fragments(in progress)

  ## DEMO

<br>

