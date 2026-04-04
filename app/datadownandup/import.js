const { initializeFirebaseApp, restore } = require('firestore-export-import');
const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

// Đọc file cấu hình Firebase
const serviceAccount = require('./appConfig.json');

// Khởi tạo Firebase App và lấy Firestore instance
const db = initializeFirebaseApp(serviceAccount);

/**
 * Hàm chuyển đổi timestamp từ format {_seconds, _nanoseconds} sang Firestore Timestamp
 */
function convertTimestamps(obj) {
    if (!obj || typeof obj !== 'object') {
        return obj;
    }

    // Kiểm tra nếu là timestamp object
    if (obj._seconds !== undefined && obj._nanoseconds !== undefined) {
        return admin.firestore.Timestamp.fromMillis(obj._seconds * 1000 + obj._nanoseconds / 1000000);
    }

    // Duyệt qua tất cả các thuộc tính
    if (Array.isArray(obj)) {
        return obj.map(item => convertTimestamps(item));
    }

    const converted = {};
    for (const key in obj) {
        converted[key] = convertTimestamps(obj[key]);
    }
    return converted;
}

const runImport = async () => {
    try {
        const inputPath = path.resolve(__dirname, 'backup.json');

        if (!fs.existsSync(inputPath)) {
            console.error('❌ Không tìm thấy file backup.json tại:', inputPath);
            return;
        }

        console.log('🚀 Đang đẩy dữ liệu từ file backup lên Firestore...');

        // Đọc và parse file JSON
        const rawData = fs.readFileSync(inputPath, 'utf8');
        let jsonData = JSON.parse(rawData);

        // Unwrap __collections__ nếu có
        if (jsonData.__collections__) {
            jsonData = jsonData.__collections__;
        }

        // Chuyển đổi tất cả timestamps
        console.log('🔄 Đang chuyển đổi timestamps...');
        jsonData = convertTimestamps(jsonData);

        /**
         * Truyền Firestore instance (db) và dữ liệu vào hàm restore
         */
        await restore(db, jsonData);

        console.log('✅ Import thành công!');

    } catch (e) {
        console.error('❌ Lỗi chi tiết:', e.message);
        console.error('Stack trace:', e.stack);
    } finally {
        process.exit();
    }
};

runImport();