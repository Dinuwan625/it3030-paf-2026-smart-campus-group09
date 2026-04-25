import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import axios from 'axios';
import Navbar from './Navbar';

const API_URL = process.env.REACT_APP_API_URL || "http://localhost:8089/api/v1";

function BookingRequestForm() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [resources, setResources] = useState([]);
  const [loadingResources, setLoadingResources] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const [form, setForm] = useState({
    resourceId: searchParams.get('resourceId') || '',
    resourceTitle: searchParams.get('resourceTitle') || '',
    date: '',
    startTime: '',
    endTime: '',
    purpose: '',
    expectedAttendees: ''
  });

  const getCurrentDateTime = () => new Date();

  const getTodayDate = () => {
    const now = getCurrentDateTime();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const getRoundedCurrentTime = () => {
    const now = getCurrentDateTime();
    const rounded = new Date(now);
    rounded.setSeconds(0, 0);
    const hours = String(rounded.getHours()).padStart(2, '0');
    const mins = String(rounded.getMinutes()).padStart(2, '0');
    return `${hours}:${mins}`;
  };

  const isToday = form.date === getTodayDate();

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
      navigate('/login');
      return;
    }

    axios.get(`${API_URL}/facility-assets/getall`, { withCredentials: true })
      .then(res => {
        const list = Array.isArray(res.data) ? res.data : (res.data.facilityAssets || []);
        setResources(list);
        // Pre-select from query param
        if (form.resourceId) {
          const matched = list.find(r => r._id === form.resourceId);
          if (matched) {
            setForm(prev => ({ ...prev, resourceTitle: matched.title }));
          }
        }
      })
      .catch(() => setErrorMsg('Failed to load resources'))
      .finally(() => setLoadingResources(false));
    // eslint-disable-next-line
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === 'resourceId') {
      const selected = resources.find(r => r._id === value);
      setForm(prev => ({ ...prev, resourceId: value, resourceTitle: selected ? selected.title : '' }));
    } else {
      setForm(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (!form.resourceId) { setErrorMsg('Please select a resource.'); return; }
    if (!form.date) { setErrorMsg('Please select a date.'); return; }
    if (!form.startTime || !form.endTime) { setErrorMsg('Please set start and end times.'); return; }
    if (form.startTime >= form.endTime) { setErrorMsg('End time must be after start time.'); return; }
    if (!form.purpose.trim()) { setErrorMsg('Please describe the purpose.'); return; }
    if (!form.expectedAttendees || form.expectedAttendees < 1) { setErrorMsg('Please enter expected attendees.'); return; }

    const now = getCurrentDateTime();
    const bookingStart = new Date(`${form.date}T${form.startTime}`);
    const bookingEnd = new Date(`${form.date}T${form.endTime}`);

    if (bookingStart < now) { setErrorMsg('Past dates or times are not allowed.'); return; }
    if (bookingEnd <= bookingStart) { setErrorMsg('End time must be after start time.'); return; }

    const user = JSON.parse(localStorage.getItem('user'));
    const payload = {
      resourceId: form.resourceId,
      resourceTitle: form.resourceTitle,
      userId: user.userId,
      username: user.username || user.name || user.email,
      date: form.date,
      startTime: form.startTime,
      endTime: form.endTime,
      purpose: form.purpose.trim(),
      expectedAttendees: parseInt(form.expectedAttendees, 10)
    };

    setSubmitting(true);
    try {
      await axios.post(`${API_URL}/bookings`, payload, { withCredentials: true });
      setSuccessMsg('Booking request submitted! You can track it in My Bookings.');
      setForm(prev => ({ ...prev, date: '', startTime: '', endTime: '', purpose: '', expectedAttendees: '' }));
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to submit booking request.';
      setErrorMsg(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const todayDate = getTodayDate();

  return (
    <>
      <Navbar />
      <div className="container sc-page-shell">
        <div className="row justify-content-center">
          <div className="col-lg-7">
            <div className="mb-3">
              <button onClick={() => navigate(-1)} className="btn btn-link text-muted p-0 d-inline-flex align-items-center gap-1 small fw-semibold text-decoration-none">
                <i className="bi bi-arrow-left"></i> Back
              </button>
            </div>

            <div className="card sc-card">
              <div className="sc-card-header">
                <h4 className="mb-0 fw-bold"><i className="bi bi-calendar-plus me-2"></i>Book a Resource</h4>
                <p className="mb-0 opacity-75 small mt-1">Fill in the details below to submit a booking request.</p>
              </div>
              <div className="card-body p-4">

                {successMsg && (
                  <div className="alert alert-success d-flex align-items-center gap-2" role="alert">
                    <i className="bi bi-check-circle-fill"></i>
                    {successMsg}
                    <button className="btn btn-sm btn-outline-success ms-auto" onClick={() => navigate('/my-bookings')}>
                      View My Bookings
                    </button>
                  </div>
                )}
                {errorMsg && (
                  <div className="alert alert-danger d-flex align-items-center gap-2" role="alert">
                    <i className="bi bi-exclamation-triangle-fill"></i>{errorMsg}
                  </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Resource <span className="text-danger">*</span></label>
                    {loadingResources ? (
                      <div className="text-muted small">Loading resources…</div>
                    ) : (
                      <select
                        name="resourceId"
                        className="form-select"
                        value={form.resourceId}
                        onChange={handleChange}
                        required
                      >
                        <option value="">— Select a resource —</option>
                        {resources.map(r => (
                          <option key={r._id} value={r._id}>{r.title}</option>
                        ))}
                      </select>
                    )}
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Date <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      name="date"
                      className="form-control"
                      value={form.date}
                      min={todayDate}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="row g-3 mb-3">
                    <div className="col-6">
                      <label className="form-label fw-semibold">Start Time <span className="text-danger">*</span></label>
                      <input
                        type="time"
                        name="startTime"
                        className="form-control"
                        value={form.startTime}
                        onChange={handleChange}
                        min={isToday ? getRoundedCurrentTime() : undefined}
                        required
                        step="60"
                      />
                    </div>
                    <div className="col-6">
                      <label className="form-label fw-semibold">End Time <span className="text-danger">*</span></label>
                      <input
                        type="time"
                        name="endTime"
                        className="form-control"
                        value={form.endTime}
                        onChange={handleChange}
                        min={form.startTime || (isToday ? getRoundedCurrentTime() : undefined)}
                        required
                        step="60"
                      />
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label fw-semibold">Purpose <span className="text-danger">*</span></label>
                    <textarea
                      name="purpose"
                      className="form-control"
                      rows={3}
                      maxLength={300}
                      value={form.purpose}
                      onChange={handleChange}
                      placeholder="Describe the purpose of your booking…"
                      required
                    />
                    <div className="form-text text-end">{form.purpose.length}/300</div>
                  </div>

                  <div className="mb-4">
                    <label className="form-label fw-semibold">Expected Attendees <span className="text-danger">*</span></label>
                    <input
                      type="number"
                      name="expectedAttendees"
                      className="form-control"
                      value={form.expectedAttendees}
                      onChange={handleChange}
                      min={1}
                      max={999}
                      placeholder="e.g. 25"
                      required
                    />
                  </div>

                  <div className="d-flex gap-2 justify-content-end">
                    <button type="button" className="btn btn-outline-secondary px-4" onClick={() => navigate(-1)}>
                      Cancel
                    </button>
                    <button type="submit" className="btn btn-primary px-4 fw-semibold" disabled={submitting}>
                      {submitting ? (
                        <><span className="spinner-border spinner-border-sm me-2" role="status"></span>Submitting…</>
                      ) : (
                        <><i className="bi bi-send me-2"></i>Submit Request</>
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
//export library BookingRequestForm
export default BookingRequestForm;
