# 🧪 ShuttleReg Testing Guide

## 📱 **Complete End-to-End Testing Scenarios**

### **🎯 Test Environment Setup**

Before testing, ensure:
- ✅ Firebase project configured
- ✅ Sample data added to Firestore
- ✅ Razorpay test keys configured
- ✅ App built successfully
- ✅ Device/emulator ready

---

## **🔐 Test Scenario 1: User Authentication Flow**

### **1.1 User Signup (New User)**
**Steps:**
1. Launch app → Splash screen appears
2. Navigate to Auth screen
3. Switch to "Sign Up" tab
4. Enter test data:
   - **Email**: `testuser@shuttlereg.com`
   - **Password**: `TestPass123!`
   - **Full Name**: `Test User`
5. Tap "Sign Up"

**Expected Results:**
- ✅ User account created in Firebase Auth
- ✅ User document created in Firestore
- ✅ Navigate to Tournament List screen
- ✅ No errors displayed

**Validation:**
- Check Firebase Console → Authentication → Users
- Check Firestore → users collection → new document

### **1.2 User Login (Existing User)**
**Steps:**
1. Use existing test credentials
2. Enter email and password
3. Tap "Sign In"

**Expected Results:**
- ✅ Successful authentication
- ✅ Navigate to Tournament List screen
- ✅ User data loaded correctly

### **1.3 Password Reset**
**Steps:**
1. Tap "Forgot Password?"
2. Enter email address
3. Submit request

**Expected Results:**
- ✅ Reset email sent
- ✅ Success message displayed
- ✅ No app crashes

---

## **🏆 Test Scenario 2: Tournament Discovery & Browsing**

### **2.1 Tournament List Loading**
**Steps:**
1. After authentication, view Tournament List
2. Wait for data loading

**Expected Results:**
- ✅ Loading indicator appears
- ✅ Tournament cards display correctly
- ✅ All tournament details visible:
  - Tournament name
  - Dates and venue
  - Registration status
  - Participant count
  - Register button

**Data Validation:**
- Tournament dates formatted correctly
- Registration deadline status accurate
- Available events listed properly

### **2.2 Tournament Search**
**Steps:**
1. Tap search bar
2. Enter "Summer"
3. Verify filtered results
4. Clear search
5. Test with "Junior"

**Expected Results:**
- ✅ Real-time filtering works
- ✅ Relevant tournaments shown
- ✅ Clear search restores full list
- ✅ No search crashes

### **2.3 Tournament Details Navigation**
**Steps:**
1. Tap on "Summer Championship 2024"
2. View tournament details

**Expected Results:**
- ✅ Complete tournament information displayed
- ✅ Event categories and prices visible
- ✅ Registration button functional
- ✅ Contact information accessible

---

## **📝 Test Scenario 3: Registration Flow (Critical Path)**

### **3.1 Personal Details Step**
**Test Data:**
```
Full Name: Arjun Kumar
Email: arjun.kumar@email.com
Phone: +91 9876543220
Date of Birth: 15/06/2010 (U15 eligible)
Gender: Male
Club: Test Badminton Academy
```

**Steps:**
1. Tap "Register" on Summer Championship
2. Fill personal details form
3. Tap "Next"

**Expected Results:**
- ✅ Age automatically calculated (14 years)
- ✅ Eligible categories determined (U15, U17, U19)
- ✅ Form validation works
- ✅ Navigate to Event Selection

**Validation Points:**
- Phone number formatting
- Email validation
- Date picker functionality
- Age calculation accuracy

### **3.2 Event Selection Step**
**Test Scenarios:**

**A. Singles Event Selection:**
1. Select "U15 Singles" (₹600)
2. Verify no partner required
3. Continue to payment

**B. Doubles Event Selection:**
1. Select "U15 Doubles" (₹800)
2. Add partner details:
   - **Name**: Vikram Singh
   - **Phone**: +91 9876543221
   - **Email**: vikram@email.com
   - **Gender**: Male
3. Continue to payment

**C. Multiple Events:**
1. Select both Singles and Doubles
2. Add partner for doubles
3. Verify total calculation

**Expected Results:**
- ✅ Event cards display correctly
- ✅ Price per person calculated
- ✅ Partner form appears for doubles
- ✅ Total amount accurate
- ✅ Age restrictions enforced

**Fee Calculation Validation:**
- Singles: ₹600 (full amount)
- Doubles: ₹800 (per person)
- Mixed/Multiple: Correct sum

### **3.3 Payment Step**
**Test Payment Details:**
```
Amount: ₹1400 (Singles ₹600 + Doubles ₹800)
Test Card: 4111 1111 1111 1111
CVV: 123
Expiry: 12/25
```

**Steps:**
1. Review registration summary
2. Tap "Proceed to Payment"
3. Complete Razorpay test payment
4. Wait for confirmation

**Expected Results:**
- ✅ Payment summary accurate
- ✅ Razorpay checkout opens
- ✅ Test payment succeeds
- ✅ Registration confirmed

**Error Testing:**
- Test failed payment scenarios
- Verify graceful error handling
- Check retry functionality

### **3.4 Confirmation Step**
**Steps:**
1. View registration confirmation
2. Check registration details
3. Tap "Done"

**Expected Results:**
- ✅ Registration ID displayed
- ✅ All details correct
- ✅ PDF generation initiated
- ✅ Navigate to Tournament List
- ✅ Success message shown

**Backend Validation:**
- Check Firestore → registrations collection
- Verify payment status = "SUCCESS"
- Confirm participant count updated

---

