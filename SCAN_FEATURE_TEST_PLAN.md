# 🧪 Test Plan - Chức năng Scan Hóa Đơn

## 📋 Tổng quan

Test plan này bao gồm các test cases để đảm bảo chức năng scan hóa đơn hoạt động đúng và ổn định.

---

## 🎯 Test Objectives

1. Đảm bảo camera hoạt động đúng trên nhiều thiết bị
2. Xác minh ML Kit nhận dạng text chính xác
3. Kiểm tra thuật toán trích xuất thông tin
4. Đảm bảo UX flow mượt mà
5. Xác minh tích hợp với form giao dịch

---

## 🔧 Test Environment

### Thiết bị test
- [ ] Samsung Galaxy S21 (Android 13)
- [ ] Xiaomi Redmi Note 10 (Android 12)
- [ ] Google Pixel 6 (Android 14)
- [ ] Oppo A55 (Android 11)
- [ ] Tablet Samsung Tab S7 (Android 12)

### Điều kiện test
- [ ] Ánh sáng tốt (ngoài trời, ban ngày)
- [ ] Ánh sáng vừa (trong nhà, đèn)
- [ ] Ánh sáng yếu (tối, đèn mờ)
- [ ] Hóa đơn rõ nét
- [ ] Hóa đơn mờ/cũ
- [ ] Hóa đơn nhăn

---

## 📝 Test Cases

### 1. Camera Permission Tests

#### TC-001: Request camera permission (First time)
**Precondition:** App chưa có quyền camera  
**Steps:**
1. Mở màn hình "Thêm giao dịch"
2. Nhấn nút "Scan"
3. Hệ thống hiển thị dialog xin quyền camera
4. Nhấn "Cho phép"

**Expected Result:**
- ✅ Dialog permission hiển thị
- ✅ Camera mở sau khi cho phép
- ✅ Preview camera hoạt động

**Status:** [ ] Pass [ ] Fail

---

#### TC-002: Deny camera permission
**Precondition:** App chưa có quyền camera  
**Steps:**
1. Mở màn hình "Thêm giao dịch"
2. Nhấn nút "Scan"
3. Nhấn "Từ chối" trong dialog permission

**Expected Result:**
- ✅ Hiển thị màn hình "Cần quyền truy cập camera"
- ✅ Icon camera và text hướng dẫn hiển thị
- ✅ Không crash app

**Status:** [ ] Pass [ ] Fail

---

#### TC-003: Camera permission already granted
**Precondition:** App đã có quyền camera  
**Steps:**
1. Mở màn hình "Thêm giao dịch"
2. Nhấn nút "Scan"

**Expected Result:**
- ✅ Camera mở ngay lập tức
- ✅ Không hiển thị dialog permission
- ✅ Preview camera hoạt động

**Status:** [ ] Pass [ ] Fail

---

### 2. Camera Functionality Tests

#### TC-004: Camera preview displays correctly
**Precondition:** Có quyền camera  
**Steps:**
1. Mở camera scan
2. Quan sát preview

**Expected Result:**
- ✅ Preview hiển thị real-time
- ✅ Không bị lag/freeze
- ✅ Orientation đúng
- ✅ Hướng dẫn "Đặt hóa đơn trong khung hình" hiển thị

**Status:** [ ] Pass [ ] Fail

---

#### TC-005: Capture image
**Precondition:** Camera preview đang hoạt động  
**Steps:**
1. Đặt hóa đơn trong khung hình
2. Nhấn nút chụp (nút tròn lớn)

**Expected Result:**
- ✅ Flash effect xuất hiện
- ✅ Ảnh được capture
- ✅ Chuyển sang màn hình xử lý
- ✅ Loading indicator hiển thị

**Status:** [ ] Pass [ ] Fail

---

#### TC-006: Back button from camera
**Precondition:** Đang ở màn hình camera  
**Steps:**
1. Nhấn nút back (←)

**Expected Result:**
- ✅ Quay lại màn hình "Thêm giao dịch"
- ✅ Camera được release
- ✅ Form không bị thay đổi

