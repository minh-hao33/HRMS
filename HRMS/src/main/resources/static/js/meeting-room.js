/**
 * Meeting Room Booking System - Modular Implementation
 *
 * This implementation breaks down the original monolithic code into
 * modular components with clear responsibilities:
 *
 * 1. ApiService - Handles all API calls
 * 2. DateTimeUtils - Date/time manipulation utilities
 * 3. MeetingRoomUI - Manages UI components
 * 4. NotificationService - Shows notifications
 * 5. MeetingGridRenderer - Renders the time grid and events
 * 6. BookingFormHandler - Manages booking form operations
 * 7. MeetingRoomApp - Main application controller
 */

// Constants
const BASE_URL = "http://localhost:8080";
const API_URL = `${BASE_URL}/api/v1/meeting-room`;

/**
 * API Service - Handles all API communication
 */
class ApiService {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.meetingRoomUrl = `${baseUrl}/api/v1/meeting-room`;
    this.bookingsUrl = `${baseUrl}/api/v1/bookings`;
  }

  /**
   * Fetch meeting rooms with optional filters
   */
  async fetchMeetingRooms(filters = {}) {
    // Build query parameters
    const queryParams = new URLSearchParams();

    // Add pagination params
    queryParams.append('pageSize', 100);
    queryParams.append('pageNo', 1);

    // Add filters
    if (filters.roomId) queryParams.append('roomId', filters.roomId);
    if (filters.duration) queryParams.append('duration', filters.duration);
    if (filters.capacity) queryParams.append('capacity', filters.capacity);
    if (filters.location) queryParams.append('location', filters.location);
    if (filters.roomName) queryParams.append('roomName', filters.roomName);

    // Handle date, startTime and endTime
    if (filters.date) {
      if (!filters.startTime) {
        const startOfDay = `${filters.date}T00:00:00`;
        queryParams.append('startTime', startOfDay);
      }
      if (!filters.endTime) {
        const endOfDay = `${filters.date}T23:59:59`;
        queryParams.append('endTime', endOfDay);
      }
    }

    if (filters.startTime) queryParams.append('startTime', filters.startTime);
    if (filters.endTime) queryParams.append('endTime', filters.endTime);

    try {
      const response = await fetch(`${this.meetingRoomUrl}?${queryParams.toString()}`);

      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching meeting rooms:', error);
      return null;
    }
  }

  /**
   * Create a new booking
   */
  async createBooking(bookingData) {
    try {
      const response = await fetch(this.bookingsUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bookingData)
      });

      return await response.json();
    } catch (error) {
      console.error('Error creating booking:', error);
      throw error;
    }
  }

  /**
   * Update an existing booking
   */
  async updateBooking(bookingId, bookingData) {
    try {
      const response = await fetch(`${this.bookingsUrl}/${bookingId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(bookingData)
      });

      return await response.json();
    } catch (error) {
      console.error('Error updating booking:', error);
      throw error;
    }
  }

  /**
   * Delete a booking
   */
  async deleteBooking(bookingId) {
    try {
      const response = await fetch(`${this.bookingsUrl}/${bookingId}`, {
        method: 'DELETE'
      });

      return await response.json();
    } catch (error) {
      console.error('Error deleting booking:', error);
      throw error;
    }
  }

  /**
   * Get booking details
   */
  async getBookingDetails(bookingId) {
    try {
      const response = await fetch(`${this.bookingsUrl}/${bookingId}`);

      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching booking details:', error);
      throw error;
    }
  }

  /**
   * Logout user
   */
  async logout() {
    try {
      const response = await fetch(`${this.baseUrl}/api/v1/users/logout`, {
        method: 'POST',
        credentials: 'include',
      });

      return response.ok;
    } catch (error) {
      console.error('Logout error:', error);
      return false;
    }
  }
}

/**
 * Date and Time Utilities
 */
class DateTimeUtils {
  /**
   * Convert UTC date string to local date object
   */
  static utcToLocal(utcDateString) {
    if (!utcDateString) return null;

    // Ensure string has timezone info
    if (!utcDateString.endsWith('Z') && !utcDateString.includes('+')) {
      utcDateString += 'Z'; // Add 'Z' to specify UTC
    }

    return new Date(utcDateString);
  }

  /**
   * Convert local date to UTC string
   */
  static localToUtc(localDate) {
    if (!localDate) return null;

    // If string, convert to Date object
    if (typeof localDate === 'string') {
      localDate = new Date(localDate);
    }

    return new Date(
        localDate.getTime() - (localDate.getTimezoneOffset() * 60000)
    ).toISOString();
  }

  /**
   * Format date for display
   */
  static formatDate(date, format = 'YYYY-MM-DD') {
    if (!date) return '';

    if (typeof date === 'string') {
      date = new Date(date);
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    switch (format) {
      case 'YYYY-MM-DD':
        return `${year}-${month}-${day}`;
      case 'HH:MM':
        return `${hours}:${minutes}`;
      case 'YYYY-MM-DD HH:MM':
        return `${year}-${month}-${day} ${hours}:${minutes}`;
      case 'ISO':
        return date.toISOString();
      case 'datetime-local':
        return `${year}-${month}-${day}T${hours}:${minutes}`;
      default:
        return date.toString();
    }
  }

  /**
   * Parse time string (HH:MM) into object with hour and minute
   */
  static parseTimeString(timeStr) {
    if (!timeStr || !timeStr.includes(':')) return null;

    const [hours, minutes] = timeStr.split(':').map(Number);
    return { hour: hours, minute: minutes };
  }

  /**
   * Calculate end time from start time and duration
   */
  static calculateEndTime(startTime, durationMinutes) {
    if (!startTime) return '';

    let hours, minutes;
    if (startTime.includes('T')) {
      // Handle ISO format
      const startDate = new Date(startTime);
      hours = startDate.getHours();
      minutes = startDate.getMinutes();
    } else {
      // Handle HH:MM format
      [hours, minutes] = startTime.split(':').map(Number);
    }

    const totalMinutes = hours * 60 + minutes + durationMinutes;
    const endHours = Math.floor(totalMinutes / 60);
    const endMinutes = totalMinutes % 60;

    return `${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  }

  /**
   * Convert time string to minutes for comparison
   */
  static convertToMinutes(timeStr) {
    if (!timeStr) return 0;

    if (timeStr.includes('T')) {
      // Handle ISO date string
      return new Date(timeStr).getHours() * 60 + new Date(timeStr).getMinutes();
    } else {
      // Handle HH:MM format
      const [hours, minutes] = timeStr.split(':').map(Number);
      return hours * 60 + minutes;
    }
  }

  /**
   * Format date in local timezone (YYYY-MM-DD HH:MM:SS)
   */
  static formatLocalDateTime(date) {
    if (!date) return '';

    // If already a formatted string, return it
    if (typeof date === 'string' && date.includes('-') && date.includes(':')) {
      return date;
    }

    // Convert to Date object
    const dateObj = new Date(date);
    if (isNaN(dateObj.getTime())) return '';

    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    const seconds = String(dateObj.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }

  /**
   * Check if time is in the past
   */
  static isPastTime(hour, minute) {
    const now = new Date();
    const currentDate = document.getElementById('date-input')?.value;

    if (!currentDate) {
      return false; // If no date selected, default to allow booking
    }

    // Create Date objects for comparison
    const selectedDate = new Date(currentDate);
    const today = new Date();

    // Reset time portions to compare just the dates
    const selectedDateOnly = new Date(selectedDate.setHours(0, 0, 0, 0));
    const todayOnly = new Date(today.setHours(0, 0, 0, 0));

    // If selected date is in the past, all times are "past"
    if (selectedDateOnly < todayOnly) {
      return true;
    }

    // If selected date is in the future, no times are "past"
    if (selectedDateOnly > todayOnly) {
      return false;
    }

    // For current date, compare hours and minutes
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    // Check if the time is in the past
    if (currentHour > hour) {
      return true;
    } else if (currentHour === hour && currentMinute >= minute) {
      return true;
    }

    return false;
  }

  /**
   * Combine date and time strings into a single datetime string
   */
  static combineDateAndTime(dateStr, timeStr) {
    if (!dateStr || !timeStr) return '';

    // Extract date part from datetime-local string
    const datePart = dateStr.split('T')[0];
    // Combine with time
    return `${datePart}T${timeStr}`;
  }
}

/**
 * Notification Service
 */
class NotificationService {
  constructor() {
    this.initStyles();
  }

  /**
   * Initialize notification styles
   */
  initStyles() {
    if (!document.getElementById('notification-styles')) {
      const style = document.createElement('style');
      style.id = 'notification-styles';
      style.innerHTML = `
        .notification {
          position: fixed;
          bottom: 20px;
          right: 20px;
          background-color: #444;
          color: white;
          padding: 10px 20px;
          border-radius: 5px;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
          z-index: 1000;
          opacity: 0.9;
          transition: opacity 0.3s ease;
        }
        .notification.success {
          background-color: #4caf50;
        }
        .notification.error {
          background-color: #f44336;
        }
        .notification.info {
          background-color: #2196f3;
        }
        .notification.warning {
          background-color: #ff9800;
        }
      `;
      document.head.appendChild(style);
    }
  }

  /**
   * Show a notification message
   */
  show(message, type = 'info', duration = 4000) {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.innerText = message;

    // Remove existing notifications of the same type
    document.querySelectorAll(`.notification.${type}`).forEach(el => el.remove());

    document.body.appendChild(notification);

    setTimeout(() => {
      notification.style.opacity = '0';
      setTimeout(() => notification.remove(), 300);
    }, duration);
  }

  /**
   * Show a modal notification
   */
  showModal(message, title = 'Notification', isConfirmation = false) {
    const modalTitle = document.getElementById("modal-title");
    const modalMessage = document.getElementById("modal-message");
    const icon = document.querySelector(".modal-box i");
    const section = document.querySelector("section");

    if (!modalTitle || !modalMessage || !icon || !section) {
      console.error('Modal elements not found');
      this.show(message, 'info');
      return;
    }

    modalTitle.textContent = isConfirmation ? "Confirmation" : title;
    modalMessage.textContent = message;
    icon.className = isConfirmation ? "fa-regular fa-circle-check" : "fa-solid fa-triangle-exclamation";
    icon.style.color = isConfirmation ? "#4caf50" : "#f5a623";
    section.classList.add("active");
  }
}

/**
 * Meeting Room UI Manager
 */
class MeetingRoomUI {
  constructor(dateTimeUtils, notificationService) {
    this.dateTimeUtils = dateTimeUtils;
    this.notifications = notificationService;
    this.startHour = 8;
    this.endHour = 18;
    this.cachedRooms = [];
    this.meetingData = [];
    this.currentRoomIndex = 0;
  }

  /**
   * Initialize UI elements and event listeners
   */
  init() {
    this.initEventListeners();
    this.setDefaultDate();
    this.updateTimeInputLabels();
  }

  /**
   * Set up all event listeners
   */
  initEventListeners() {
    // Filter button click
    document.querySelector('.btn-filter')?.addEventListener('click', () => {
      this.handleFilterButtonClick();
    });

    // Reset button click
    document.querySelector('.btn-reset')?.addEventListener('click', () => {
      this.handleResetButtonClick();
    });

    // Navigation arrows
    document.querySelector('.av-left')?.addEventListener('click', () => {
      if (this.currentRoomIndex > 0) {
        this.currentRoomIndex--;
        this.updateRoomScrollPosition();
      }
    });

    document.querySelector('.av-right')?.addEventListener('click', () => {
      const totalRooms = document.querySelectorAll('.room-card').length;
      if (this.currentRoomIndex < totalRooms - 1) {
        this.currentRoomIndex++;
        this.updateRoomScrollPosition();
      }
    });

    // Date input change
    document.querySelector('.date-input')?.addEventListener('change', (e) => {
      this.handleDateChange(e.target.value);
    });

    // Close buttons for notification modal
    document.querySelector('.close-btn')?.addEventListener('click', () => {
      document.querySelector('section').classList.remove('active');
    });

    document.querySelector('.overlay')?.addEventListener('click', () => {
      document.querySelector('section').classList.remove('active');
    });

    // Modal close buttons
    document.querySelectorAll('#bookingModal .close, #bookingModal .btn-secondary').forEach(el => {
      el.addEventListener('click', () => {
        this.closeModal('bookingModal');
      });
    });

    document.querySelectorAll('#createBookingModal .close, #createBookingModal .btn-secondary').forEach(el => {
      el.addEventListener('click', () => {
        this.closeModal('createBookingModal');
      });
    });

    // Duration calculation when start/end time changes
    const startTimeInput = document.getElementById('startTime');
    const endTimeInput = document.getElementById('endTime');

    if (startTimeInput && endTimeInput) {
      [startTimeInput, endTimeInput].forEach(input => {
        input.addEventListener('change', () => this.updateDurationFromTimes());
      });
    }
  }

  /**
   * Update duration dropdown based on start and end times
   */
  updateDurationFromTimes() {
    const startTimeInput = document.getElementById('startTime');
    const endTimeInput = document.getElementById('endTime');
    const durationSelect = document.getElementById('duration');

    if (!startTimeInput || !endTimeInput || !durationSelect) return;

    const startTimeValue = startTimeInput.value;
    const endTimeValue = endTimeInput.value;
    const dateValue = document.getElementById('date-input')?.value ||
        new Date().toISOString().split('T')[0];

    if (startTimeValue && endTimeValue) {
      try {
        // Parse times
        const startDate = new Date(`${dateValue}T${startTimeValue}`);
        const endDate = new Date(`${dateValue}T${endTimeValue}`);

        // Calculate duration in minutes
        const durationMinutes = Math.round((endDate - startDate) / (1000 * 60));

        if (durationMinutes > 0) {
          // Format for display
          let displayDuration;

          if (durationMinutes < 60) {
            displayDuration = `${durationMinutes}'`;
          } else if (durationMinutes % 60 === 0) {
            displayDuration = `${durationMinutes / 60}h`;
          } else {
            const hours = Math.floor(durationMinutes / 60);
            const minutes = durationMinutes % 60;
            displayDuration = `${hours}h ${minutes}'`;
          }

          // Update dropdown - create a new option if needed
          let found = false;
          for (let i = 0; i < durationSelect.options.length; i++) {
            if (durationSelect.options[i].value === displayDuration) {
              durationSelect.selectedIndex = i;
              found = true;
              break;
            }
          }

          if (!found) {
            const option = document.createElement('option');
            option.value = displayDuration;
            option.text = displayDuration;
            durationSelect.add(option);
            durationSelect.value = displayDuration;
          }
        } else if (durationMinutes < 0) {
          this.notifications.show('End time must be after start time', 'error');
        }
      } catch (error) {
        console.error('Error calculating duration:', error);
      }
    }
  }

  /**
   * Handle filter button click
   */
  handleFilterButtonClick() {
    // Get filter values
    const dateValue = document.getElementById('date-input')?.value;
    const startTimeValue = document.getElementById('startTime')?.value;
    const endTimeValue = document.getElementById('endTime')?.value;
    const durationValue = document.getElementById('duration')?.value;
    const capacityValue = document.querySelector('.seats-input')?.value;
    const locationValue = document.querySelector('.location-input')?.value;
    const searchValue = document.querySelector('.search-input')?.value;

    // Create filters object
    const filters = {
      date: dateValue,
      capacity: capacityValue !== 'Select' ? parseInt(capacityValue) : null
    };

    // Process start/end times
    if (dateValue && startTimeValue) {
      filters.startTime = `${dateValue}T${startTimeValue}`;
    }

    if (dateValue && endTimeValue) {
      filters.endTime = `${dateValue}T${endTimeValue}`;
    }

    // Process location and room name
    if (locationValue && locationValue !== 'Select') {
      if (locationValue.includes(' - ')) {
        const parts = locationValue.split(' - ');
        filters.roomName = parts[0].trim();
        filters.location = parts[1].trim();
      } else {
        filters.location = locationValue.trim();
      }
    }

    // Handle search by room name
    if (searchValue) {
      filters.roomName = searchValue.trim();
    }

    // Handle duration without start/end time
    if (durationValue && durationValue !== 'Select' && !startTimeValue && !endTimeValue) {
      const now = new Date();
      filters.startTime = `${dateValue}T${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

      let durationMinutes = 0;
      if (durationValue.includes('h')) {
        durationMinutes = parseInt(durationValue) * 60;
      } else if (durationValue.includes("'")) {
        durationMinutes = parseInt(durationValue.replace("'", ""));
      } else {
        durationMinutes = parseInt(durationValue);
      }

      const endDateTime = new Date(now.getTime() + durationMinutes * 60000);
      filters.endTime = `${dateValue}T${endDateTime.getHours().toString().padStart(2, '0')}:${endDateTime.getMinutes().toString().padStart(2, '0')}`;
    }

    console.log("Applied filters:", filters);
    // Signal to fetch with these filters
    document.dispatchEvent(new CustomEvent('fetchMeetingRooms', { detail: filters }));
  }

  /**
   * Handle reset button click
   */
  handleResetButtonClick() {
    // Reset form inputs
    const dateInput = document.getElementById('date-input');
    if (dateInput) {
      dateInput.value = new Date().toISOString().split('T')[0];
    }

    const fieldsToReset = [
      'startTime', 'endTime', 'duration',
      '.seats-input', '.location-input', '.search-input'
    ];

    fieldsToReset.forEach(selector => {
      const el = document.querySelector(selector);
      if (el) {
        el.value = selector.includes('input') ? 'Select' : '';
      }
    });

    // Fetch with default date
    document.dispatchEvent(new CustomEvent('fetchMeetingRooms', {
      detail: { date: dateInput?.value || new Date().toISOString().split('T')[0] }
    }));
  }

  /**
   * Handle date change
   */
  handleDateChange(dateValue) {
    document.dispatchEvent(new CustomEvent('fetchMeetingRooms', {
      detail: { date: dateValue }
    }));
  }

  /**
   * Set default date to today
   */
  setDefaultDate() {
    const dateInput = document.getElementById('date-input');
    if (dateInput) {
      dateInput.value = new Date().toISOString().split('T')[0];
    }
  }

  /**
   * Update placeholders for time inputs
   */
  updateTimeInputLabels() {
    const startTime = document.getElementById('startTime');
    const endTime = document.getElementById('endTime');

    if (startTime) startTime.placeholder = 'HH:MM';
    if (endTime) endTime.placeholder = 'HH:MM';
  }

  /**
   * Close a modal by ID
   */
  closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal && typeof bootstrap !== 'undefined') {
      const bsModal = bootstrap.Modal.getInstance(modal);
      if (bsModal) {
        bsModal.hide();
      }
    } else if (modal && typeof jQuery !== 'undefined') {
      // Fallback to jQuery
      jQuery(modal).modal('hide');
    }
  }

  /**
   * Update room scroll position
   */
  updateRoomScrollPosition() {
    const roomCards = document.querySelectorAll('.room-card');
    const totalRooms = roomCards.length;

    // Ensure current index is within bounds
    if (this.currentRoomIndex < 0) this.currentRoomIndex = 0;
    if (this.currentRoomIndex >= totalRooms) this.currentRoomIndex = totalRooms - 1;

    // Update room list scroll position
    const roomList = document.querySelector('.room-list');
    if (roomList) {
      const scrollPosition = this.currentRoomIndex * 215; // 200px width + 15px margin
      roomList.scrollTo({ left: scrollPosition, behavior: 'smooth' });
    }

    // Update grid scroll position
    const scheduleContainer = document.querySelector('.schedule-container');
    if (scheduleContainer) {
      const gridScrollPosition = this.currentRoomIndex * 200; // 200px column width
      scheduleContainer.scrollTo({ left: gridScrollPosition, behavior: 'smooth' });
    }

    // Update selected room
    roomCards.forEach((card, index) => {
      if (index === this.currentRoomIndex) {
        card.classList.add('selected-room');
      } else {
        card.classList.remove('selected-room');
      }
    });
  }

  /**
   * Update the room list display
   */
  updateRoomList(rooms) {
    const roomList = document.getElementById('roomList');
    if (!roomList) return;

    roomList.innerHTML = '';

    // Store rooms in cache
    this.cachedRooms = rooms;

    // Create room cards
    rooms.forEach((room, index) => {
      const isSelected = index === 0 ? 'selected-room' : '';
      const roomStatus = room.status || 'Available';

      const roomCard = document.createElement('div');
      roomCard.className = `room-card ${isSelected}`;
      roomCard.dataset.room = room.room;
      roomCard.innerHTML = `
        <div class="room-header">
          <div class="room-header-top">
            <div class="room-icon">
              <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAcCSURBVHgBzZgJbJRFFIBnt7+ItqGVFrT3sRViRSxHjAJCMRxR0YBGMIJgkQQRSBQQkWiknJIIeNAgoLQJmGgwUYNIgCKXSgpUrpRDe0Hb1FLSbqXtlrZ0/d7uv/jz828rtFC+5O17/8y8Y97MezOzNtUOEBsbOxEyE3zI7XZ3sdlsx5ubmzcWFxdnqjaCTbURYmJiNuPQJNgMnDpgt9s1nBxC2xRoJt8zioqK6lVHQFxc3CIcvBwdHf2kuY/2h4msE/xAdQQkJCQEY7wKXOhvDE5+KGOYSIi6RbCrWwSWsx8kBJrpb0xAQMAXMoalHqRuEew32X4NcEwT6nA4LvobU1hYWA65CnRXrYNlPmg+Jj4+/lGMLoJ9AryfWeey0XfW19evLC8vt3KiTH7y8vL6Qw5bKdd1BqDnrFW" alt="room icon" width="40" height="40">
            </div>
            <h4 class="room-name">${room.roomName} - ${room.location}</h4>
          </div>
          <div class="room-info text-neutral-8 text-sm">
            <span>${room.capacity} seats</span>
          </div>
          <div class="room-info">
            <span>${roomStatus}</span>
          </div>
        </div>
      `;

      roomList.appendChild(roomCard);
    });

    // Add click event listeners to room cards
    document.querySelectorAll('.room-card').forEach(card => {
      card.addEventListener('click', () => {
        // Remove selected class from all cards
        document.querySelectorAll('.room-card').forEach(c =>
            c.classList.remove('selected-room'));

        // Add selected class to clicked card
        card.classList.add('selected-room');

        // Get room ID and scroll to show corresponding column
        const roomId = card.dataset.room;
        const roomIndex = Array.from(document.querySelectorAll('.room-card'))
        .findIndex(c => c === card);

        if (roomIndex !== -1) {
          this.currentRoomIndex = roomIndex;
          this.updateRoomScrollPosition();
        }
      });
    });
  }

  /**
   * Populate filter dropdowns with data
   */
  populateFilters(roomData) {
    if (!roomData || !roomData.length) return;

    // Get capacity values and sort
    const capacities = [...new Set(roomData
    .map(room => room.capacity)
    .filter(capacity => capacity !== null && capacity !== undefined))]
    .sort((a, b) => a - b);

    // Get unique location combinations
    const locationMap = {};
    roomData.forEach(room => {
      if (room.roomName && room.location) {
        const key = `${room.roomName} - ${room.location}`;
        if (!locationMap[key]) {
          locationMap[key] = {
            value: key,
            roomName: room.roomName,
            location: room.location
          };
        }
      }
    });
    const locations = Object.values(locationMap);

    // Update capacity dropdown
    const capacitySelect = document.querySelector('.seats-input');
    if (capacitySelect) {
      // Clear existing options except first
      Array.from(capacitySelect.options)
      .slice(1)
      .forEach(option => capacitySelect.removeChild(option));

      // Add new options
      capacities.forEach(capacity => {
        const option = document.createElement('option');
        option.value = capacity;
        option.textContent = `${capacity} seats`;
        capacitySelect.appendChild(option);
      });
    }

    // Update location dropdown
    const locationSelect = document.querySelector('.location-input');
    if (locationSelect) {
      // Clear existing options except first
      Array.from(locationSelect.options)
      .slice(1)
      .forEach(option => locationSelect.removeChild(option));

      // Add new options
      locations.forEach(loc => {
        const option = document.createElement('option');
        option.value = loc.value;
        option.textContent = loc.value;
        locationSelect.appendChild(option);
      });
    }
  }
}

/**
 * Meeting Grid Renderer
 * Handles rendering the time grid and meeting events
 */
class MeetingGridRenderer {
  constructor(startHour = 8, endHour = 18) {
    this.startHour = startHour;
    this.endHour = endHour;
    this.dateTimeUtils = new DateTimeUtils();
  }

  /**
   * Generate the time column (hours)
   */
  generateTimeColumn() {
    const timeColumn = document.getElementById('timeColumn');
    if (!timeColumn) return;

    timeColumn.innerHTML = '';

    for (let hour = this.startHour; hour <= this.endHour; hour++) {
      const formattedHour = hour.toString().padStart(2, '0') + ':00';
      const timeSlot = document.createElement('div');
      timeSlot.className = 'time-slot';
      timeSlot.textContent = formattedHour;
      timeColumn.appendChild(timeSlot);
    }
  }

  /**
   * Generate the grid for each room
   */
  generateRoomGrid(roomsToShow, meetingData) {
    const gridContainer = document.getElementById('gridContainer');
    if (!gridContainer) return;

    gridContainer.innerHTML = '';

    // If no rooms provided, create default rooms
    if (!roomsToShow || roomsToShow.length === 0) {
      roomsToShow = this.createDefaultRooms();
    }

    // Create a column for each room
    roomsToShow.forEach(room => {
      const roomColumn = document.createElement('div');
      roomColumn.className = 'room-column';
      roomColumn.dataset.room = room.room;

      const eventGrid = document.createElement('div');
      eventGrid.className = 'event-grid';

      // Create time cells (15 min intervals)
      for (let hour = this.startHour; hour <= this.endHour; hour++) {
        for (let minute = 0; minute < 60; minute += 15) {
          const isHourMarker = minute === 0;
          const isPast = DateTimeUtils.isPastTime(hour, minute);

          const timeCell = document.createElement('div');
          timeCell.className = `time-cell ${isHourMarker ? 'hour-marker' : ''} ${isPast ? 'past-time' : ''}`;
          timeCell.dataset.time = `${hour}:${minute.toString().padStart(2, '0')}`;

          eventGrid.appendChild(timeCell);
        }
      }

      roomColumn.appendChild(eventGrid);
      gridContainer.appendChild(roomColumn);
    });

    // Add meetings to the grid
    this.addMeetingsToGrid(meetingData);

    // Add event listeners for time cells (for creating new meetings)
    this.addNewEventHandlers();

    // Add current time indicator
    this.addCurrentTimeIndicator();
  }

  /**
   * Add meetings to the grid
   */
  addMeetingsToGrid(meetingData) {
    if (!meetingData || !meetingData.length) return;

    // Get selected date
    const selectedDate = document.getElementById('date-input')?.value ||
        new Date().toISOString().split('T')[0];

    // Group meetings by room
    const meetingsByRoom = {};
    meetingData.forEach(meeting => {
      if (!meeting.room) return;

      if (!meetingsByRoom[meeting.room]) {
        meetingsByRoom[meeting.room] = [];
      }

      meetingsByRoom[meeting.room].push(meeting);
    });

    // Process each room
    Object.keys(meetingsByRoom).forEach(roomId => {
      const roomMeetings = meetingsByRoom[roomId];

      // Sort meetings by start time
      roomMeetings.sort((a, b) => {
        const timeA = DateTimeUtils.convertToMinutes(a.startTime);
        const timeB = DateTimeUtils.convertToMinutes(b.startTime);
        return timeA - timeB;
      });

      // Clear existing meetings
      document.querySelectorAll(`.room-column[data-room="${roomId}"] .event-grid .meeting-event`)
      .forEach(el => el.remove());

      // Add each meeting
      roomMeetings.forEach(meeting => {
        // Check if meeting is for selected date
        if (meeting.startDateTime) {
          const meetingDate = new Date(meeting.startDateTime).toISOString().split('T')[0];
          if (meetingDate !== selectedDate) {
            return; // Skip meetings not on selected date
          }
        }

        // Parse start time
        let hours, minutes;
        if (meeting.startTime.includes('T')) {
          const startTimeObj = new Date(meeting.startTime);
          hours = startTimeObj.getHours();
          minutes = startTimeObj.getMinutes();
        } else {
          [hours, minutes] = meeting.startTime.split(':').map(Number);
        }

        // Skip if outside time range
        if (hours < this.startHour || hours > this.endHour) {
          return;
        }

        // Calculate position
        const startTimeMinutes = (hours - this.startHour) * 60 + minutes;
        const topPosition = (startTimeMinutes / 15) * 45; // 45px per 15 minutes

        // Calculate duration
        let duration = meeting.duration || 15; // Default to 15 minutes

        // Calculate height
        const displayHeight = (duration / 15) * 45;

        // Set color based on status
        let statusColor;
        switch (meeting.status) {
          case 'Confirmed':
            statusColor = 'rgba(118, 191, 250, 0.7)';
            break;
          case 'Cancelled':
            statusColor = 'rgba(220, 53, 69, 0.6)';
            break;
          case 'Requested':
            statusColor = 'rgba(255, 193, 7, 0.6)';
            break;
          default:
            statusColor = 'rgba(118, 191, 250, 0.7)';
        }

        // Format times for tooltip
        const startTime = meeting.startDateTime ?
            meeting.startDateTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) :
            meeting.startTime;

        const endTime = meeting.endDateTime ?
            meeting.endDateTime.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) :
            DateTimeUtils.calculateEndTime(meeting.startTime, duration);

        const dateStr = meeting.startDateTime ?
            meeting.startDateTime.toLocaleDateString() :
            selectedDate;

        // Create meeting event element
        const meetingEvent = document.createElement('div');
        meetingEvent.className = 'meeting-event';
        meetingEvent.style.top = `${topPosition}px`;
        meetingEvent.style.height = `${displayHeight}px`;
        meetingEvent.style.backgroundColor = statusColor;
        meetingEvent.dataset.room = meeting.room;
        meetingEvent.dataset.start = meeting.startTime;
        meetingEvent.dataset.duration = meeting.duration;
        meetingEvent.dataset.id = meeting.roomId;
        meetingEvent.dataset.status = meeting.status;

        meetingEvent.innerHTML = `
          <div class="meeting-title">${meeting.title}</div>
          <div class="meeting-organizer">${meeting.organizer}</div>
          ${meeting.description ? `<div class="meeting-description">${meeting.description}</div>` : ''}
          <div class="meeting-tooltip">
            <div class="tooltip-title">${meeting.title || 'Unnamed Meeting'}</div>
            <div class="tooltip-info"><strong>Date:</strong> ${dateStr}</div>
            <div class="tooltip-info"><strong>Time:</strong> ${startTime} - ${endTime}</div>
            <div class="tooltip-info"><strong>Duration:</strong> ${meeting.duration} min</div>
            <div class="tooltip-info"><strong>Organizer:</strong> ${meeting.organizer || 'Unnamed'}</div>
            <div class="tooltip-info"><strong>Room:</strong> ${meeting.roomName}</div>
            <div class="tooltip-info"><strong>Location:</strong> ${meeting.location}</div>
            ${meeting.bookingType !== 'ONLY' ? `<div class="tooltip-info">
                                      <strong>Type:</strong>
                                      ${meeting.bookingType}</div>` : ''}
            ${meeting.weekdays ? `<div class="tooltip-info">
                                      <strong>Weekly:</strong>
                                      ${meeting.weekdays}</div>` : ''}
            ${meeting.attendees ? `<div class="tooltip-info">
                                      <strong>Attendees:</strong>
                                      ${meeting.attendees}</div>` : ''}
            ${meeting.content ? `<div class="tooltip-info">
                                      <strong>Content:</strong>
                                      ${meeting.content}</div>` : ''}
          </div>
        `;

        // Add to room column
        const roomColumn = document.querySelector(`.room-column[data-room="${meeting.room}"] .event-grid`);
        if (roomColumn) {
          roomColumn.appendChild(meetingEvent);
        }
      });
    });

    // Add click event handlers to meetings
    this.addMeetingClickHandlers();
  }

  /**
   * Add click event handlers to meetings
   */
  addMeetingClickHandlers() {
    document.querySelectorAll('.meeting-event').forEach(meetingEvent => {
      meetingEvent.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();

        // Extract meeting data
        const roomIdentifier = meetingEvent.dataset.room;
        const startTime = meetingEvent.dataset.start;
        const duration = meetingEvent.dataset.duration;
        const roomId = meetingEvent.dataset.id;
        const status = meetingEvent.dataset.status;

        // Dispatch custom event for meeting view
        document.dispatchEvent(new CustomEvent('viewMeeting', {
          detail: {
            roomIdentifier,
            startTime,
            duration,
            roomId,
            status
          }
        }));
      });
    });
  }

  /**
   * Add handlers for creating new events
   */
  addNewEventHandlers() {
    document.querySelectorAll('.time-cell').forEach(timeCell => {
      timeCell.addEventListener('click', () => {
        const clickedTime = timeCell.dataset.time;
        const roomId = timeCell.closest('.room-column').dataset.room;

        // Check if this time is in the past
        if (timeCell.classList.contains('past-time')) {
          document.dispatchEvent(new CustomEvent('showNotification', {
            detail: {
              message: 'Cannot schedule a meeting in the past!',
              type: 'error'
            }
          }));
          return;
        }

        // Dispatch event to create new meeting
        document.dispatchEvent(new CustomEvent('createMeeting', {
          detail: {
            roomId,
            clickedTime
          }
        }));
      });
    });
  }

  /**
   * Add current time indicator
   */
  addCurrentTimeIndicator() {
    // Add CSS for time indicator
    if (!document.getElementById('time-indicator-styles')) {
      const style = document.createElement('style');
      style.id = 'time-indicator-styles';
      style.innerHTML = `
        .current-time-indicator {
          position: absolute;
          left: 22px;
          right: 0;
          height: 2px;
          background-color: #f44336;
          z-index: 1;
          pointer-events: none;
        }

        .current-time-label {
          position: absolute;
          left: 22px;
          transform: translateY(-50%);
          background-color: #f44336;
          color: white;
          padding: 2px 5px;
          font-size: 12px;
          border-radius: 3px;
          z-index: 1;
          pointer-events: none;
        }

        .current-hour-highlight {
          position: absolute;
          left: 59px;
          right: 0;
          background-color: rgba(171, 245, 250, 0.3);
          z-index: 1;
          pointer-events: none;
        }
      `;
      document.head.appendChild(style);
    }

    // Create indicator elements
    const timeIndicator = document.createElement('div');
    timeIndicator.className = 'current-time-indicator';

    const timeLabel = document.createElement('div');
    timeLabel.className = 'current-time-label';

    const hourHighlight = document.createElement('div');
    hourHighlight.className = 'current-hour-highlight';

    // Add to DOM
    const scheduleContainer = document.querySelector('.schedule-container');
    if (scheduleContainer) {
      scheduleContainer.appendChild(timeIndicator);
      scheduleContainer.appendChild(timeLabel);
      scheduleContainer.appendChild(hourHighlight);
    }

    // Initial update
    this.updateCurrentTimeIndicator();

    // Update every minute
    setInterval(() => this.updateCurrentTimeIndicator(), 60000);
  }

  /**
   * Update current time indicator position
   */
  updateCurrentTimeIndicator() {
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    // Check if current time is within display range
    if (currentHour < this.startHour || currentHour >= this.endHour) {
      document.querySelectorAll('.current-time-indicator, .current-time-label, .current-hour-highlight')
      .forEach(el => el.style.display = 'none');
      return;
    }

    // Show indicators
    document.querySelectorAll('.current-time-indicator, .current-time-label, .current-hour-highlight')
    .forEach(el => el.style.display = 'block');

    // Calculate position
    const hoursSinceStart = currentHour - this.startHour;
    const minutePercentage = currentMinute / 60;
    const timeSlotHeight = 45; // Height of each 15-minute slot
    const topPosition = (hoursSinceStart * 4 + minutePercentage * 4) * timeSlotHeight;

    // Update indicator position
    const timeIndicator = document.querySelector('.current-time-indicator');
    if (timeIndicator) {
      timeIndicator.style.top = `${topPosition}px`;
    }

    // Update time label
    const timeLabel = document.querySelector('.current-time-label');
    if (timeLabel) {
      timeLabel.textContent = `${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}`;
      timeLabel.style.top = `${topPosition}px`;
    }

    // Calculate highlight for next hour
    const nextHour = new Date(now);
    nextHour.setHours(nextHour.getHours() + 1);
    const nextHourValue = nextHour.getHours();

    const highlightHeight = nextHourValue >= this.endHour ?
        ((this.endHour - currentHour) * 60 - currentMinute) / 60 * 4 * timeSlotHeight :
        4 * timeSlotHeight; // 4 slots per hour

    // Update highlight
    const hourHighlight = document.querySelector('.current-hour-highlight');
    if (hourHighlight) {
      hourHighlight.style.top = `${topPosition}px`;
      hourHighlight.style.height = `${highlightHeight}px`;
    }

    // Update past-time cells
    this.updatePastTimeCells();
  }

  /**
   * Update past-time status for cells
   */
  updatePastTimeCells() {
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();

    // Update time cells
    document.querySelectorAll('.time-cell').forEach(cell => {
      const timeStr = cell.dataset.time;
      if (!timeStr) return;

      const [hour, minute] = timeStr.split(':').map(Number);
      const isPast = DateTimeUtils.isPastTime(hour, minute);

      if (isPast) {
        cell.classList.add('past-time');
      } else {
        cell.classList.remove('past-time');
      }
    });
  }

  /**
   * Create default rooms if none provided
   */
  createDefaultRooms() {
    const defaultRooms = [];
    const roomNames = [
      'Conference Room', 'Meeting Room', 'Brainstorming Room',
      'Board Room', 'Training Room', 'Executive Room',
      'Project Room', 'Team Room'
    ];
    const locations = [
      'Floor 1', 'Floor 2', 'West Wing', 'East Wing',
      'North Wing', 'South Wing', 'Floor 3', 'Floor 4'
    ];
    const capacities = [6, 8, 12, 20, 4, 10, 15, 30];

    for (let i = 0; i < roomNames.length; i++) {
      defaultRooms.push({
        roomId: i + 1,
        roomName: roomNames[i],
        location: locations[i],
        capacity: capacities[i],
        room: `room-${i+1}`
      });
    }

    return defaultRooms;
  }
}

/**
 * Booking Form Handler
 * Manages all booking form operations
 */
class BookingFormHandler {
  constructor(apiService, dateTimeUtils, notifications) {
    this.api = apiService;
    this.dateTimeUtils = dateTimeUtils;
    this.notifications = notifications;
    this.initialFormData = {};
  }

  /**
   * Initialize form handlers
   */
  init() {
    // Store initial form state for reset
    this.storeInitialFormData();

    // Set up modal reset on close
    document.getElementById('bookingModal')?.addEventListener('hidden.bs.modal', () => {
      this.resetFormData();
      this.setFormReadOnly(true);

      const editButton = document.getElementById('editButton');
      if (editButton) {
        editButton.textContent = 'Edit';
      }
    });

    // Set up edit button click handler
    document.getElementById('editButton')?.addEventListener('click', () => {
      this.toggleEdit();
    });

    // Set up recurrence toggles
    document.querySelectorAll('input[name="recurrence"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        this.toggleRecurrence(e.target.id);
      });
    });

    document.querySelectorAll('input[name="createRecurrence"]').forEach(radio => {
      radio.addEventListener('change', (e) => {
        this.toggleCreateRecurrence(e.target.id.replace('create', '').toLowerCase());
      });
    });

    // Set up create booking button
    document.querySelector('#createBookingModal .btn-primary')?.addEventListener('click', () => {
      this.createBooking();
    });

    // Event listener for viewing meetings
    document.addEventListener('viewMeeting', (e) => {
      this.viewMeeting(e.detail);
    });

    // Event listener for creating meetings
    document.addEventListener('createMeeting', (e) => {
      this.populateCreateBookingModal(e.detail.roomId, e.detail.clickedTime);
    });
  }

  /**
   * Store initial form data for reset
   */
  storeInitialFormData() {
    const formElements = document.querySelectorAll('#bookingForm input, #bookingForm textarea');
    formElements.forEach(element => {
      if (element.type === 'checkbox' || element.type === 'radio') {
        this.initialFormData[element.id] = element.checked;
      } else {
        this.initialFormData[element.id] = element.value;
      }
    });
  }

  /**
   * Reset form to initial state
   */
  resetFormData() {
    const formElements = document.querySelectorAll('#bookingForm input, #bookingForm textarea');
    formElements.forEach(element => {
      if (element.type === 'checkbox' || element.type === 'radio') {
        element.checked = this.initialFormData[element.id] || false;
      } else {
        element.value = this.initialFormData[element.id] || '';
      }
    });

    // Reset TinyMCE if used
    if (window.tinymce && tinymce.get('content')) {
      tinymce.get('content').setContent('');
    }
  }

  /**
   * Set form fields to read-only
   */
  setFormReadOnly(readonly = true) {
    const formElements = document.querySelectorAll('#bookingForm input, #bookingForm textarea');
    formElements.forEach(element => {
      if (element.id !== 'username') { // Keep username always readonly
        element.readOnly = readonly;
      } else {
        element.readOnly = true;
      }

      if (element.type === 'radio' || element.type === 'checkbox') {
        element.disabled = readonly;
      }
    });
  }

  /**
   * Toggle between view and edit modes
   */
  toggleEdit() {
    const editButton = document.getElementById('editButton');
    if (!editButton) return;

    const isEditing = editButton.textContent === 'Edit';

    if (isEditing) {
      // Switch to edit mode
      this.setFormReadOnly(false);
      editButton.textContent = 'Save';
      this.notifications.show("Editing mode enabled", 'info');
    } else {
      // Save changes
      this.saveBookingChanges();
      this.setFormReadOnly(true);
      editButton.textContent = 'Edit';
    }
  }

  /**
   * Save booking changes via API
   */
  async saveBookingChanges() {
    // Get booking ID
    const bookingForm = document.getElementById('bookingForm');
    const bookingId = bookingForm?.dataset.bookingId;

    if (!bookingId) {
      this.notifications.show("Error: Cannot identify the booking to update", 'error');
      return;
    }

    // Collect form data
    const updatedData = {
      bookingId: bookingId,
      title: document.getElementById('title')?.value || '',
      attendees: document.getElementById('attendees')?.value || '',
      content: (window.tinymce && tinymce.get('content'))
          ? tinymce.get('content').getContent()
          : document.getElementById('content')?.value || '',
      linkMS: document.getElementById('linkYes')?.checked ? 'yes' : 'no',
      bookingType: document.querySelector('input[name="recurrence"]:checked')?.id.toUpperCase() || 'ONLY',
      username: document.getElementById('username')?.value || ''
    };

    // Get room ID from modal or data attribute
    const roomInfoText = document.querySelector('#bookingModal .room-info strong:contains("Meeting room:")')?.parentElement.textContent;
    if (roomInfoText) {
      const roomName = roomInfoText.replace('Meeting room:', '').trim();

      if (roomName.includes('Sun Room')) {
        updatedData.roomId = 2; // Sun Room ID
      } else if (roomName.includes('Sky Room')) {
        updatedData.roomId = 1; // Sky Room ID
      } else {
        updatedData.roomId = document.getElementById('roomId')?.value || 1;
      }
    } else {
      updatedData.roomId = document.getElementById('roomId')?.value || 1;
    }

    // Handle dates based on booking type
    if (updatedData.bookingType === 'ONLY') {
      const startDateTime = new Date(document.getElementById('dateOnly')?.value || '');
      const timeStr = document.getElementById('timeOnly')?.value || '';

      if (timeStr && startDateTime && !isNaN(startDateTime)) {
        const [endHours, endMinutes] = timeStr.split(':').map(Number);
        const endDateTime = new Date(startDateTime);
        endDateTime.setHours(endHours, endMinutes, 0, 0);

        updatedData.startTime = this.dateTimeUtils.formatLocalDateTime(startDateTime);
        updatedData.endTime = this.dateTimeUtils.formatLocalDateTime(endDateTime);
      }
    } else if (updatedData.bookingType === 'DAILY') {
      updatedData.startTime = this.dateTimeUtils.formatLocalDateTime(
          document.getElementById('dateStartDaily')?.value || '');
      updatedData.endTime = this.dateTimeUtils.formatLocalDateTime(
          document.getElementById('dateEndDaily')?.value || '');
    } else if (updatedData.bookingType === 'WEEKLY') {
      updatedData.startTime = this.dateTimeUtils.formatLocalDateTime(
          document.getElementById('dateStartWeekly')?.value || '');
      updatedData.endTime = this.dateTimeUtils.formatLocalDateTime(
          document.getElementById('dateEndWeekly')?.value || '');

      // Collect selected weekdays
      const selectedDays = [];
      const days = ['mo', 'tu', 'we', 'th', 'fr'];
      days.forEach(day => {
        if (document.getElementById(day)?.checked) {
          selectedDays.push(day.charAt(0).toUpperCase() + day.slice(1));
        }
      });
      updatedData.weekdays = selectedDays.join(',');
    }

    try {
      console.log("Updating booking with data:", updatedData);
      const response = await this.api.updateBooking(bookingId, updatedData);

      if (response) {
        this.notifications.show("Changes saved successfully", 'success');

        // Refresh meeting display
        setTimeout(() => {
          document.dispatchEvent(new CustomEvent('fetchMeetingRooms', {
            detail: { date: document.getElementById('date-input')?.value }
          }));

          // Close modal
          const modal = document.getElementById('bookingModal');
          if (modal && typeof bootstrap !== 'undefined') {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) bsModal.hide();
          } else if (modal && typeof jQuery !== 'undefined') {
            jQuery(modal).modal('hide');
          }
        }, 1000);
      }
    } catch (error) {
      console.error("Error updating booking:", error);
      this.notifications.show(`Failed to save changes: ${error.message || 'Unknown error'}`, 'error');
    }
  }

  /**
   * Toggle recurrence options display
   */
  toggleRecurrence(type) {
    document.getElementById('recurrenceOnly').style.display = 'none';
    document.getElementById('recurrenceDaily').style.display = 'none';
    document.getElementById('recurrenceWeekly').style.display = 'none';

    if (type === 'only') {
      document.getElementById('recurrenceOnly').style.display = 'block';
    } else if (type === 'daily') {
      document.getElementById('recurrenceDaily').style.display = 'block';
    } else if (type === 'weekly') {
      document.getElementById('recurrenceWeekly').style.display = 'block';
    }
  }

  /**
   * Toggle create booking recurrence options
   */
  toggleCreateRecurrence(type) {
    document.getElementById('createRecurrenceOnly').style.display = type === 'only' ? 'block' : 'none';
    document.getElementById('createRecurrenceDaily').style.display = type === 'daily' ? 'block' : 'none';
    document.getElementById('createRecurrenceWeekly').style.display = type === 'weekly' ? 'block' : 'none';
  }

  /**
   * Create a new booking
   */
  async createBooking() {
    const selectedType = document.querySelector('input[name="createRecurrence"]:checked')?.id;
    if (!selectedType) {
      this.notifications.show("Please select a recurrence pattern", 'warning');
      return;
    }

    const bookingData = {
      title: document.getElementById('createTitle')?.value || '',
      attendees: document.getElementById('createAttendees')?.value || '',
      content: document.getElementById('createContent')?.value || '',
      linkMS: document.getElementById('createLinkYes')?.checked,
      bookingType: selectedType.replace('create', '').toUpperCase(),
      startTime: '',
      endTime: '',
      weekdays: null,
      username: document.getElementById('createUsername')?.value || localStorage.getItem('Username') || 'User'
    };

    // Get room ID
    const roomIdInput = document.getElementById('roomIdInput');
    if (roomIdInput) {
      const roomIdValue = roomIdInput.value;

      if (roomIdValue && typeof roomIdValue === 'string' &&
          (roomIdValue.includes('Sun') || roomIdValue.includes('sun'))) {
        bookingData.roomId = 2; // Sun Room
      } else {
        const match = roomIdValue.match(/room-(\d+)/);
        if (match && match[1]) {
          bookingData.roomId = parseInt(match[1], 10);
        } else {
          bookingData.roomId = 1; // Default to Sky Room
        }
      }
    } else {
      // Try to get from room info
      const roomInfo = document.querySelector('#createBookingModal .room-info strong:contains("Room:")')?.parentElement;
      if (roomInfo) {
        const roomText = roomInfo.textContent;
        const roomName = roomText.replace('Room:', '').trim();

        if (roomName.includes('Sun Room')) {
          bookingData.roomId = 2;
        } else {
          bookingData.roomId = 1;
        }
      } else {
        bookingData.roomId = 1;
      }
    }

    const now = new Date();

    // Handle dates based on recurrence type
    if (selectedType === 'createOnly') {
      const date = document.getElementById('createDateOnly')?.value;
      const time = document.getElementById('createTimeOnly')?.value;

      if (!date || !time) {
        this.notifications.show("Please enter date and time", 'warning');
        return;
      }

      // Parse dates
      const startDateTime = new Date(date);
      const [endHours, endMinutes] = time.split(':').map(Number);
      const endDateTime = new Date(startDateTime);
      endDateTime.setHours(endHours, endMinutes);

      // Validate dates
      if (isNaN(startDateTime) || isNaN(endDateTime)) {
        this.notifications.show("Invalid date or time format", 'error');
        return;
      }

      if (startDateTime < now) {
        this.notifications.show("Start time cannot be in the past", 'warning');
        return;
      }

      const diffMinutes = (endDateTime - startDateTime) / (1000 * 60);
      if (diffMinutes < 15) {
        this.notifications.show("Booking duration must be at least 15 minutes", 'warning');
        return;
      }

      if (endDateTime <= startDateTime) {
        this.notifications.show("End time must be after start time", 'warning');
        return;
      }

      bookingData.startTime = this.dateTimeUtils.formatLocalDateTime(startDateTime);
      bookingData.endTime = this.dateTimeUtils.formatLocalDateTime(endDateTime);
    } else if (selectedType === 'createDaily' || selectedType === 'createWeekly') {
      const startElement = selectedType === 'createDaily' ? 'createDateStartDaily' : 'createDateStartWeekly';
      const endElement = selectedType === 'createDaily' ? 'createDateEndDaily' : 'createDateEndWeekly';

      const start = document.getElementById(startElement)?.value;
      const end = document.getElementById(endElement)?.value;

      if (!start || !end) {
        this.notifications.show("Please provide start and end dates", 'warning');
        return;
      }

      const startDate = new Date(start);
      const endDate = new Date(end);

      if (startDate < now) {
        this.notifications.show("Start date cannot be in the past", 'warning');
        return;
      }

      if (endDate <= startDate) {
        this.notifications.show("End date must be after start date", 'warning');
        return;
      }

      bookingData.startTime = this.dateTimeUtils.formatLocalDateTime(startDate);
      bookingData.endTime = this.dateTimeUtils.formatLocalDateTime(endDate);

      if (selectedType === 'createWeekly') {
        const weekdays = this.getSelectedWeekdays();
        if (!weekdays) return;
        bookingData.weekdays = weekdays;
      }
    }

    try {
      console.log("Creating booking with data:", bookingData);
      const response = await this.api.createBooking(bookingData);

      this.notifications.show("Booking created successfully", 'success');

      // Close modal and refresh
      const modal = document.getElementById('createBookingModal');
      if (modal && typeof bootstrap !== 'undefined') {
        const bsModal = bootstrap.Modal.getInstance(modal);
        if (bsModal) bsModal.hide();
      } else if (modal && typeof jQuery !== 'undefined') {
        jQuery(modal).modal('hide');
      }

      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (error) {
      let errorMsg = 'Failed to create booking';

      if (error.status === 409) {
        errorMsg = `Time conflict with existing booking: ${error.retMsg || ''}`;
      } else if (error.retMsg) {
        errorMsg = `Error: ${error.retMsg}`;
      }

      this.notifications.show(errorMsg, 'error');
    }
  }

  /**
   * Get selected weekdays for weekly recurrence
   */
  getSelectedWeekdays() {
    const selectedDays = [];
    const days = ['createMo', 'createTu', 'createWe', 'createTh', 'createFr'];

    days.forEach(day => {
      if (document.getElementById(day)?.checked) {
        selectedDays.push(day.replace('create', ''));
      }
    });

    if (selectedDays.length === 0) {
      this.notifications.show("Please select at least one weekday", 'warning');
      return null;
    }

    return selectedDays.join(',');
  }

  /**
   * View meeting details
   */
  async viewMeeting(detail) {
    const { roomIdentifier, startTime, duration, roomId } = detail;

    // Find meeting in global data
    const allMeetings = window.meetingData || [];
    const meeting = allMeetings.find(m =>
        m.room === roomIdentifier && m.startTime === startTime);

    if (!meeting) {
      console.error(`Meeting in room ${roomIdentifier} at ${startTime} not found in data`);
      return;
    }

    // Check if meeting is in the past
    const now = new Date();
    const meetingStartTime = meeting.startDateTime || new Date(meeting.date + 'T' + meeting.startTime);
    const isPast = meetingStartTime < now;

    // Populate form fields
    document.getElementById('title').value = meeting.title || 'Untitled Meeting';
    document.getElementById('attendees').value = meeting.attendees || meeting.organizer || '';
    document.getElementById('username').value = meeting.organizer || '';

    // Set room ID
    let roomIdValue = meeting.roomId || 1;
    if (meeting.roomName && meeting.roomName.includes('Sun Room')) {
      roomIdValue = 2;
    }
    document.getElementById('roomId').value = roomIdValue;

    // Set content
    const contentEl = document.getElementById('content');
    if (window.tinymce && tinymce.get('content')) {
      tinymce.get('content').setContent(meeting.content || '');
    } else if (contentEl) {
      contentEl.value = meeting.content || '';
    }

    // Set MS Teams link
    const hasTeamsLink = meeting.linkMS === 'yes' || meeting.linkMS === true;
    document.getElementById('linkYes').checked = hasTeamsLink;
    document.getElementById('linkNo').checked = !hasTeamsLink;

    // Set recurrence type
    const bookingType = meeting.bookingType || 'ONLY';
    document.getElementById('only').checked = bookingType === 'ONLY';
    document.getElementById('daily').checked = bookingType === 'DAILY';
    document.getElementById('weekly').checked = bookingType === 'WEEKLY';
    this.toggleRecurrence(bookingType.toLowerCase());

    // Format dates for the form
    if (meeting.startDateTime) {
      const startDateTimeStr = this.dateTimeUtils.formatDate(meeting.startDateTime, 'datetime-local');
      const endDateTime = meeting.endDateTime || new Date(meeting.startDateTime.getTime() + (meeting.duration || 60) * 60000);
      const endDateTimeStr = this.dateTimeUtils.formatDate(endDateTime, 'datetime-local');
      const endTimeStr = this.dateTimeUtils.formatDate(endDateTime, 'HH:MM');

      if (bookingType === 'ONLY') {
        document.getElementById('dateOnly').value = startDateTimeStr;
        document.getElementById('timeOnly').value = endTimeStr;
      } else if (bookingType === 'DAILY') {
        document.getElementById('dateStartDaily').value = startDateTimeStr;
        document.getElementById('dateEndDaily').value = endDateTimeStr;
      } else if (bookingType === 'WEEKLY') {
        document.getElementById('dateStartWeekly').value = startDateTimeStr;
        document.getElementById('dateEndWeekly').value = endDateTimeStr;

        // Set weekdays
        if (meeting.weekdays) {
          const weekdays = meeting.weekdays.split(',');
          document.getElementById('mo').checked = weekdays.includes('Mo');
          document.getElementById('tu').checked = weekdays.includes('Tu');
          document.getElementById('we').checked = weekdays.includes('We');
          document.getElementById('th').checked = weekdays.includes('Th');
          document.getElementById('fr').checked = weekdays.includes('Fr');
        }
      }
    } else {
      // Fallback for missing datetime objects
      const selectedDate = document.getElementById('date-input').value || this.dateTimeUtils.formatDate(new Date());
      const timeObj = this.dateTimeUtils.parseTimeString(meeting.startTime);

      if (timeObj) {
        const startDateTime = new Date(selectedDate);
        startDateTime.setHours(timeObj.hour, timeObj.minute, 0, 0);
        const endDateTime = new Date(startDateTime.getTime() + (meeting.duration || 60) * 60000);

        const formattedStartDateTime = this.dateTimeUtils.formatDate(startDateTime, 'datetime-local');
        const formattedEndDateTime = this.dateTimeUtils.formatDate(endDateTime, 'datetime-local');
        const formattedEndTime = this.dateTimeUtils.formatDate(endDateTime, 'HH:MM');

        if (bookingType === 'ONLY') {
          document.getElementById('dateOnly').value = formattedStartDateTime;
          document.getElementById('timeOnly').value = formattedEndTime;
        } else if (bookingType === 'DAILY') {
          document.getElementById('dateStartDaily').value = formattedStartDateTime;
          document.getElementById('dateEndDaily').value = formattedEndDateTime;
        } else if (bookingType === 'WEEKLY') {
          document.getElementById('dateStartWeekly').value = formattedStartDateTime;
          document.getElementById('dateEndWeekly').value = formattedEndDateTime;
        }
      }
    }

    // Update room information
    const roomInfoEl = document.querySelector('#bookingModal .room-info');
    if (roomInfoEl) {
      roomInfoEl.innerHTML = `
        <h5>Room Information</h5>
        <p><strong>Time:</strong> ${meeting.startTime} - ${meeting.endTime || 'N/A'}</p>
        <p><strong>Building:</strong> ${meeting.building || '789 Tower'}</p>
        <p><strong>Floor:</strong> ${meeting.location || 'Tầng 8 - new'}</p>
        <p><strong>Meeting room:</strong> ${meeting.roomName || 'Room ' + roomId}</p>
        <p><strong>Price:</strong> ${meeting.price || '100,000 VNĐ/h'}</p>
        <p><strong>Seats:</strong> ${meeting.capacity || '6'}</p>
        <p><strong>Devices and services:</strong> ${meeting.devices || 'Standard equipment'}</p>
        <p><strong>Status:</strong> ${meeting.status || 'Confirmed'}</p>
      `;
    }

    // Make form read-only
    this.setFormReadOnly(true);

    // Update edit button based on whether meeting is in the past
    const editButton = document.getElementById('editButton');
    if (editButton) {
      if (isPast) {
        editButton.disabled = true;
        editButton.classList.add('disabled');

        const modalFooter = document.querySelector('#bookingModal .modal-footer');
        if (modalFooter) {
          const existingMsg = modalFooter.querySelector('.past-meeting-msg');
          if (existingMsg) existingMsg.remove();

          const pastMeetingMsg = document.createElement('div');
          pastMeetingMsg.className = 'past-meeting-msg text-danger';
          pastMeetingMsg.style.marginRight = '10px';
          pastMeetingMsg.textContent = 'Cannot edit meetings in the past';
          modalFooter.insertBefore(pastMeetingMsg, editButton);
        }
      } else {
        editButton.disabled = false;
        editButton.classList.remove('disabled');

        const pastMeetingMsg = document.querySelector('#bookingModal .modal-footer .past-meeting-msg');
        if (pastMeetingMsg) pastMeetingMsg.remove();
      }
    }

    // Set booking ID on form
    const bookingForm = document.getElementById('bookingForm');
    if (bookingForm) {
      bookingForm.dataset.bookingId = meeting.bookingId || '';
    }

    // Show the modal
    const modal = document.getElementById('bookingModal');
    if (modal && typeof bootstrap !== 'undefined') {
      const bsModal = new bootstrap.Modal(modal);
      bsModal.show();
    } else if (modal && typeof jQuery !== 'undefined') {
      jQuery(modal).modal('show');
    }
  }

  /**
   * Populate the create booking modal
   */
  populateCreateBookingModal(roomId, clickedTime) {
    // Find room details
    const allRooms = window.allRooms || [];
    const room = allRooms.find(r => r.room === roomId) ||
        { room: roomId, roomName: `Room ${roomId.replace('room-', '')}` };

    // Get selected date
    const selectedDate = document.getElementById('date-input')?.value ||
        this.dateTimeUtils.formatDate(new Date());

    // Parse clicked time
    const timeObj = this.dateTimeUtils.parseTimeString(clickedTime);
    if (!timeObj) {
      console.error('Invalid time format:', clickedTime);
      return;
    }

    // Create datetime for clicked time
    const clickedDateTime = new Date(selectedDate);
    clickedDateTime.setHours(timeObj.hour, timeObj.minute, 0, 0);

    // Format for inputs
    const formattedDateTime = this.dateTimeUtils.formatDate(clickedDateTime, 'datetime-local');

    // Calculate default end time (1 hour after start)
    const endDateTime = new Date(clickedDateTime);
    endDateTime.setHours(endDateTime.getHours() + 1);
    const formattedEndTime = this.dateTimeUtils.formatDate(endDateTime, 'HH:MM');
    const formattedEndDateTime = this.dateTimeUtils.formatDate(endDateTime, 'datetime-local');

    // Update room ID
    document.getElementById('roomIdInput').value = roomId;

    // Update room information
    const roomInfoEl = document.querySelector('#createBookingModal .room-info');
    if (roomInfoEl) {
      roomInfoEl.innerHTML = `
        <h5>Room Information</h5>
        <p><strong>Time:</strong> ${clickedTime}</p>
        <p><strong>Date:</strong> ${selectedDate}</p>
        <p><strong>Room:</strong> ${room.roomName || `Room ${roomId.replace('room-', '')}`}</p>
        <p><strong>Building:</strong> ${room.building || '789 Tower'}</p>
        <p><strong>Floor:</strong> ${room.location || 'Tầng 8 - new'}</p>
        <p><strong>Seats:</strong> ${room.capacity || '6'}</p>
      `;
    }

    // Set default values
    document.getElementById('createTitle').value = '';
    document.getElementById('createAttendees').value = '';
    document.getElementById('createContent').value = '';
    document.getElementById('createUsername').value = localStorage.getItem('Username') || 'User';

    // Set recurrence to "Only" by default
    document.getElementById('createOnly').checked = true;
    this.toggleCreateRecurrence('only');

    // Set datetime values
    document.getElementById('createDateOnly').value = formattedDateTime;
    document.getElementById('createTimeOnly').value = formattedEndTime;
    document.getElementById('createDateStartDaily').value = formattedDateTime;
    document.getElementById('createDateEndDaily').value = formattedEndDateTime;
    document.getElementById('createDateStartWeekly').value = formattedDateTime;
    document.getElementById('createDateEndWeekly').value = formattedEndDateTime;

    // Show modal
    const modal = document.getElementById('createBookingModal');
    if (modal && typeof bootstrap !== 'undefined') {
      const bsModal = new bootstrap.Modal(modal);
      bsModal.show();
    } else if (modal && typeof jQuery !== 'undefined') {
      jQuery(modal).modal('show');
    }
  }
}

/**
 * Main Application Controller
 */
class MeetingRoomApp {
  constructor() {
    // Initialize services
    this.dateTimeUtils = new DateTimeUtils();
    this.notifications = new NotificationService();
    this.api = new ApiService(BASE_URL);

    // Initialize UI components
    this.ui = new MeetingRoomUI(this.dateTimeUtils, this.notifications);
    this.gridRenderer = new MeetingGridRenderer();
    this.bookingHandler = new BookingFormHandler(this.api, this.dateTimeUtils, this.notifications);

    // Store data
    this.meetingData = [];
    this.cachedRooms = [];
  }

  /**
   * Initialize the application
   */
  init() {
    // Initialize UI components
    this.ui.init();
    this.bookingHandler.init();

    // Set up event listeners
    this.setupEventListeners();

    // Initial data fetch
    this.fetchMeetingRooms({
      date: document.getElementById('date-input')?.value || new Date().toISOString().split('T')[0]
    });
  }

  /**
   * Set up event listeners
   */
  setupEventListeners() {
    // Custom event for fetching meeting rooms
    document.addEventListener('fetchMeetingRooms', (e) => {
      this.fetchMeetingRooms(e.detail);
    });

    // Custom event for showing notifications
    document.addEventListener('showNotification', (e) => {
      this.notifications.show(e.detail.message, e.detail.type);
    });

    // Handle user authentication
    document.addEventListener('DOMContentLoaded', () => {
      this.handleAuthentication();
    });

    // Logout button
    document.querySelector('a[onclick="logout()"]')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.logout();
    });
  }

  /**
   * Handle user authentication
   */
  handleAuthentication() {
    const username = localStorage.getItem('Username');
    const role = localStorage.getItem('Role');

    if (username && role) {
      // Update UI for logged in user
      document.getElementById('userInfo').style.display = 'block';
      document.getElementById('loginBtn').style.display = 'none';
      document.getElementById('dropdownUsername').textContent = username;
      document.getElementById('dropdownRole').textContent = role.charAt(0) + role.slice(1).toLowerCase();

      // Handle role-specific UI
      if (role === 'ADMIN') {
        document.getElementById('meeting').style.display = 'none';
      } else if (role === 'EMPLOYEE') {
        document.getElementById('office').style.display = 'none';
        document.getElementById('account').style.display = 'none';
        document.getElementById('dpment').style.display = 'none';
      } else if (role === 'SUPERVISOR') {
        document.getElementById('office').style.display = 'none';
        document.getElementById('meeting').style.display = 'none';
        document.getElementById('dpment').style.display = 'none';
      }
    }
  }

  /**
   * Logout the user
   */
  async logout() {
    try {
      const success = await this.api.logout();

      if (success) {
        localStorage.clear();
        document.cookie.split(";").forEach(function(c) {
          document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });
        window.location.href = "/users/login";
      } else {
        this.notifications.show("Logout failed. Please try again.", 'error');
      }
    } catch (error) {
      console.error("Logout error:", error);
      this.notifications.show("Logout failed. Please try again.", 'error');
    }
  }

  /**
   * Fetch meeting rooms and process response
   */
  async fetchMeetingRooms(filters = {}) {
    try {
      const response = await this.api.fetchMeetingRooms(filters);

      if (response && response.status === 'success') {
        this.processApiResponse(response);
      } else {
        console.error('API returned error:', response?.retMsg || 'Unknown error');
        this.notifications.show(response?.retMsg || 'Failed to fetch meeting rooms');

        // Use cached data or defaults if API fails
        this.useFallbackData();
      }
    } catch (error) {
      console.error('Error fetching meeting rooms:', error);
      this.notifications.show('Error loading meeting data. Using cached data.');

      // Use cached data or defaults if API fails
      this.useFallbackData();
    }
  }

  /**
   * Use fallback data when API fails
   */
  useFallbackData() {
    // Use cached rooms if available
    if (this.cachedRooms && this.cachedRooms.length > 0) {
      this.ui.updateRoomList(this.cachedRooms);
      this.ui.populateFilters(this.cachedRooms);
    } else {
      // Create default rooms
      const defaultRooms = this.createDefaultRooms();
      this.ui.updateRoomList(defaultRooms);
      this.ui.populateFilters(defaultRooms);
      this.cachedRooms = defaultRooms;
    }

    // Render grid
    this.gridRenderer.generateTimeColumn();
    this.gridRenderer.generateRoomGrid(this.cachedRooms, this.meetingData);
  }

  /**
   * Process API response
   */
  processApiResponse(response) {
    // Clear existing meeting data
    this.meetingData = [];

    // Process rooms first
    const allRooms = [];
    const roomMap = {};

    // Extract unique rooms
    response.data.forEach(item => {
      if (!roomMap[item.roomId]) {
        roomMap[item.roomId] = true;
        allRooms.push({
          roomId: item.roomId,
          roomName: item.roomName,
          location: item.location || 'Sky Room - Floor 1',
          capacity: item.capacity || 10,
          room: `room-${item.roomId}`
        });
      }
    });

    // Get selected date
    const selectedDate = document.getElementById('date-input')?.value;

    // Process meetings
    response.data.forEach(item => {
      try {
        // Parse dates
        const startDateTime = new Date(item.startTime);
        const endDateTime = new Date(item.endTime);
        const startHour = startDateTime.getHours();
        const startMinute = startDateTime.getMinutes();
        const endHour = endDateTime.getHours();
        const endMinute = endDateTime.getMinutes();

        // Calculate duration
        const durationMs = endDateTime - startDateTime;
        const durationMinutes = Math.floor(durationMs / (1000 * 60));

        // Format times
        const formattedStartTime = `${startHour.toString().padStart(2, '0')}:${startMinute.toString().padStart(2, '0')}`;
        const formattedEndTime = `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`;

        // Create room ID
        const roomIdFormatted = `room-${item.roomId}`;

        // Get corresponding room
        const room = allRooms.find(r => r.roomId === item.roomId);

        // Check if meeting is for selected date
        const meetingDate = startDateTime.toISOString().split('T')[0];

        // Only add if for selected date
        if (meetingDate === selectedDate) {
          this.meetingData.push({
            // Room info
            room: roomIdFormatted,
            roomId: item.roomId,
            roomName: item.roomName,
            location: item.location || (room ? room.location : 'Sky Room - Floor 1'),
            capacity: item.capacity || (room ? room.capacity : 10),

            // Booking info
            title: item.title || `${item.roomName} - ${item.username}`,
            organizer: item.username,
            attendees: item.attendees || '',
            content: item.content || '',
            description: item.content || `Status: ${item.status}`,

            // Time info
            startTime: formattedStartTime,
            endTime: formattedEndTime,
            duration: durationMinutes,
            startDateTime: startDateTime,
            endDateTime: endDateTime,
            date: meetingDate,

            // Status and type info
            status: item.status,
            bookingType: item.bookingType || 'ONLY',
            weekdays: item.weekdays || '',
            bookingId: item.bookingId,

            // Additional metadata
            pageInfo: {
              pageSize: response.pageSize,
              pageNo: response.pageNo,
              total: response.total
            }
          });
        }
      } catch (error) {
        console.error('Error processing meeting data:', error, item);
      }
    });

    // If no rooms found in API result
    if (allRooms.length === 0) {
      this.notifications.show('No rooms found matching your criteria. Showing all available rooms.', 'info');

      // Use cached rooms or create default ones
      if (this.cachedRooms && this.cachedRooms.length > 0) {
        this.ui.updateRoomList(this.cachedRooms);
        this.ui.populateFilters(this.cachedRooms);
      } else {
        const defaultRooms = this.createDefaultRooms();
        this.ui.updateRoomList(defaultRooms);
        this.ui.populateFilters(defaultRooms);
        this.cachedRooms = defaultRooms;
      }
    } else {
      // Cache and update with API rooms
      this.cachedRooms = [...allRooms];
      this.ui.updateRoomList(allRooms);
      this.ui.populateFilters(allRooms);
    }

    // Make meeting data available globally
    window.meetingData = this.meetingData;
    window.allRooms = this.cachedRooms;

    // Always render the grid
    this.gridRenderer.generateTimeColumn();
    this.gridRenderer.generateRoomGrid(this.cachedRooms, this.meetingData);

    console.log(`Loaded ${this.meetingData.length} meetings for date: ${selectedDate}`);
  }

  /**
   * Create default rooms when no rooms are available
   */
  createDefaultRooms() {
    const defaultRooms = [];
    const roomNames = [
      'Conference Room', 'Meeting Room', 'Brainstorming Room',
      'Board Room', 'Training Room', 'Executive Room',
      'Project Room', 'Team Room'
    ];
    const locations = [
      'Floor 1', 'Floor 2', 'West Wing', 'East Wing',
      'North Wing', 'South Wing', 'Floor 3', 'Floor 4'
    ];
    const capacities = [6, 8, 12, 20, 4, 10, 15, 30];

    for (let i = 0; i < roomNames.length; i++) {
      defaultRooms.push({
        roomId: i + 1,
        roomName: roomNames[i],
        location: locations[i],
        capacity: capacities[i],
        room: `room-${i+1}`
      });
    }

    return defaultRooms;
  }
}

/**
 * Initialize the application when DOM is ready
 */
document.addEventListener('DOMContentLoaded', () => {
  // Initialize the application
  const app = new MeetingRoomApp();
  app.init();

  // Initialize TinyMCE if available
  if (window.tinymce) {
    tinymce.init({
      selector: '#content',
      plugins: 'advlist autolink lists link image charmap print preview hr anchor pagebreak',
      toolbar: 'bold italic underline | alignleft aligncenter alignright alignjustify | link image',
      height: 200
    });
  }
});