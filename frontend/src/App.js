import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import FacilityAssetList from './components/FacilityAssetList';
import FacilityAssetDetail from './components/FacilityAssetDetail';
import AddFacilityAssetEntry from './components/AddFacilityAssetEntry';
import EditFacilityAssetEntry from './components/EditFacilityAssetEntry';
import Notifications from './components/Notifications';
import AdminUserManagement from './components/AdminUserManagement';
import BookingRequestForm from './components/BookingRequestForm';
import MyBookings from './components/MyBookings';
import AdminBookings from './components/AdminBookings';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Home and Authentication Routes */}
        <Route path="/" element={<FacilityAssetList />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Facility & Asset Routes */}
        <Route path="/facility-assets" element={<FacilityAssetList />} />
        <Route path="/facility-asset/:assetId" element={<FacilityAssetDetail />} />
        <Route path="/add-facility-asset" element={<AddFacilityAssetEntry />} />
        <Route path="/edit-facility-asset/:assetId" element={<EditFacilityAssetEntry />} />

        {/* Notifications Route */}
        <Route path="/notifications" element={<Notifications />} />

        {/* Admin Routes */}
        <Route path="/admin/users" element={<AdminUserManagement />} />

        {/* Booking Routes */}
        <Route path="/book" element={<BookingRequestForm />} />
        <Route path="/my-bookings" element={<MyBookings />} />
        <Route path="/admin/bookings" element={<AdminBookings />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
