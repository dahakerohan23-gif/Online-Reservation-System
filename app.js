/**
 * Online Reservation System - Full Client-side Engine
 * Features: Login, Registration, Train Auto-Population, PNR Generation, 
 * Confirmation Modal, Cancellation with "Are you sure?" Confirmation,
 * Persistent LocalStorage Database.
 */

// Safe Icon Helper
function safeCreateIcons() {
  if (window.lucide && typeof lucide.createIcons === 'function') {
    try {
      lucide.createIcons();
    } catch (e) {
      console.warn("Lucide icons warning:", e);
    }
  }
}

// ==================== DEFAULT SEED DATA ====================
const DEFAULT_TRAINS = [
  { number: 12001, name: "Shatabdi Express", source: "New Delhi", destination: "Bhopal Junction" },
  { number: 12951, name: "Mumbai Rajdhani Express", source: "Mumbai Central", destination: "New Delhi" },
  { number: 22436, name: "Vande Bharat Express", source: "New Delhi", destination: "Varanasi Junction" },
  { number: 12259, name: "Sealdah Bikaner Duronto", source: "Sealdah", destination: "Bikaner Junction" },
  { number: 12626, name: "Kerala Superfast Express", source: "New Delhi", destination: "Thiruvananthapuram Central" },
  { number: 12138, name: "Punjab Mail", source: "Firozpur Cantt", destination: "Mumbai CSMT" },
  { number: 12301, name: "Howrah Rajdhani Express", source: "Howrah Junction", destination: "New Delhi" },
  { number: 12616, name: "Grand Trunk Express", source: "New Delhi", destination: "MGR Chennai Central" },
  { number: 20608, name: "Mysuru Vande Bharat", source: "Mysuru Junction", destination: "MGR Chennai Central" },
  { number: 12009, name: "Mumbai Ahmedabad Shatabdi", source: "Mumbai Central", destination: "Ahmedabad Junction" },
  { number: 12424, name: "Dibrugarh Rajdhani", source: "New Delhi", destination: "Dibrugarh" },
  { number: 12724, name: "Telangana Express", source: "New Delhi", destination: "Hyderabad Deccan" }
];

const DEFAULT_USERS = [
  { username: "admin", password: "admin123", fullName: "System Administrator", role: "ADMIN" },
  { username: "rohan", password: "pass123", fullName: "Rohan Dahake", role: "USER" },
  { username: "user", password: "user123", fullName: "Demo Passenger", role: "USER" }
];

const DEFAULT_BOOKINGS = [
  {
    pnr: "8492019482",
    passengerName: "Aarav Patel",
    trainNumber: 12951,
    trainName: "Mumbai Rajdhani Express",
    classType: "2A - AC 2-Tier",
    journeyDate: "2026-11-15",
    source: "Mumbai Central",
    destination: "New Delhi",
    bookingDate: "2026-11-08 14:20:10",
    status: "CONFIRMED",
    bookedBy: "rohan"
  },
  {
    pnr: "6381920394",
    passengerName: "Sneha Mukherjee",
    trainNumber: 22436,
    trainName: "Vande Bharat Express",
    classType: "CC - AC Chair Car",
    journeyDate: "2026-11-20",
    source: "New Delhi",
    destination: "Varanasi Junction",
    bookingDate: "2026-11-08 15:45:00",
    status: "CONFIRMED",
    bookedBy: "admin"
  }
];

// ==================== STORAGE CONTROLLER ====================
class Database {
  static init() {
    try {
      if (!localStorage.getItem("rail_users")) {
        localStorage.setItem("rail_users", JSON.stringify(DEFAULT_USERS));
      }
      if (!localStorage.getItem("rail_trains")) {
        localStorage.setItem("rail_trains", JSON.stringify(DEFAULT_TRAINS));
      }
      if (!localStorage.getItem("rail_bookings")) {
        localStorage.setItem("rail_bookings", JSON.stringify(DEFAULT_BOOKINGS));
      }
    } catch (e) {
      console.warn("Storage error:", e);
    }
  }

  static getUsers() {
    try {
      return JSON.parse(localStorage.getItem("rail_users") || "[]");
    } catch (e) {
      return DEFAULT_USERS;
    }
  }

  static saveUser(user) {
    const users = this.getUsers();
    users.push(user);
    localStorage.setItem("rail_users", JSON.stringify(users));
  }

