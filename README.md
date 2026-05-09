# OOP-Project
# 🏨 Hotel Reservation System

A comprehensive Hotel Management Application developed using **Java & JavaFX** with multiple layers, background threads, TCP Chat functionality, and cloud-based storage using **Supabase(PostgreSQL)**.

The project ships two runnable frontends:
- `hotel.Main` — console/terminal-driven UI (`hotel/ui/`)
- `hotel.gui.HotelApp` — JavaFX graphical UI (`hotel/gui/`)

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Multi-Threading Design](#multi-threading-design)
- [Chat System](#chat-system)
- [Cloud Persistence](#cloud-persistence)
- [Class Overview](#class-overview)
- [Demo Accounts](#demo-accounts)
- [Getting Started](#getting-started)

---

## ✨ Features

### Guest
- Sign up and sign in using username/password (verification: minimum of 8 characters, one number, one capital letter)
- Find available rooms through dynamic filters based on type and budget
- Search rooms for particular periods, detecting overlapping schedules
- Create reservations and purchase additional services when booking
- Cancel reservations (only those in CONFIRMED or PENDING status)
- Pay bills using account credits or outside sources (cash, credit card, debit card)
- Access full reservation and invoice history
- Add funds to account balance
- Customize room settings such as type, floor level, smoking, and accessibility
- Chat with the receptionist live

### Receptionist
- Access to all guests, rooms, and bookings
- Process check-ins (status change from CONFIRMED to CHECKED\_IN)
- Process check-outs (status change from CHECKED\_IN to CHECKED\_OUT)
- Quick view of today’s check-ins and check-out guests
- Real-time chat with guests
- Real-time room availability status board (refreshes every 15 seconds)

### Admin (Receptionist features + additional features)
- Complete control over rooms, room types, and room amenities
- Create new rooms with suggested IDs based on floors
- View all invoices and income report (KPI report)
- Total income and room occupancy statistics (occupied and available rooms, total number of guests and bookings)


## 🏗️ Architecture

```
┌───────────────────────────────────────────────────────────┐
│                      Entry Points                         │
│        hotel.Main (console)   hotel.gui.HotelApp (GUI)    │
└──────────────────────┬────────────────────────────────────┘
                       │
┌──────────────────────▼────────────────────────────────────┐
│                    GUI Layer (JavaFX)                      │
│  hotel.gui.controllers                                     │
│  LoginController · GuestDashboardController               │
│  StaffDashboardController · ChatController                 │
│                                                            │
│  hotel.gui.utils                                           │
│  SceneManager (singleton) · AlertHelper                    │
│                                                            │
│  hotel.ui  (console menus)                                 │
│  MainMenu · GuestMenu · AdminMenu · ReceptionistMenu       │
└──────────────────────┬────────────────────────────────────┘
                       │ calls
┌──────────────────────▼────────────────────────────────────┐
│                   Service Layer                            │
│  AuthService · GuestService · AdminService                 │
│  ReceptionistService · RoomService                         │
│  ReservationService · InvoiceService                       │
└──────────────────────┬────────────────────────────────────┘
                       │ reads / writes
┌──────────────────────▼────────────────────────────────────┐
│              In-Memory Store + Cloud Sync                  │
│  hotel.database.HotelDatabase  (static ArrayLists)        │
│  hotel.database.DatabaseSyncService  (Supabase / JDBC)    │
└──────────────────────┬────────────────────────────────────┘
                       │
┌──────────────────────▼────────────────────────────────────┐
│                    Domain Model                            │
│  Guest · Staff · Admin · Receptionist                      │
│  Room · RoomType · Amenity · RoomPreference                │
│  Reservation · Invoice                                     │
│  Interfaces: Authenticatable · Manageable<T> · Payable    │
└───────────────────────────────────────────────────────────┘

┌───────────────────────────────────────────────────────────┐
│                  Network Layer (TCP)                        │
│  ChatServer (port 8080) — runs on receptionist's machine  │
│  ChatClient — connects from guest dashboard               │
└───────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
OOP-Project-main/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── hotel/
        │       ├── Main.java                        ← console entry point
        │       │
        │       ├── models/                          ← domain objects
        │       │   ├── Guest.java
        │       │   ├── Staff.java                   (abstract)
        │       │   ├── Admin.java
        │       │   ├── Receptionist.java
        │       │   ├── Room.java
        │       │   ├── RoomType.java
        │       │   ├── Amenity.java
        │       │   ├── RoomPreference.java
        │       │   ├── Reservation.java
        │       │   └── Invoice.java
        │       │
        │       ├── enums/
        │       │   ├── ReservationStatus.java       (PENDING/CONFIRMED/CHECKED_IN/CHECKED_OUT/CANCELLED)
        │       │   ├── PaymentMethod.java           (CASH/CREDIT_CARD/DEBIT_CARD/BALANCE)
        │       │   ├── Role.java                    (ADMIN/RECEPTIONIST)
        │       │   └── Gender.java                  (MALE/FEMALE)
        │       │
        │       ├── interfaces/
        │       │   ├── Authenticatable.java         (authenticate, getUsername)
        │       │   ├── Manageable.java              (add/update/delete/findById — generic)
        │       │   └── Payable.java                 (pay, getAmountDue)
        │       │
        │       ├── database/
        │       │   ├── HotelDatabase.java           ← static lists + seed data
        │       │   └── DatabaseSyncService.java     ← Supabase sync (threaded)
        │       │
        │       ├── services/
        │       │   ├── AuthService.java
        │       │   ├── GuestService.java
        │       │   ├── AdminService.java
        │       │   ├── ReceptionistService.java
        │       │   ├── RoomService.java             (implements Manageable<Room>)
        │       │   ├── ReservationService.java
        │       │   └── InvoiceService.java
        │       │
        │       ├── gui/
        │       │   ├── HotelApp.java                ← JavaFX entry point
        │       │   ├── controllers/
        │       │   │   ├── LoginController.java
        │       │   │   ├── GuestDashboardController.java
        │       │   │   ├── StaffDashboardController.java
        │       │   │   └── ChatController.java
        │       │   └── utils/
        │       │       ├── SceneManager.java        (singleton, holds all services)
        │       │       └── AlertHelper.java
        │       │
        │       ├── ui/                              ← console menus
        │       │   ├── MainMenu.java
        │       │   ├── GuestMenu.java
        │       │   ├── AdminMenu.java
        │       │   └── ReceptionistMenu.java
        │       │
        │       ├── network/
        │       │   ├── ChatServer.java              (singleton, TCP port 8080)
        │       │   └── ChatClient.java              (singleton)
        │       │
        │       └── utils/
        │           ├── PasswordUtils.java
        │           └── DisplayUtils.java
        │
        └── resources/
            └── hotel/gui/
                ├── fxml/
                │   ├── Login.fxml
                │   ├── GuestDashboard.fxml
                │   ├── StaffDashboard.fxml
                │   └── ChatWindow.fxml
                └── css/
                    └── main.css
```

---

## 🧵 Multi-Threading Design

All blocking or periodic processes are carried out using the JavaFX Application Thread. UI components can only be modified through calls to `Platform.runLater()` or JavaFX `Task` handlers (which will dispatch back to the FX thread).

### 1. Room Availability Auto-Refresh — Guest Dashboard

**Where:** `GuestDashboardController.startRoomRefreshTask()`  
**Mechanism:** `ScheduledExecutorService` (single daemon thread)  
**Interval:** every **10 seconds**


Filters (type, budget) are captured as final local variables before the task runs, so the background thread reads an **immutable snapshot** — no race conditions.

### 2. Room Status Auto-Refresh — Staff Dashboard

**Where:** `StaffDashboardController.startRoomStatusRefresh()`  
**Mechanism:** `ScheduledExecutorService` (single daemon thread)  
**Interval:** every **15 seconds**

Follows the same pattern as the guest-side refresh. Only updates the `liveStaffRoomTable` when the rooms pane (identified by `node.getId() == "staffRoomsPane"`) is the active content, avoiding unnecessary work when other tabs are open.

### 3. Background Loading — Reservations & Invoices (Guest)

**Where:** `buildMyReservationsPaneAsync()`, `buildInvoicesPaneAsync()`  
**Mechanism:** `javafx.concurrent.Task<List<T>>` on a daemon thread


A `ProgressIndicator` spinner is shown immediately, then swapped for real content once the task completes.

### 4. Background Loading — Guests & Reservations (Staff)

**Where:** `buildGuestsPaneAsync()`, `buildReservationsPaneAsync()` in `StaffDashboardController`  
**Mechanism:** Same `Task` + daemon thread pattern, using a shared `daemon(task, name)` helper method.

### 5. Database Sync — Supabase

**Where:** `DatabaseSyncService.startSyncTask()`  
**Mechanism:** `ScheduledExecutorService` (single named daemon thread `"database-sync-thread"`)  
**Interval:** every **5 seconds**  
**Tables synced:** `reservations_sync`, `guests_sync`


On application close, `HotelApp.stop()` calls `DatabaseSyncService.forceSync()` for a final blocking flush before the JVM exits.

### Thread-Safety Summary

| Rule | Applied where |
|---|---|
| All JavaFX node reads/writes on FX thread | Enforced via `Platform.runLater()` in every scheduler callback |
| `Task.setOnSucceeded` / `setOnFailed` fire on FX thread automatically | All four async pane builders |
| Daemon threads so they never block JVM shutdown | All background threads and scheduler factories |
| Filter values snapshot before background task runs | `GuestDashboardController.startRoomRefreshTask()` |
| `DatabaseSyncService.syncedState` uses `ConcurrentHashMap` | Conflict-detection maps for reservations and guest balances |
| `ChatServer.clients` uses `CopyOnWriteArrayList` | Safe concurrent reads during broadcast to all clients |

---

## 💬 Chat System

A real-time TCP chat connects guests and receptionists directly in the application.

| Component | Class | Detail |
|---|---|---|
| Server | `ChatServer` | Singleton; starts on `localhost:8080` when staff logs in. Uses a cached `ExecutorService` thread pool — each connected client gets its own `ClientHandler` thread. Broadcasts every received message to all clients. |
| Client | `ChatClient` | Singleton; connects from the guest dashboard. A background thread listens for incoming messages and delivers them to the UI via `Platform.runLater()` through a registered `Consumer<String>` callback. |
| UI | `ChatController` | FXML controller for `ChatWindow.fxml`. Calls `ChatServer.broadcast()` (staff) or `ChatClient.sendMessage()` (guest). |

**Startup sequence:**
1. Staff logs in → `StaffDashboardController.initialize()` calls `ChatServer.getInstance().start()`
2. Guest clicks the Chat tab → `ChatClient.getInstance().connect(username)` is called
3. Messages: Guest → TCP → port 8080 → `broadcast()` → all connected clients + server UI callback

---

## ☁️ Cloud Persistence

The app syncs with a **Supabase PostgreSQL** database hosted on AWS (eu-west-1).

**Tables:**

| Table | Key synced fields |
|---|---|
| `reservations_sync` | `reservation_id`, `guest_id`, `room_id`, `check_in_date`, `check_out_date`, `status`, `total_cost`, `created_at` |
| `guests_sync` | `guest_id`, `username`, `password`, `date_of_birth`, `balance`, `address`, `gender`, `pref_type`, `pref_floor`, `pref_smoking`, `pref_access` |

**Sync logic:** Remote-only records are pulled into the local lists. Local-only or changed records are upserted. A `ConcurrentHashMap` of last-synced states detects which side changed since the previous cycle, preventing one side from silently overwriting the other.

---

## 📦 Class Overview

### Domain Models

| Class | Key Responsibilities |
|---|---|
| `Guest` | Credentials (stored via `PasswordUtils`), balance (`deposit` / `deduct`), address, room preferences. Auto-generates IDs (`G001`, `G002`, …). Implements `Authenticatable`. |
| `Staff` *(abstract)* | Shared base for `Admin` and `Receptionist`. Hashed passwords, working hours, role, gender. Implements `Authenticatable`. |
| `Admin` | Extends `Staff`; role is `ADMIN`. Used for role-based access checks in controllers. |
| `Receptionist` | Extends `Staff`; role is `RECEPTIONIST`. |
| `Room` | Room ID, floor number, `RoomType`, list of `Amenity`. Computes `getTotalPricePerNight()` as base + sum of amenity costs. |
| `RoomType` | Name, description, base price, max occupancy. Auto-generates IDs (`rt006`, …) for new types. |
| `Amenity` | Name, description, extra cost per night. Auto-generates IDs (`a009`, …) for new amenities. |
| `RoomPreference` | Preferred type name, floor, smoking flag, accessibility flag. Attached to `Guest`. |
| `Reservation` | Links a guest ID to a room ID for a date range. Lifecycle: `cancel()`, `checkIn()`, `checkOut()`. Auto-generates IDs (`R001`, …). Supports extra amenities. |
| `Invoice` | Tracks `amountDue`, `amountPaid`, `PaymentMethod`, paid status. Implements `Payable`. Guards against modification after payment. |

### Enums

| Enum | Values |
|---|---|
| `ReservationStatus` | `PENDING`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED` |
| `PaymentMethod` | `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BALANCE` |
| `Role` | `ADMIN`, `RECEPTIONIST` |
| `Gender` | `MALE`, `FEMALE` |

### Interfaces

| Interface | Contract |
|---|---|
| `Authenticatable` | `authenticate(plainPassword)`, `getUsername()` — implemented by `Guest` and `Staff` |
| `Manageable<T>` | `add(T)`, `update(T)`, `delete(String id)`, `findById(String id)` — implemented by `RoomService` |
| `Payable` | `pay(amount, PaymentMethod)`, `getAmountDue()` — implemented by `Invoice` |

### Services

| Service | Responsibilities |
|---|---|
| `AuthService` | Guest registration (with validation), guest/staff login, create staff accounts |
| `RoomService` | CRUD on rooms, room types, amenities. Filtering by type, floor, budget, amenity list. Implements `Manageable<Room>`. |
| `ReservationService` | Make reservations (overlap detection), cancel, check-in, check-out, add extra amenities |
| `GuestService` | Facade over `RoomService`, `ReservationService`, `InvoiceService` for guest operations |
| `AdminService` | Facade for all room/amenity/type CRUD; revenue and occupancy reporting |
| `ReceptionistService` | Facade for check-in/check-out; view guests, rooms, reservations |
| `InvoiceService` | Create, pay (balance or external), void, update invoice amounts |

---

## 🔐 Demo Accounts

### Staff

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `Admin@123` |
| Receptionist | `pierre` | `Recep@123` |
| Receptionist | `fabio` | `Recep@456` |

### Guests

| Username | Password | Balance |
|---|---|---|
| `habiba` | `Guest@123` | 3,000 EGP |
| `mennat-allah` | `Guest@456` | 7,500 EGP |
| `selena` | `Guest@789` | 1,200 EGP |

---

## 🏠 Seeded Data

### Room Types

| ID | Name | Base Price/Night | Max Occupancy |
|---|---|---|---|
| rt001 | Single | 500 EGP | 1 |
| rt002 | Double | 800 EGP | 2 |
| rt003 | Suite | 2,000 EGP | 4 |
| rt004 | Deluxe | 1,200 EGP | 2 |
| rt005 | Family | 1,500 EGP | 6 |

### Amenities

| ID | Name | Extra Cost/Night |
|---|---|---|
| a001 | WiFi | 10 EGP |
| a002 | TV | 5 EGP |
| a003 | Mini-bar | 20 EGP |
| a004 | Air Conditioning | Free |
| a005 | Jacuzzi | 50 EGP |
| a006 | Breakfast | 30 EGP |
| a007 | Safe | Free |
| a008 | Gym Access | 15 EGP |

### Rooms (12 total across 5 floors)

| Floor | Rooms | Type |
|---|---|---|
| 1 | 101, 102, 103 | Single |
| 2 | 201, 202, 203 | Double |
| 3 | 301, 302 | Deluxe |
| 4 | 401, 402 | Family |
| 5 | 501, 502 | Suite |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- JavaFX 21 (included via Maven dependencies)

### Run the JavaFX GUI

```bash
cd OOP-Project-main
mvn clean javafx:run
```

### Run the Console UI

```bash
mvn compile
mvn exec:java -Dexec.mainClass="hotel.Main"
```

### Build

```bash
mvn clean package
```

### Password Rules

All passwords must be **at least 8 characters**, contain **at least one digit**, and **at least one uppercase letter**.  
Example valid passwords: `Admin@123`, `Guest@123`