**Status:** [ ] Pass [ ] Fail

---

### 3. Text Recognition Tests

#### TC-007: Scan hóa đơn siêu thị (format chuẩn)
**Test Data:**
```
Circle K
123 Nguyễn Huệ, Q.1

Coca Cola        15,000
Snack Oishi      12,000
Bánh mì          20,000
─────────────────────────
TỔNG CỘNG:       47,000đ
16/04/2026
```

**Steps:**
1. Chụp hóa đơn
2. Đợi xử lý

**Expected Result:**
- ✅ Tổng tiền: 47,000
- ✅ Cửa hàng: Circle K
- ✅ Ngày: 16/04/2026
- ✅ Items: 3 items

**Status:** [ ] Pass [ ] Fail

---

#### TC-008: Scan hóa đơn nhà hàng
**Test Data:**
```
PHỞ 24
456 Lê Lợi, Q.1

Phở bò đặc biệt   65,000
Trà đá             5,000
─────────────────────────
Tổng tiền:        70,000đ
```

**Steps:**
1. Chụp hóa đơn
2. Đợi xử lý

**Expected Result:**
- ✅ Tổng tiền: 70,000
- ✅ Cửa hàng: PHỞ 24
- ✅ Items: 2 items

**Status:** [ ] Pass [ ] Fail

---

#### TC-009: Scan hóa đơn với format số tiền khác nhau
**Test Data:**
- Format 1: `100,000` (dấu phẩy)
- Format 2: `100.000` (dấu chấm)
- Format 3: `100000` (không dấu)
- Format 4: `100,000đ` (có ký hiệu)
- Format 5: `100,000 VND`

**Steps:**
1. Chụp từng hóa đơn với format khác nhau

**Expected Result:**
- ✅ Tất cả format đều được nhận dạng đúng
- ✅ Số tiền được parse thành số

**Status:** [ ] Pass [ ] Fail

---

#### TC-010: Scan hóa đơn tiếng Anh
**Test Data:**
```
Starbucks Coffee
789 Dong Khoi, D.1

Latte Grande      75,000
Croissant         45,000
─────────────────────────
Total:           120,000đ
```

**Steps:**
1. Chụp hóa đơn tiếng Anh

**Expected Result:**
- ✅ Nhận dạng từ khóa "Total"
- ✅ Tổng tiền: 120,000
- ✅ Cửa hàng: Starbucks Coffee

**Status:** [ ] Pass [ ] Fail

---

#### TC-011: Scan hóa đơn mờ/chất lượng kém
**Precondition:** Hóa đơn cũ, in mờ  
**Steps:**
1. Chụp hóa đơn chất lượng kém

**Expected Result:**
- ✅ Vẫn cố gắng nhận dạng
- ✅ Hiển thị raw text nếu không parse được
- ✅ Không crash

**Status:** [ ] Pass [ ] Fail

---

#### TC-012: Scan hóa đơn không có text
**Test Data:** Ảnh trắng hoặc không có text  
**Steps:**
1. Chụp ảnh không có text

**Expected Result:**
- ✅ Hiển thị thông báo lỗi
- ✅ Cho phép chụp lại
- ✅ Không crash

**Status:** [ ] Pass [ ] Fail

---

### 4. Result Display Tests

#### TC-013: Display scan result successfully
**Precondition:** Scan thành công  
**Steps:**
1. Xem màn hình kết quả

**Expected Result:**
- ✅ Ảnh hóa đơn hiển thị
- ✅ Card kết quả hiển thị đầy đủ thông tin
- ✅ Tổng tiền nổi bật (màu primary, font lớn)
- ✅ 2 nút: "Chụp lại" và "Xác nhận"

**Status:** [ ] Pass [ ] Fail

---

#### TC-014: Retake photo
**Precondition:** Đang ở màn hình kết quả  
**Steps:**
1. Nhấn nút "Chụp lại"

**Expected Result:**
- ✅ Quay lại camera preview
- ✅ Kết quả cũ bị xóa
- ✅ Có thể chụp lại