  static getTrains() {
    try {
      return JSON.parse(localStorage.getItem("rail_trains") || "[]");
    } catch (e) {
      return DEFAULT_TRAINS;
    }
  }

  static getBookings() {
    try {
      return JSON.parse(localStorage.getItem("rail_bookings") || "[]");
    } catch (e) {
      return DEFAULT_BOOKINGS;
    }
  }

  static saveBooking(booking) {
    const bookings = this.getBookings();
    bookings.unshift(booking);
    localStorage.setItem("rail_bookings", JSON.stringify(bookings));
  }

  static findBookingByPNR(pnr) {
    if (!pnr) return null;
    const cleanPnr = pnr.toString().trim().toUpperCase();
    const bookings = this.getBookings();
    return bookings.find(b => b.pnr.toString().trim().toUpperCase() === cleanPnr) || null;
  }

  static removeBookingByPNR(pnr) {
    if (!pnr) return false;
    const cleanPnr = pnr.toString().trim().toUpperCase();
    const bookings = this.getBookings();
    const initialLength = bookings.length;
    const filtered = bookings.filter(b => b.pnr.toString().trim().toUpperCase() !== cleanPnr);
    if (filtered.length < initialLength) {
      localStorage.setItem("rail_bookings", JSON.stringify(filtered));
      return true;
    }
    return false;
  }
}

// Current Session State
let currentUser = null;
let currentCancellationBooking = null;

// ==================== APP INITIALIZATION ====================
document.addEventListener("DOMContentLoaded", () => {
  Database.init();
  setQuickDate(1); // Default journey date to tomorrow
  renderTrainDirectory();

  // Check if session already exists
  const savedUser = sessionStorage.getItem("rail_session");
  if (savedUser) {
    try {
      currentUser = JSON.parse(savedUser);
      showApp();
    } catch (e) {
      sessionStorage.removeItem("rail_session");
    }
  }

  safeCreateIcons();
});

// ==================== AUTHENTICATION ====================
function handleLogin(event) {
  if (event) event.preventDefault();
  const usernameInput = document.getElementById("loginUsername").value.trim().toLowerCase();
  const passwordInput = document.getElementById("loginPassword").value;
  const alertEl = document.getElementById("loginAlert");

  const users = Database.getUsers();
  const user = users.find(u => u.username.toLowerCase() === usernameInput && u.password === passwordInput);

  if (user) {
    alertEl.classList.add("hidden");
    currentUser = user;
    sessionStorage.setItem("rail_session", JSON.stringify(user));
    showToast(`Welcome back, ${user.fullName}!`, "success");
    showApp();
  } else {
    alertEl.classList.remove("hidden");
    document.getElementById("loginAlertTitle").innerText = "Access Denied!";
    document.getElementById("loginAlertMessage").innerText = "Invalid username or password. Please use admin / admin123 or rohan / pass123.";
  }
}

function quickFillLogin(username, password) {
  document.getElementById("loginUsername").value = username;
  document.getElementById("loginPassword").value = password;
  document.getElementById("loginAlert").classList.add("hidden");
  handleLogin();
}

function handleLogout() {
  sessionStorage.removeItem("rail_session");
  currentUser = null;
  document.getElementById("loginSection").classList.remove("hidden");
  document.getElementById("appSection").classList.add("hidden");
  document.getElementById("loginAlert").classList.add("hidden");
  safeCreateIcons();
}

function showApp() {
  try {
    const loginSec = document.getElementById("loginSection");
    const appSec = document.getElementById("appSection");

    loginSec.classList.add("hidden");
    appSec.classList.remove("hidden");

    if (currentUser) {
      const nameEl = document.getElementById("navUserName");
      const roleEl = document.getElementById("navUserRole");
      if (nameEl) nameEl.innerText = currentUser.fullName || currentUser.username;
      if (roleEl) roleEl.innerText = currentUser.role || "USER";
    }

    renderBookingsTable();
    safeCreateIcons();
  } catch (err) {
    console.error("Error displaying app:", err);
    document.getElementById("loginSection").style.display = "none";
    document.getElementById("appSection").style.display = "flex";
  }
}

