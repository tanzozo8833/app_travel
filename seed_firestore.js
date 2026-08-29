/**
 * ==============================================================================
 * LEVART - SCRIPT KHỞI TẠO DỮ LIỆU CLOUD FIRESTORE (IDEMPOTENT SEED SCRIPT)
 * ==============================================================================
 * Script này chạy trên máy tính của bạn (Node.js) để nạp danh sách người dùng mẫu,
 * bài viết mẫu và bình luận vào Cloud Firestore mà KHÔNG tạo bản ghi trùng lặp
 * nhờ sử dụng mã định danh (Document ID) cố định và cơ chế merge: true.
 *
 * YÊU CẦU:
 * 1. Đã cài đặt Node.js (phiên bản 16 trở lên)
 * 2. Tải file khoá quản trị `serviceAccountKey.json` từ Firebase Console và đặt cùng thư mục
 * 3. File dữ liệu `firestore_seed_data.json` đặt cùng thư mục
 * ==============================================================================
 */

const fs = require('fs');
const path = require('path');

// 1. Kiểm tra sự tồn tại của file khoá quản trị
const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
const seedDataPath = path.join(__dirname, 'firestore_seed_data.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error('\n❌ [LỖI THIẾU KHOÁ QUẢN TRỊ]');
  console.error('Không tìm thấy tệp "serviceAccountKey.json" trong thư mục hiện tại.');
  console.error('\n👉 HƯỚNG DẪN LẤY KHOÁ:');
  console.error('1. Mở Firebase Console: https://console.firebase.google.com/');
  console.error('2. Chọn dự án của bạn > Bấm vào biểu tượng Bánh răng (Project settings)');
  console.error('3. Chuyển sang tab "Service accounts"');
  console.error('4. Nhấn nút "Generate new private key" (Tạo khoá riêng tư mới)');
  console.error('5. Đổi tên tệp tải về thành "serviceAccountKey.json" và đặt vào cùng thư mục với script này.');
  console.error('⚠️  LƯU Ý BẢO MẬT: Tuyệt đối KHÔNG đẩy tệp này lên GitHub!\n');
  process.exit(1);
}

if (!fs.existsSync(seedDataPath)) {
  console.error(`\n❌ [LỖI THIẾU DỮ LIỆU] Không tìm thấy tệp "${seedDataPath}"\n`);
  process.exit(1);
}

// 2. Khởi tạo Firebase Admin SDK
let admin;
try {
  // firebase-admin v13+ dùng API modular: require('firebase-admin') không còn .credential
  admin = {
    app: require('firebase-admin/app'),
    firestore: require('firebase-admin/firestore'),
  };
} catch (e) {
  console.error('\n❌ [LỖI THIẾU THƯ VIỆN]');
  console.error('Chưa cài đặt thư viện firebase-admin. Hãy chạy lệnh sau trước khi chạy script:');
  console.error('👉 npm install firebase-admin\n');
  process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

admin.app.initializeApp({
  credential: admin.app.cert(serviceAccount)
});

const db = admin.firestore.getFirestore();

async function seedFirestore() {
  console.log('\n🚀 Bắt đầu quá trình nạp dữ liệu lên Cloud Firestore...');
  
  const rawData = fs.readFileSync(seedDataPath, 'utf8');
  const seedData = JSON.parse(rawData);

  const users = seedData.users || [];
  const posts = seedData.posts || [];

  console.log(`📦 Tìm thấy: ${users.length} hồ sơ người dùng, ${posts.length} bài viết.`);

  // 1. Đẩy hồ sơ người dùng mẫu vào collection "users"
  console.log('\n--- 1. Đang nạp hồ sơ người dùng (users) ---');
  for (const user of users) {
    const userId = user.id;
    if (!userId) {
      console.warn('⚠️ Bỏ qua hồ sơ thiếu id:', user);
      continue;
    }
    
    // Lưu với Document ID cố định và merge: true
    await db.collection('users').doc(userId).set(user, { merge: true });
    console.log(`  ✅ [User] ${userId} (${user.displayName || user.username})`);
  }

  // 2. Đẩy bài viết vào collection "posts" và bình luận vào sub-collection "comments"
  console.log('\n--- 2. Đang nạp bài viết (posts) & bình luận (comments) ---');
  let totalComments = 0;
  for (const post of posts) {
    const postId = post.id;
    if (!postId) {
      console.warn('⚠️ Bỏ qua bài viết thiếu id:', post);
      continue;
    }

    // Tách subcollection comments ra khỏi dữ liệu bài viết
    const { comments, ...postData } = post;

    // Lưu bài viết
    await db.collection('posts').doc(postId).set(postData, { merge: true });
    console.log(`  ✅ [Post] ${postId} - "${postData.caption ? postData.caption.substring(0, 40) + '...' : ''}"`);

    // Lưu từng bình luận vào sub-collection /posts/{postId}/comments/{commentId}
    if (Array.isArray(comments) && comments.length > 0) {
      for (const comment of comments) {
        const commentId = comment.id;
        if (!commentId) continue;
        
        await db.collection('posts')
          .doc(postId)
          .collection('comments')
          .doc(commentId)
          .set(comment, { merge: true });
        
        totalComments++;
        console.log(`     💬 [Comment] ${commentId} bởi ${comment.authorName}`);
      }
    }
  }

  console.log('\n================================================================');
  console.log('🎉 HOÀN THÀNH NẠP DỮ LIỆU LÊN CLOUD FIRESTORE THÀNH CÔNG!');
  console.log(`- Người dùng đã nạp: ${users.length}`);
  console.log(`- Bài viết đã nạp:   ${posts.length}`);
  console.log(`- Bình luận đã nạp: ${totalComments}`);
  console.log('================================================================\n');
}

seedFirestore()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error('\n❌ Gặp lỗi khi nạp dữ liệu lên Firestore:', err);
    process.exit(1);
  });