**Status:** [ ] Pass [ ] Fail

---

#### TC-015: Confirm scan result
**Precondition:** Đang ở màn hình kết quả  
**Steps:**
1. Nhấn nút "Xác nhận"

**Expected Result:**
- ✅ Quay lại màn hình "Thêm giao dịch"
- ✅ Số tiền được điền tự động
- ✅ Ghi chú có tên cửa hàng
- ✅ User có thể chỉnh sửa

**Status:** [ ] Pass [ ] Fail

---

#### TC-016: Display error result
**Precondition:** Scan thất bại  
**Steps:**
1. Xem màn hình lỗi

**Expected Result:**
- ✅ Error card hiển thị (màu đỏ nhạt)
- ✅ Icon warning
- ✅ Message lỗi rõ ràng
- ✅ Nút "Thử lại"

**Status:** [ ] Pass [ ] Fail

---

### 5. Integration Tests

#### TC-017: Auto-fill transaction form
**Precondition:** Scan thành công với tổng tiền 50,000 và cửa hàng "Circle K"  
**Steps:**
1. Xác nhận kết quả scan
2. Kiểm tra form "Thêm giao dịch"

**Expected Result:**
- ✅ Số tiền = "50000"
- ✅ Ghi chú = "Circle K"
- ✅ Danh mục chưa chọn (cần user chọn)
- ✅ Type = Chi tiêu (default)

**Status:** [ ] Pass [ ] Fail

---

#### TC-018: Edit auto-filled data
**Precondition:** Form đã được auto-fill  
**Steps:**
1. Chỉnh sửa số tiền
2. Chỉnh sửa ghi chú
3. Chọn danh mục
4. Lưu giao dịch

**Expected Result:**
- ✅ Có thể chỉnh sửa tất cả fields
- ✅ Lưu thành công với dữ liệu đã chỉnh sửa

**Status:** [ ] Pass [ ] Fail

---

#### TC-019: Scan multiple times
**Steps:**
1. Scan hóa đơn 1 → Xác nhận
2. Xóa form
3. Scan hóa đơn 2 → Xác nhận

**Expected Result:**
- ✅ Mỗi lần scan đều hoạt động đúng
- ✅ Không bị conflict dữ liệu
- ✅ Không memory leak

**Status:** [ ] Pass [ ] Fail

---

#### TC-020: Cancel scan and enter manually
**Steps:**
1. Nhấn "Scan"
2. Nhấn back từ camera
3. Nhập thông tin thủ công

**Expected Result:**
- ✅ Form vẫn trống
- ✅ Có thể nhập thủ công bình thường

**Status:** [ ] Pass [ ] Fail

---

### 6. Performance Tests

#### TC-021: Scan processing time
**Steps:**
1. Chụp hóa đơn
2. Đo thời gian từ capture đến hiển thị kết quả

**Expected Result:**
- ✅ Thời gian < 3 giây (hóa đơn đơn giản)
- ✅ Thời gian < 5 giây (hóa đơn phức tạp)

**Status:** [ ] Pass [ ] Fail

---

#### TC-022: Memory usage
**Steps:**
1. Mở camera
2. Chụp 10 hóa đơn liên tiếp
3. Kiểm tra memory usage

**Expected Result:**
- ✅ Không memory leak
- ✅ Memory được release sau mỗi scan
- ✅ App không crash

**Status:** [ ] Pass [ ] Fail

---

#### TC-023: Battery consumption
**Steps:**
1. Sử dụng camera scan trong 10 phút
2. Kiểm tra battery usage

**Expected Result:**
- ✅ Battery drain hợp lý (< 5% trong 10 phút)

**Status:** [ ] Pass [ ] Fail

---

### 7. Edge Cases Tests

#### TC-024: Rotate device during scan
**Steps:**
1. Mở camera
2. Xoay thiết bị (portrait ↔ landscape)

**Expected Result:**
- ✅ Camera preview adapt đúng orientation
- ✅ Không crash
- ✅ UI vẫn hiển thị đúng

**Status:** [ ] Pass [ ] Fail

