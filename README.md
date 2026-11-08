# 🚆 Online Reservation System (Java Swing + JDBC + SQLite)

A modern, desktop-based train and transport reservation system built using **Java Swing**, **JDBC**, and **SQLite**. Users can securely authenticate, book tickets with automatic train lookup, generate unique 10-digit PNR numbers, view their ticket receipts, inspect the train schedule directory, and cancel bookings via PNR with confirmation safeguards.

---

## 🌐 Live Web Edition & Vercel Deployment

In addition to the desktop Java application, a modern **Live Web Edition** (`index.html`, `app.js`, `vercel.json`) is included for web browsers and cloud deployment on **Vercel**:

### Deploy to Vercel in 1 Minute:
1. Go to **[vercel.com](https://vercel.com)** and sign in with your GitHub account (`dahakerohan23-gif`).
2. Click **"Add New..."** ➔ **"Project"**.
3. Select your repository: **`Online-Reservation-System`**.
4. Leave Framework Preset as **"Other"** (Root directory `./`).
5. Click **"Deploy"**.
6. Within 10 seconds, Vercel will give you a live production URL:
   `https://online-reservation-system-<your-name>.vercel.app`

---

## 📋 Feature Checklist Coverage

- [x] **Login Form**: Username and password fields, SHA-256 hashed password verification, inline and dialog error messages for invalid credentials, plus a user registration modal.
- [x] **Reservation Form**: Passenger name, numeric train number, train name (**auto-populated from train number**), class type dropdown (1A, 2A, 3A, SL, CC, EC, 2S), date of journey, source station, and destination station.
- [x] **Insert/Book Button**: Generates a unique 10-digit PNR number, saves the reservation to SQLite database using secure `PreparedStatement` to prevent SQL injection.
- [x] **Confirmation Dialog**: Displays complete booking details (PNR, Passenger Name, Train No & Name, Class, Date, Route, Status, Booking Time).
- [x] **Cancellation Form**: PNR number input field + **Fetch button** that retrieves and displays the full booking details card.
- [x] **Confirm Cancellation Button**: Protected by an `"Are you sure?"` confirmation dialog; permanently removes the booking from the database upon user confirmation.
- [x] **Input Validation**: Rejects empty required fields, enforces numeric train numbers, validates `YYYY-MM-DD` date format, blocks past dates, and prevents source/destination collision.

---

## 🛠️ Tech Stack & Architecture

- **GUI Framework**: Java Swing (`JFrame`, `JTabbedPane`, `JTable`, `JTextField`, `JComboBox`, `JDialog`, `JOptionPane`)
- **Database Engine**: SQLite 3 (Zero-configuration file database `reservation.db`)
- **Database Connectivity**: JDBC (`org.sqlite.JDBC`) with `PreparedStatement`
- **Security**: SHA-256 hashing for user passwords (`PasswordUtil`)
- **Logging**: SLF4J (integrated NOP logger to prevent console noise)

### Project Structure

```
d:\internshiprd\
├── lib\
│   ├── sqlite-jdbc.jar          # SQLite JDBC Driver (v3.45.1.0)
│   ├── slf4j-api.jar            # SLF4J API
│   └── slf4j-nop.jar            # SLF4J NOP Logger
├── src\
│   └── com\
│       └── reservation\
│           ├── Main.java                 # Application Entry Point & Look-and-Feel Setup
│           ├── db\
│           │   └── DatabaseManager.java  # SQLite Connection & Auto-schema Initialization
│           ├── model\
│           │   ├── User.java             # User entity
│           │   ├── Train.java            # Train schedule entity
│           │   └── Reservation.java      # Reservation / Booking entity
│           ├── dao\
│           │   ├── UserDao.java          # User authentication and registration
│           │   ├── TrainDao.java         # Train lookup and schedule querying
│           │   └── ReservationDao.java   # Booking creation, PNR search, and deletion
│           ├── util\
│           │   ├── PasswordUtil.java     # SHA-256 hashing and verification
│           │   ├── PNRGenerator.java     # 10-digit random unique PNR generator
│           │   └── ValidationUtil.java   # Input & Date validation logic
│           └── ui\
│               ├── UIStyle.java          # Design system (colors, fonts, buttons, cards)
│               ├── LoginFrame.java       # Login & Registration window
│               ├── MainFrame.java        # Dashboard with tabs and status bar
│               ├── ReservationPanel.java # Ticket booking form
│               ├── CancellationPanel.java# PNR fetch & cancellation form
│               ├── ViewBookingsPanel.java# Interactive JTable of reservations
│               └── TrainDirectoryPanel.java# Train schedule directory & 1-click booking
├── build\                                # Compiled .class bytecode
├── ReservationSystem.jar                 # Standalone executable JAR
├── TestSystem.java                       # Automated 36-point test suite
├── run.bat                               # Double-click launcher
├── build.bat                             # One-click compiler & packager
├── test.bat                              # Test runner
└── reservation.db                        # SQLite database file
```

---

## 🚀 How to Run the Application

### Option 1: Quick Launch (Windows Batch File)
Double-click `run.bat` or execute in terminal:
```cmd
run.bat
```

### Option 2: Run via Standalone JAR
```cmd
java --enable-native-access=ALL-UNNAMED -jar ReservationSystem.jar
```

### Option 3: Compile and Run from Source
```cmd
build.bat
run.bat
```

Or manually:
```cmd
javac -cp "lib/*" -d build src\com\reservation\util\*.java src\com\reservation\model\*.java src\com\reservation\db\*.java src\com\reservation\dao\*.java src\com\reservation\ui\*.java src\com\reservation\Main.java
java -cp "ReservationSystem.jar;lib/*" com.reservation.Main
```

---

## 🔑 Default Credentials

The database automatically seeds the following user accounts upon first launch:

| Username | Password | Full Name | Role |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | System Administrator | Administrator |
| `user` | `user123` | Demo Passenger | Passenger |
| `rohan` | `pass123` | Rohan Sharma | Passenger |

> **Note**: You can also register any new account instantly by clicking the **"Create New Account"** button on the Login screen!

---

## 🚆 Pre-Seeded Train Numbers

When booking a ticket, enter any of the following train numbers to automatically populate the train name, origin station, and destination:

| Train No. | Train Name | Origin | Destination |
| :--- | :--- | :--- | :--- |
| `12001` | Shatabdi Express | New Delhi | Bhopal Junction |
| `12951` | Mumbai Rajdhani Express | Mumbai Central | New Delhi |
| `22436` | Vande Bharat Express | New Delhi | Varanasi Junction |
| `12259` | Sealdah Bikaner Duronto | Sealdah | Bikaner Junction |
| `12626` | Kerala Superfast Express | New Delhi | Thiruvananthapuram Central |
| `12138` | Punjab Mail | Firozpur Cantt | Mumbai CSMT |
| `12301` | Howrah Rajdhani Express | Howrah Junction | New Delhi |
| `12616` | Grand Trunk Express | New Delhi | MGR Chennai Central |
| `20608` | Mysuru Vande Bharat | Mysuru Junction | MGR Chennai Central |
| `12009` | Mumbai Ahmedabad Shatabdi | Mumbai Central | Ahmedabad Junction |
| `12424` | Dibrugarh Rajdhani | New Delhi | Dibrugarh |
| `12724` | Telangana Express | New Delhi | Hyderabad Deccan |

*(You can also browse all trains directly in the **Train Schedule Directory** tab and click **"Book Selected Train"**).*

---

## 🧪 Automated Test Suite

To run the automated verification suite covering 36 critical tests (Authentication, Train Auto-population, Input Validation, PNR Generation, PreparedStatement SQL Injection Prevention, Database Insertion, Fetching, and Cancellation):

```cmd
test.bat
```
Output:
```
=================================================
  SUMMARY: 36/36 TESTS PASSED
=================================================
```