// ==================== TAB NAVIGATION ====================
function switchTab(tabId) {
  // Hide all contents
  document.querySelectorAll(".tab-content").forEach(el => el.classList.add("hidden"));
  // Remove active styling on all tab buttons
  document.querySelectorAll(".tab-btn").forEach(btn => {
    btn.classList.remove("tab-active", "border-rail-500", "text-rail-900");
    btn.classList.add("border-transparent", "text-slate-500");
  });

  // Show active tab
  const target = document.getElementById(tabId);
  if (target) target.classList.remove("hidden");

  // Highlight active button
  const activeBtnMap = {
    bookingTab: "btnBookingTab",
    cancelTab: "btnCancelTab",
    myBookingsTab: "btnMyBookingsTab",
    trainsTab: "btnTrainsTab"
  };
  const activeBtn = document.getElementById(activeBtnMap[tabId]);
  if (activeBtn) {
    activeBtn.classList.add("tab-active", "border-rail-500", "text-rail-900");
    activeBtn.classList.remove("border-transparent", "text-slate-500");
  }

  if (tabId === "myBookingsTab") {
    renderBookingsTable();
  }
  safeCreateIcons();
}

// ==================== TRAIN AUTO-POPULATION ====================
function handleTrainNumberInput(value) {
  const trainNo = parseInt(value.trim(), 10);
  const nameField = document.getElementById("resTrainName");
  const badge = document.getElementById("trainLookupBadge");
  const sourceField = document.getElementById("resSource");
  const destField = document.getElementById("resDestination");

  if (!trainNo || isNaN(trainNo)) {
    nameField.value = "";
    badge.innerText = "Type: 12001, 12951, 22436";
    badge.className = "text-[11px] text-slate-500 font-medium";
    return;
  }

  const trains = Database.getTrains();
  const found = trains.find(t => t.number === trainNo);

  if (found) {
    nameField.value = found.name;
    badge.innerText = `✓ Found: ${found.name}`;
    badge.className = "text-[11px] text-emerald-600 font-bold";

    // Auto-fill stations if empty
    if (!sourceField.value) sourceField.value = found.source;
    if (!destField.value) destField.value = found.destination;
  } else {
    nameField.value = "";
    badge.innerText = `✕ Train ${trainNo} not in directory`;
    badge.className = "text-[11px] text-red-500 font-medium";
  }
}

function selectTrainForBooking(train) {
  document.getElementById("resTrainNumber").value = train.number;
  document.getElementById("resTrainName").value = train.name;
  document.getElementById("resSource").value = train.source;
  document.getElementById("resDestination").value = train.destination;

  const badge = document.getElementById("trainLookupBadge");
  badge.innerText = `✓ Selected: ${train.name}`;
  badge.className = "text-[11px] text-emerald-600 font-bold";

  switchTab("bookingTab");
  closeTrainPickerModal();
  showToast(`Auto-populated train ${train.number} - ${train.name}!`);
}

function selectTrainByNumber(num) {
  const trains = Database.getTrains();
  const train = trains.find(t => t.number === num);
  if (train) {
    selectTrainForBooking(train);
  }
}

// ==================== RESERVATION BOOKING ====================
function handleReservationSubmit(event) {
  event.preventDefault();

  const passenger = document.getElementById("resPassengerName").value.trim();
  const trainNo = parseInt(document.getElementById("resTrainNumber").value.trim(), 10);
  const trainName = document.getElementById("resTrainName").value.trim();
  const classType = document.getElementById("resClassType").value;
  const journeyDate = document.getElementById("resJourneyDate").value;
  const source = document.getElementById("resSource").value.trim();
  const destination = document.getElementById("resDestination").value.trim();

  // Validations
  if (!passenger || passenger.length < 2) {
    alert("Please enter a valid Passenger Name (at least 2 characters).");
    return;
  }
  if (!trainNo || isNaN(trainNo)) {
    alert("Please enter a numeric Train Number.");
    return;
  }
  if (!trainName) {
    alert("Train Name is required. Please enter a valid train number.");
    return;
  }
  if (!journeyDate) {
    alert("Please select a valid Date of Journey.");
    return;
  }
  if (source.toLowerCase() === destination.toLowerCase()) {
    alert("Source and Destination stations cannot be identical.");
    return;
  }

  // Generate Unique 10-Digit PNR
  let pnr;
  do {
    pnr = Math.floor(1000000000 + Math.random() * 9000000000).toString();
  } while (Database.findBookingByPNR(pnr));

  const booking = {
    pnr: pnr,
    passengerName: passenger,
    trainNumber: trainNo,
    trainName: trainName,
    classType: classType,
    journeyDate: journeyDate,
    source: source,
    destination: destination,
    bookingDate: new Date().toLocaleString(),
    status: "CONFIRMED",
    bookedBy: currentUser ? currentUser.username : "guest"
  };

  // Save to Database
  Database.saveBooking(booking);

  // Show Confirmation Modal Dialog
  showConfirmationModal(booking);

  // Reset form
  resetReservationForm();
  renderBookingsTable();
}