---

#### TC-025: Background app during scan
**Steps:**
1. Đang scan hóa đơn (loading)
2. Nhấn home button
3. Quay lại app

**Expected Result:**
- ✅ Scan tiếp tục hoặc hiển thị lỗi
- ✅ Không crash
- ✅ Có thể retry

**Status:** [ ] Pass [ ] Fail

---

#### TC-026: Low storage
**Precondition:** Thiết bị gần hết bộ nhớ  
**Steps:**
1. Thử scan hóa đơn

**Expected Result:**
- ✅ Hiển thị thông báo lỗi rõ ràng
- ✅ Không crash

**Status:** [ ] Pass [ ] Fail

---

#### TC-027: No internet connection
**Precondition:** Tắt wifi và mobile data  
**Steps:**
1. Scan hóa đơn

**Expected Result:**
- ✅ Vẫn hoạt động bình thường (ML Kit offline)
- ✅ Không hiển thị lỗi network

**Status:** [ ] Pass [ ] Fail

---

#### TC-028: Very long receipt
**Test Data:** Hóa đơn siêu thị với 50+ items  
**Steps:**
1. Scan hóa đơn dài

**Expected Result:**
- ✅ Nhận dạng được tổng tiền
- ✅ Hiển thị tối đa 10 items
- ✅ Không crash

**Status:** [ ] Pass [ ] Fail

---

### 8. Accessibility Tests

#### TC-029: Screen reader support
**Precondition:** Bật TalkBack  
**Steps:**
1. Navigate qua các elements

**Expected Result:**
- ✅ Tất cả buttons có content description
- ✅ Screen reader đọc đúng labels
- ✅ Focus order hợp lý

**Status:** [ ] Pass [ ] Fail

---

#### TC-030: Large text support
**Precondition:** Bật Large Text trong settings  
**Steps:**
1. Mở màn hình scan

**Expected Result:**
- ✅ Text scale đúng
- ✅ UI không bị vỡ layout

**Status:** [ ] Pass [ ] Fail

---

#### TC-031: Color contrast
**Steps:**
1. Kiểm tra contrast ratio của text và background

**Expected Result:**
- ✅ Contrast ratio ≥ 4.5:1 cho text
- ✅ Contrast ratio ≥ 3:1 cho icons

**Status:** [ ] Pass [ ] Fail

---

## 📊 Test Metrics

### Coverage Goals
- [ ] Unit tests: 80%
- [ ] Integration tests: 70%
- [ ] UI tests: 60%

### Success Criteria
- [ ] 100% critical test cases pass
- [ ] 95% high priority test cases pass
- [ ] 90% medium priority test cases pass
- [ ] No P0/P1 bugs
- [ ] < 5 P2 bugs

---

## 🐛 Bug Severity Levels

### P0 - Critical
- App crash
- Data loss
- Security issues

### P1 - High
- Feature không hoạt động
- Blocking user flow
- Performance issues nghiêm trọng

### P2 - Medium
- UI issues
- Minor functionality issues
- Performance issues nhẹ

### P3 - Low
- Cosmetic issues
- Nice-to-have features
- Minor UX improvements

---

## 📝 Test Report Template

```
Test Date: [Date]
Tester: [Name]
Device: [Device Model]
OS Version: [Android Version]
App Version: [Version]

Total Test Cases: [Number]
Passed: [Number]
Failed: [Number]
Blocked: [Number]

Critical Bugs: [Number]
High Priority Bugs: [Number]
Medium Priority Bugs: [Number]
Low Priority Bugs: [Number]

Notes:
[Additional notes]
```

---

## ✅ Sign-off Checklist

- [ ] All critical test cases passed
- [ ] All high priority bugs fixed
- [ ] Performance meets requirements
- [ ] Accessibility requirements met
- [ ] Documentation complete
- [ ] Code review approved
- [ ] QA sign-off
- [ ] Product owner approval

---

**Test Plan Version:** 1.0  
**Last Updated:** 2026-04-16  
**Next Review:** 2026-05-16
