// Sample Data Script for ShuttleReg Firestore
// Run this in Firebase Console → Firestore → Add data manually

// 1. Create "tournaments" collection
const sampleTournaments = [
  {
    id: "tournament_2024_summer",
    name: "Summer Badminton Championship 2024",
    description: "Annual badminton tournament featuring all age categories from U9 to Open. Professional referees, quality courts, and exciting prizes await!",
    startDate: new Date("2024-08-15"),
    endDate: new Date("2024-08-17"),
    venue: "City Sports Complex, Main Hall",
    availableEvents: ["U9", "U11", "U13", "U15", "U17", "U19", "MENS_OPEN", "WOMENS_OPEN"],
    eventPrices: {
      "U9_SINGLES": 300,
      "U9_DOUBLES": 500,
      "U11_SINGLES": 400,
      "U11_DOUBLES": 600,
      "U13_SINGLES": 500,
      "U13_DOUBLES": 700,
      "U15_SINGLES": 600,
      "U15_DOUBLES": 800,
      "U17_SINGLES": 700,
      "U17_DOUBLES": 900,
      "U19_SINGLES": 800,
      "U19_DOUBLES": 1000,
      "MENS_OPEN_SINGLES": 1000,
      "MENS_OPEN_DOUBLES": 1200,
      "MENS_OPEN_MIXED_DOUBLES": 1200,
      "WOMENS_OPEN_SINGLES": 1000,
      "WOMENS_OPEN_DOUBLES": 1200,
      "WOMENS_OPEN_MIXED_DOUBLES": 1200
    },
    registrationDeadline: new Date("2024-08-10T23:59:59"),
    maxParticipants: 200,
    currentParticipants: 47,
    isActive: true,
    rules: "Standard BWF rules apply. Age proof required. Equipment will be provided.",
    contactInfo: {
      organizerName: "Sports Club Association",
      email: "tournament@sportsclub.com",
      phone: "+91 9876543210",
      website: "www.sportsclub.com",
      address: "123 Sports Street, City Center"
    },
    bannerImageUrl: null,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    id: "tournament_2024_winter",
    name: "Winter Indoor Championship 2024",
    description: "Premier indoor badminton tournament with enhanced safety protocols and professional live streaming.",
    startDate: new Date("2024-12-20"),
    endDate: new Date("2024-12-22"),
    venue: "Grand Sports Arena, Indoor Courts 1-6",
    availableEvents: ["U13", "U15", "U17", "U19", "MENS_OPEN", "WOMENS_OPEN"],
    eventPrices: {
      "U13_SINGLES": 600,
      "U13_DOUBLES": 800,
      "U15_SINGLES": 700,
      "U15_DOUBLES": 900,
      "U17_SINGLES": 800,
      "U17_DOUBLES": 1000,
      "U19_SINGLES": 900,
      "U19_DOUBLES": 1100,
      "MENS_OPEN_SINGLES": 1200,
      "MENS_OPEN_DOUBLES": 1400,
      "MENS_OPEN_MIXED_DOUBLES": 1400,
      "WOMENS_OPEN_SINGLES": 1200,
      "WOMENS_OPEN_DOUBLES": 1400,
      "WOMENS_OPEN_MIXED_DOUBLES": 1400
    },
    registrationDeadline: new Date("2024-12-15T23:59:59"),
    maxParticipants: 150,
    currentParticipants: 23,
    isActive: true,
    rules: "BWF rules. Temperature checks mandatory. Masks required in common areas.",
    contactInfo: {
      organizerName: "Winter Sports Federation",
      email: "winter@badminton.org",
      phone: "+91 9876543211",
      website: "www.winterbadminton.org",
      address: "456 Winter Street, North District"
    },
    bannerImageUrl: null,
    createdAt: Date.now(),
    updatedAt: Date.now()
  },
  {
    id: "tournament_2024_junior",
    name: "Junior Development Cup 2024",
    description: "Special tournament focused on junior players (U9 to U17) with coaching clinics and skill development sessions.",
    startDate: new Date("2024-09-28"),
    endDate: new Date("2024-09-29"),
    venue: "Youth Development Center, Courts A-D",
    availableEvents: ["U9", "U11", "U13", "U15", "U17"],
    eventPrices: {
      "U9_SINGLES": 200,
      "U9_DOUBLES": 350,
      "U11_SINGLES": 250,
      "U11_DOUBLES": 400,
      "U13_SINGLES": 300,
      "U13_DOUBLES": 450,
      "U15_SINGLES": 350,
      "U15_DOUBLES": 500,
      "U17_SINGLES": 400,
      "U17_DOUBLES": 550
    },
    registrationDeadline: new Date("2024-09-23T23:59:59"),
    maxParticipants: 100,
    currentParticipants: 67,
    isActive: true,
    rules: "Junior-friendly rules. Coaching sessions included. Parent supervision required for U11 and below.",
    contactInfo: {
      organizerName: "Junior Badminton Academy",
      email: "junior@academy.com",
      phone: "+91 9876543212",
      website: "www.junioracademy.com",
      address: "789 Youth Lane, Education District"
    },
    bannerImageUrl: null,
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
];

// 2. Create sample users (for testing)
const sampleUsers = [
  {
    id: "user_demo_1",
    fullName: "Rahul Sharma",
    email: "rahul.sharma@email.com",
    phone: "+91 9876543213",
    dateOfBirth: new Date("2010-05-15"), // U15 eligible
    gender: "MALE",
    clubName: "City Badminton Club",
    profilePhotoUrl: null,
    eligibleEvents: ["U15", "U17", "U19"],
    savedPartners: [],
    createdAt: Date.now(),
    isEmailVerified: true,
    isPhoneVerified: false
  },
  {
    id: "user_demo_2",
    fullName: "Priya Patel",
    email: "priya.patel@email.com",
    phone: "+91 9876543214",
    dateOfBirth: new Date("2008-12-20"), // U17 eligible
    gender: "FEMALE",
    clubName: "Smash Academy",
    profilePhotoUrl: null,
    eligibleEvents: ["U17", "U19", "WOMENS_OPEN"],
    savedPartners: [
      {
        id: "partner_1",
        name: "Ananya Gupta",
        phone: "+91 9876543215",
        email: "ananya@email.com",
        gender: "FEMALE",
        isRegistered: false
      }
    ],
    createdAt: Date.now(),
    isEmailVerified: true,
    isPhoneVerified: true
  }
];

// 3. Create sample registrations
const sampleRegistrations = [
  {
    id: "reg_demo_1",
    userId: "user_demo_1",
    tournamentId: "tournament_2024_summer",
    selectedEvents: [
      {
        category: "U15",
        type: "SINGLES",
        partnerInfo: null,
        price: 600
      },
      {
        category: "U15",
        type: "DOUBLES",
        partnerInfo: {
          id: "partner_rahul",
          name: "Amit Kumar",
          phone: "+91 9876543216",
          email: "amit@email.com",
          gender: "MALE",
          isRegistered: false
        },
        price: 800
      }
    ],
    totalAmount: 1400,
    paymentStatus: "SUCCESS",
    paymentId: "pay_demo_123456",
    registrationDate: new Date(),
    pdfUrl: null,
    status: "CONFIRMED",
    notes: "Test registration",
    createdAt: Date.now(),
    updatedAt: Date.now()
  }
];

// Instructions for adding to Firestore:
// 1. Go to Firebase Console → Firestore Database
// 2. Click "Start collection"
// 3. Collection ID: "tournaments"
// 4. Add each tournament object as a document (use the ID field as document ID)
// 5. Repeat for "users" and "registrations" collections

console.log("Sample data ready for Firestore!");
console.log("Tournaments:", sampleTournaments.length);
console.log("Users:", sampleUsers.length);
console.log("Registrations:", sampleRegistrations.length);

// Export for copy-paste into Firebase Console
module.exports = {
  tournaments: sampleTournaments,
  users: sampleUsers,
  registrations: sampleRegistrations
};