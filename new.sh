curl --location 'https://haloalignersbackend-production.up.railway.app/api/auth/register' \
--header 'Content-Type: application/json' \
--data '{
    "username": "drsmith",
    "password": "password123",
    "userRole": "DOCTOR",
    "fullName": "Dr. John Smith",
    "email": "john.smith@example.com",
    "phone": "1234567890",
    "gstNumber": "GSTIN12345",
    "clinicName": "Smith'\''s Dental Clinic"
}'