function resetReservationForm() {
  document.getElementById("reservationForm").reset();
  setQuickDate(1);
  const badge = document.getElementById("trainLookupBadge");
  badge.innerText = "Type: 12001, 12951, 22436";
  badge.className = "text-[11px] text-slate-500 font-medium";
}

function setQuickDate(daysFromToday) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  document.getElementById("resJourneyDate").value = `${yyyy}-${mm}-${dd}`;
}

// ==================== CONFIRMATION MODAL ====================
function showConfirmationModal(booking) {
  document.getElementById("modalPnrValue").innerText = booking.pnr;
  document.getElementById("modalPassenger").innerText = booking.passengerName;
  document.getElementById("modalTrain").innerText = `${booking.trainNumber} - ${booking.trainName}`;
  document.getElementById("modalClass").innerText = booking.classType;
  document.getElementById("modalDate").innerText = booking.journeyDate;
  document.getElementById("modalRoute").innerText = `${booking.source} ➔ ${booking.destination}`;

  document.getElementById("confirmationModal").classList.remove("hidden");
  safeCreateIcons();
}

function viewBookingByPnr(pnr) {
  const booking = Database.findBookingByPNR(pnr);
  if (booking) {
    showConfirmationModal(booking);
  }
}

function closeConfirmationModal() {
  document.getElementById("confirmationModal").classList.add("hidden");
}

function copyPnrToClipboard() {
  const pnr = document.getElementById("modalPnrValue").innerText;
  navigator.clipboard.writeText(pnr).then(() => {
    showToast(`PNR ${pnr} copied to clipboard!`, "success");
  });
}

// ==================== CANCELLATION WORKFLOW ====================
function handleFetchPNR(event) {
  if (event) event.preventDefault();
  const pnr = document.getElementById("cancelPnrInput").value.trim();

  if (!pnr) {
    alert("Please enter a PNR number.");
    return;
  }

  const booking = Database.findBookingByPNR(pnr);
  const resultCard = document.getElementById("cancellationResultCard");
  const emptyState = document.getElementById("cancellationEmptyState");

  if (booking) {
    currentCancellationBooking = booking;
    document.getElementById("cancelCardPnr").innerText = booking.pnr;
    document.getElementById("cancelCardPassenger").innerText = booking.passengerName;
    document.getElementById("cancelCardTrain").innerText = `${booking.trainNumber} - ${booking.trainName}`;
    document.getElementById("cancelCardClass").innerText = booking.classType;
    document.getElementById("cancelCardDate").innerText = booking.journeyDate;
    document.getElementById("cancelCardRoute").innerText = `${booking.source} ➔ ${booking.destination}`;
    document.getElementById("cancelCardBookedBy").innerText = booking.bookedBy;

    emptyState.classList.add("hidden");
    resultCard.classList.remove("hidden");
    safeCreateIcons();
    showToast("Booking details retrieved successfully!", "success");
  } else {
    currentCancellationBooking = null;
    resultCard.classList.add("hidden");
    emptyState.classList.remove("hidden");
    alert(`❌ No reservation found for PNR: ${pnr}. Please verify the number.`);
  }
}

function promptConfirmCancellation() {
  if (!currentCancellationBooking) return;
  document.getElementById("cancelDialogPnr").innerText = currentCancellationBooking.pnr;
  document.getElementById("cancelConfirmModal").classList.remove("hidden");
  safeCreateIcons();
}

function closeCancelConfirmModal() {
  document.getElementById("cancelConfirmModal").classList.add("hidden");
}

