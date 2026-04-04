const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { backup } = require('firestore-export-import');
const fs = require('fs');

const serviceAccount = require('./appConfig.json');

initializeApp({
  credential: cert(serviceAccount)
});

const db = getFirestore();

// Danh sách 3 collection của đồ án Cinder's Soul
const collections = ['albums', 'artists', 'songs'];

const exportAll = async () => {
  try {
    console.log('Đang bắt đầu export dữ liệu từng collection...');
    let finalData = {};

    for (const col of collections) {
      console.log(`- Đang lấy dữ liệu từ: ${col}`);
      // Export từng collection một để tránh lỗi tham số
      const data = await backup(db, col);
      // Gộp vào đối tượng tổng
      Object.assign(finalData, data);
    }

    // Ghi tất cả vào một file duy nhất
    fs.writeFileSync('backup.json', JSON.stringify(finalData, null, 2));
    console.log('Export hoàn tất! File backup.json đã sẵn sàng.');
  } catch (error) {
    console.error('Lỗi chi tiết:', error);
  }
};

exportAll();