## **🏸 Test Scenario 4: My Matches Screen**

### **4.1 Match List Display**
**Steps:**
1. Navigate to My Matches
2. Check all tabs (All, Upcoming, Past)
3. Verify match cards

**Expected Results:**
- ✅ Match data loads correctly
- ✅ Tab filtering works
- ✅ Match status chips accurate
- ✅ Date/time formatting correct

### **4.2 Match Details**
**Validation Points:**
- Event category and type
- Partner information (if applicable)
- Opponent details
- Court and schedule info
- Score display (for completed matches)

---

## **👤 Test Scenario 5: Profile Management**

### **5.1 Profile Display**
**Steps:**
1. Navigate to Profile screen
2. View user information
3. Check tournament history

**Expected Results:**
- ✅ User details displayed correctly
- ✅ Profile photo placeholder shown
- ✅ Registration history listed
- ✅ Settings accessible

### **5.2 Profile Updates**
**Steps:**
1. Tap edit profile
2. Update phone number
3. Save changes

**Expected Results:**
- ✅ Changes saved to Firestore
- ✅ UI updated immediately
- ✅ No data loss

---

## **⚡ Test Scenario 6: Performance & Edge Cases**

### **6.1 Network Connectivity**
**Test Cases:**
1. **Offline Mode**: Disable network, test app behavior
2. **Poor Connection**: Simulate slow network
3. **Connection Recovery**: Re-enable network, verify sync

**Expected Results:**
- ✅ Graceful offline handling
- ✅ Loading states displayed
- ✅ Data syncs when reconnected
- ✅ No crashes or hangs

### **6.2 Memory & Performance**
**Test Cases:**
1. **Memory Usage**: Monitor during registration flow
2. **CPU Usage**: Check during data loading
3. **Battery Impact**: Extended usage test

**Expected Results:**
- ✅ Memory usage stable
- ✅ No memory leaks
- ✅ Smooth animations
- ✅ Responsive UI

### **6.3 Data Validation Edge Cases**
**Test Cases:**
1. **Invalid Dates**: Future birth dates
2. **Long Names**: 100+ character names
3. **Special Characters**: Unicode in names
4. **Duplicate Registrations**: Same user, same tournament

**Expected Results:**
- ✅ Proper validation messages
- ✅ No data corruption
- ✅ Graceful error handling
- ✅ User-friendly feedback

---

## **🛡️ Test Scenario 7: Security & Data Protection**

### **7.1 Authentication Security**
**Test Cases:**
1. **Session Management**: App backgrounding/foregrounding
2. **Token Expiry**: Long-term usage
3. **Unauthorized Access**: Protected screens

**Expected Results:**
- ✅ Session maintained properly
- ✅ Auto-login on app restart
- ✅ Protected routes enforced

### **7.2 Data Privacy**
**Test Cases:**
1. **Personal Data**: Verify data encryption
2. **Payment Info**: No sensitive data stored
3. **User Isolation**: User A can't see User B's data

**Expected Results:**
- ✅ Data properly secured
- ✅ No payment data in local storage
- ✅ User data isolation maintained

---

## **📊 Test Scenario 8: Admin Dashboard (Basic)**

### **8.1 Dashboard Loading**
**Steps:**
1. Access Admin Dashboard screen
2. View statistics cards
3. Check upcoming matches

**Expected Results:**
- ✅ Stats load correctly
- ✅ Tournament info displayed
- ✅ Action shortcuts functional
- ✅ Notifications visible

---

## **🚨 Critical Bug Testing Checklist**

### **High Priority Issues to Test:**

**Registration Flow:**
- [ ] Multiple event registration calculates correctly
- [ ] Partner information saves properly
- [ ] Payment failure doesn't create duplicate registrations
- [ ] Age eligibility enforced strictly

**Data Consistency:**
- [ ] Tournament participant count updates
- [ ] User registration list syncs
- [ ] Match generation from registrations
- [ ] Payment status propagation

**UI/UX Issues:**
- [ ] Loading states don't hang indefinitely
- [ ] Error messages are user-friendly
- [ ] Navigation stack maintains correctly
- [ ] Back button behavior consistent

**Performance:**
- [ ] Large tournament lists scroll smoothly
- [ ] Image loading doesn't block UI
- [ ] Database queries optimized
- [ ] Memory usage controlled

---

## **📋 Test Completion Criteria**

**✅ All Scenarios Must Pass:**
1. User can sign up and login successfully
2. Tournament browsing works flawlessly
3. Complete registration flow (including payment)
4. Match tracking displays correctly
5. Profile management functional
6. No critical crashes or data loss
7. Performance meets expectations
8. Security measures in place

**📊 Success Metrics:**
- **Registration Success Rate**: >95%
- **Payment Success Rate**: >98%
- **App Crash Rate**: <1%
- **User Flow Completion**: >90%
- **Performance**: <3s load times

---

## **🔄 Automated Testing Setup (Future)**

### **Unit Tests** (Next Phase)
```kotlin
// Example test cases to implement
class RegistrationViewModelTest {
    @Test
    fun `calculate age correctly from date of birth`()
    
    @Test
    fun `fee calculation for multiple events`()
    
    @Test
    fun `validate eligible categories for age`()
}
```

### **Integration Tests**
```kotlin
// Firebase integration testing
class FirestoreRepositoryTest {
    @Test
    fun `create registration successfully`()
    
    @Test
    fun `handle network errors gracefully`()
}
```

---

**🎯 This testing guide ensures ShuttleReg is production-ready with comprehensive coverage of all user journeys and edge cases!**