function executeCancellation() {
  if (!currentCancellationBooking) return;
  const pnr = currentCancellationBooking.pnr;

  const removed = Database.removeBookingByPNR(pnr);
  closeCancelConfirmModal();

  if (removed) {
    document.getElementById("cancellationResultCard").classList.add("hidden");
    document.getElementById("cancellationEmptyState").classList.remove("hidden");
    document.getElementById("cancelPnrInput").value = "";
    currentCancellationBooking = null;

    showToast(`Reservation PNR ${pnr} cancelled and permanently removed from database!`, "success");
    renderBookingsTable();
  } else {
    alert("Failed to remove reservation from database.");
  }
}

function navigateToCancel(pnr) {
  switchTab("cancelTab");
  document.getElementById("cancelPnrInput").value = pnr;
  handleFetchPNR();
}

// ==================== ALL BOOKINGS TABLE ====================
function renderBookingsTable(filterQuery = "") {
  let bookings = Database.getBookings();

  // If regular user, filter to user bookings; if admin, show all
  const noteEl = document.getElementById("bookingsFilterNote");
  if (currentUser && currentUser.role !== "ADMIN") {
    bookings = bookings.filter(b => b.bookedBy === currentUser.username);
    if (noteEl) noteEl.innerText = `Displaying reservations booked by ${currentUser.fullName} (${currentUser.username}).`;
  } else {
    if (noteEl) noteEl.innerText = `Displaying all system reservations (Admin Mode).`;
  }

  // Search filter
  if (filterQuery) {
    const q = filterQuery.toLowerCase();
    bookings = bookings.filter(b =>
      b.pnr.toLowerCase().includes(q) ||
      b.passengerName.toLowerCase().includes(q) ||
      b.trainName.toLowerCase().includes(q)
    );
  }

  const countBadge = document.getElementById("bookingCountBadge");
  if (countBadge) countBadge.innerText = bookings.length;

  const tbody = document.getElementById("bookingsTableBody");
  const empty = document.getElementById("bookingsTableEmpty");
  if (!tbody) return;

  tbody.innerHTML = "";

  if (bookings.length === 0) {
    if (empty) empty.classList.remove("hidden");
    return;
  }
  if (empty) empty.classList.add("hidden");

  bookings.forEach(b => {
    const tr = document.createElement("tr");
    tr.className = "hover:bg-slate-50 transition border-b border-slate-100";
    tr.innerHTML = `
      <td class="py-3 px-4 font-mono font-bold text-rail-900">${b.pnr}</td>
      <td class="py-3 px-4 font-semibold text-slate-800">${b.passengerName}</td>
      <td class="py-3 px-4">
        <span class="font-bold text-slate-900">${b.trainNumber}</span> - ${b.trainName}
      </td>
      <td class="py-3 px-4">${b.classType}</td>
      <td class="py-3 px-4 font-medium">${b.journeyDate}</td>
      <td class="py-3 px-4 text-slate-600">${b.source} ➔ ${b.destination}</td>
      <td class="py-3 px-4 text-center">
        <span class="px-2 py-0.5 bg-emerald-100 text-emerald-800 text-[10px] font-bold rounded-full border border-emerald-200">
          ${b.status}
        </span>
      </td>
      <td class="py-3 px-4 text-right space-x-1">
        <button onclick="viewBookingByPnr('${b.pnr}')" 
          class="p-1 hover:bg-blue-50 text-blue-600 rounded" title="View Ticket">
          <i data-lucide="eye" class="w-4 h-4"></i>
        </button>
        <button onclick="navigateToCancel('${b.pnr}')" 
          class="p-1 hover:bg-red-50 text-red-600 rounded" title="Cancel Booking">
          <i data-lucide="trash-2" class="w-4 h-4"></i>
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  safeCreateIcons();
}

function filterBookingsTable(query) {
  renderBookingsTable(query.trim());
}

// ==================== TRAIN DIRECTORY ====================
function renderTrainDirectory(filterQuery = "") {
  let trains = Database.getTrains();
  if (filterQuery) {
    const q = filterQuery.toLowerCase();
    trains = trains.filter(t =>
      t.number.toString().includes(q) ||
      t.name.toLowerCase().includes(q) ||
      t.source.toLowerCase().includes(q) ||
      t.destination.toLowerCase().includes(q)
    );
  }

  const tbody = document.getElementById("trainDirectoryBody");
  if (!tbody) return;
  tbody.innerHTML = "";

  trains.forEach(t => {
    const tr = document.createElement("tr");
    tr.className = "hover:bg-slate-50 transition border-b border-slate-100";
    tr.innerHTML = `
      <td class="py-3 px-4 font-mono font-bold text-rail-900 text-center">${t.number}</td>
      <td class="py-3 px-4 font-semibold text-slate-800">${t.name}</td>
      <td class="py-3 px-4 text-slate-600">${t.source}</td>
      <td class="py-3 px-4 text-slate-600">${t.destination}</td>
      <td class="py-3 px-4 text-right">
        <button onclick="selectTrainByNumber(${t.number})"
          class="px-2.5 py-1 bg-rail-50 hover:bg-rail-100 text-rail-700 font-semibold text-xs rounded-lg border border-rail-200 transition">
          Book This Train
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  safeCreateIcons();
}

function filterTrainDirectory(query) {
  renderTrainDirectory(query.trim());
}

// ==================== TRAIN PICKER MODAL ====================
function openTrainPickerModal() {
  const container = document.getElementById("pickerListContainer");
  const trains = Database.getTrains();
  container.innerHTML = "";

  trains.forEach(t => {
    const div = document.createElement("div");
    div.className = "p-3 hover:bg-slate-50 cursor-pointer flex items-center justify-between transition";
    div.onclick = () => selectTrainByNumber(t.number);
    div.innerHTML = `
      <div>
        <span class="font-bold font-mono text-rail-900">${t.number}</span>
        <span class="font-semibold text-slate-800 ml-2">${t.name}</span>
        <p class="text-xs text-slate-500 mt-0.5">${t.source} ➔ ${t.destination}</p>
      </div>
      <span class="text-xs text-rail-600 font-semibold">Select ➔</span>
    `;
    container.appendChild(div);
  });

  document.getElementById("trainPickerModal").classList.remove("hidden");
  safeCreateIcons();
}

function closeTrainPickerModal() {
  document.getElementById("trainPickerModal").classList.add("hidden");
}

function filterPickerList(query) {
  const q = query.toLowerCase();
  const trains = Database.getTrains().filter(t =>
    t.number.toString().includes(q) || t.name.toLowerCase().includes(q) || t.source.toLowerCase().includes(q) || t.destination.toLowerCase().includes(q)
  );

  const container = document.getElementById("pickerListContainer");
  container.innerHTML = "";
  trains.forEach(t => {
    const div = document.createElement("div");
    div.className = "p-3 hover:bg-slate-50 cursor-pointer flex items-center justify-between transition";
    div.onclick = () => selectTrainByNumber(t.number);
    div.innerHTML = `
      <div>
        <span class="font-bold font-mono text-rail-900">${t.number}</span>
        <span class="font-semibold text-slate-800 ml-2">${t.name}</span>
        <p class="text-xs text-slate-500 mt-0.5">${t.source} ➔ ${t.destination}</p>
      </div>
      <span class="text-xs text-rail-600 font-semibold">Select ➔</span>
    `;
    container.appendChild(div);
  });
}

// ==================== USER REGISTRATION ====================
function openRegisterModal() {
  document.getElementById("registerModal").classList.remove("hidden");
  safeCreateIcons();
}

function closeRegisterModal() {
  document.getElementById("registerModal").classList.add("hidden");
}

function handleRegisterSubmit(event) {
  event.preventDefault();
  const fullName = document.getElementById("regFullName").value.trim();
  const username = document.getElementById("regUsername").value.trim().toLowerCase();
  const password = document.getElementById("regPassword").value;
  const confirmPassword = document.getElementById("regConfirmPassword").value;

  if (password !== confirmPassword) {
    alert("Passwords do not match!");
    return;
  }

  const users = Database.getUsers();
  if (users.some(u => u.username.toLowerCase() === username)) {
    alert("Username already exists. Please choose another.");
    return;
  }

  const newUser = { username, password, fullName, role: "USER" };
  Database.saveUser(newUser);

  alert("Account registered successfully! You can now log in.");
  closeRegisterModal();
  quickFillLogin(username, password);
}

// ==================== TOAST NOTIFICATIONS ====================
function showToast(message, type = "info") {
  const toast = document.getElementById("toastNotification");
  const msgEl = document.getElementById("toastMessage");
  if (!toast || !msgEl) return;

  msgEl.innerText = message;
  toast.classList.remove("translate-y-20", "opacity-0");

  setTimeout(() => {
    toast.classList.add("translate-y-20", "opacity-0");
  }, 3500);
}
