# 📸 Screenshots & UI Flow - Chức năng Scan Hóa Đơn

## 🎨 Màn hình 1: Thêm giao dịch với nút Scan

### Layout
```
┌─────────────────────────────────────┐
│  Thêm giao dịch          [📷 Scan]  │ ← Header với nút Scan
├─────────────────────────────────────┤
│  ┌─────────────┬─────────────┐      │
│  │  Chi tiêu   │  Thu nhập   │      │ ← Toggle
│  └─────────────┴─────────────┘      │
│                                      │
│  Số tiền                             │
│  ┌─────────────────────────────┐    │
│  │ 0                         đ │    │ ← Input số tiền
│  └─────────────────────────────┘    │
│                                      │
│  Danh mục                            │
│  ┌───┬───┬───┬───┐                  │
│  │🍜 │🚕 │🛒 │⚡ │                  │ ← Grid danh mục
│  │Ăn │Di │Mua│Hóa│                  │
│  ├───┼───┼───┼───┤                  │
│  │🎮 │💊 │📚 │📦 │                  │
│  │Giải│Sức│Giáo│Khác│                │
│  └───┴───┴───┴───┘                  │
│                                      │
│  Ghi chú (tuỳ chọn)                  │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │      Lưu giao dịch          │    │ ← Button
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Nút "Scan" ở góc trên phải với icon camera
- Màu của nút theo theme (đỏ cho chi tiêu, xanh cho thu nhập)
- Border outline để nổi bật

---

## 🎨 Màn hình 2: Camera Preview

### Layout
```
┌─────────────────────────────────────┐
│  ← Scan Hóa Đơn                     │ ← Top bar
├─────────────────────────────────────┤
│                                      │
│  ┌─────────────────────────────┐    │
│  │ Đặt hóa đơn trong khung hình│    │ ← Hướng dẫn
│  └─────────────────────────────┘    │
│                                      │
│                                      │
│         [CAMERA PREVIEW]             │ ← Live camera
│                                      │
│                                      │
│                                      │
│                                      │
│                                      │
│              ┌───┐                   │
│              │ 📷 │                  │ ← Nút chụp (tròn lớn)
│              └───┘                   │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Full screen camera preview
- Card hướng dẫn ở trên với background đen mờ
- Nút chụp lớn, tròn, màu primary
- Icon camera trắng

---

## 🎨 Màn hình 3: Đang xử lý

### Layout
```
┌─────────────────────────────────────┐
│  ← Scan Hóa Đơn                     │
├─────────────────────────────────────┤
│                                      │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │    [ẢNH HÓA ĐƠN ĐÃ CHỤP]    │    │ ← Preview ảnh
│  │                             │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │         ⏳ Loading           │    │ ← Loading indicator
│  │  Đang phân tích hóa đơn...  │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                      │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Hiển thị ảnh đã chụp
- Circular progress indicator
- Text "Đang phân tích hóa đơn..."

---

## 🎨 Màn hình 4: Kết quả thành công

### Layout
```
┌─────────────────────────────────────┐
│  ← Scan Hóa Đơn                     │
├─────────────────────────────────────┤
│                                      │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │    [ẢNH HÓA ĐƠN ĐÃ CHỤP]    │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │ Kết quả scan                │    │
│  ├─────────────────────────────┤    │
│  │ Cửa hàng:      Circle K     │    │
│  │ Tổng tiền:     47,000 đ     │    │ ← Màu primary
│  │ Ngày:          16/04/2026   │    │
│  ├─────────────────────────────┤    │
│  │ Các mục:                    │    │
│  │ • Coca Cola        15,000   │    │
│  │ • Snack Oishi      12,000   │    │
│  │ • Bánh mì          20,000   │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌──────────┐  ┌──────────────┐     │
│  │ ✕ Chụp lại│  │ ✓ Xác nhận   │    │ ← Buttons
│  └──────────┘  └──────────────┘     │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Card kết quả với thông tin rõ ràng
- Tổng tiền nổi bật với màu primary và font lớn
- 2 nút: Chụp lại (outline) và Xác nhận (filled)
- Icon check và close

---

## 🎨 Màn hình 5: Lỗi scan

### Layout
```
┌─────────────────────────────────────┐
│  ← Scan Hóa Đơn                     │
├─────────────────────────────────────┤
│                                      │
│  ┌─────────────────────────────┐    │
│  │                             │    │
│  │    [ẢNH HÓA ĐƠN ĐÃ CHỤP]    │    │
│  │                             │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │ ⚠️ Lỗi scan hóa đơn          │    │ ← Error card (màu đỏ nhạt)
│  │                             │    │
│  │ Không thể nhận dạng hóa đơn │    │
│  │ Vui lòng thử lại            │    │
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │         Thử lại             │    │ ← Button
│  └─────────────────────────────┘    │
│                                      │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Error card với background màu errorContainer
- Icon warning
- Message rõ ràng
- Nút "Thử lại"

---

## 🎨 Màn hình 6: Không có quyền camera

### Layout
```
┌─────────────────────────────────────┐
│  ← Scan Hóa Đơn                     │
├─────────────────────────────────────┤
│                                      │
│                                      │
│                                      │
│              📷                      │ ← Icon camera lớn
│                                      │
│     Cần quyền truy cập camera       │ ← Title
│                                      │
│  Vui lòng cấp quyền camera          │ ← Description
│     để scan hóa đơn                 │
│                                      │
│                                      │
│                                      │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Centered layout
- Icon camera lớn màu primary
- Text hướng dẫn rõ ràng

---

## 🎨 Màn hình 7: Sau khi xác nhận (Back to Add Transaction)

### Layout
```
┌─────────────────────────────────────┐
│  Thêm giao dịch          [📷 Scan]  │
├─────────────────────────────────────┤
│  ┌─────────────┬─────────────┐      │
│  │  Chi tiêu   │  Thu nhập   │      │
│  └─────────────┴─────────────┘      │
│                                      │
│  Số tiền                             │
│  ┌─────────────────────────────┐    │
│  │ 47000                     đ │    │ ← ✅ Đã điền từ scan
│  └─────────────────────────────┘    │
│                                      │
│  Danh mục                            │
│  ┌───┬───┬───┬───┐                  │
│  │🍜 │🚕 │🛒 │⚡ │                  │
│  │Ăn │Di │Mua│Hóa│                  │ ← Cần chọn
│  ├───┼───┼───┼───┤                  │
│  │🎮 │💊 │📚 │📦 │                  │
│  │Giải│Sức│Giáo│Khác│                │
│  └───┴───┴───┴───┘                  │
│                                      │
│  Ghi chú (tuỳ chọn)                  │
│  ┌─────────────────────────────┐    │
│  │ Circle K                    │    │ ← ✅ Đã điền từ scan
│  └─────────────────────────────┘    │
│                                      │
│  ┌─────────────────────────────┐    │
│  │      Lưu giao dịch          │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Điểm nhấn
- Số tiền đã được điền tự động
- Ghi chú đã có tên cửa hàng
- User chỉ cần chọn danh mục và lưu

---

## 🎬 Animation & Transitions

### 1. Mở camera
- Slide up từ dưới lên
- Duration: 300ms
- Easing: EaseOut

### 2. Chụp ảnh
- Flash effect (white overlay fade in/out)
- Duration: 100ms

### 3. Loading
- Circular progress rotation
- Infinite animation

### 4. Hiển thị kết quả
- Fade in từ transparent
- Duration: 200ms

### 5. Quay lại form
- Slide down
- Duration: 300ms
- Auto-fill animation (số tiền count up)

---

## 🎨 Color Palette

### Primary Colors
- **Chi tiêu (Expense)**: `#FF7676` (Red)
- **Thu nhập (Income)**: `#2DC98E` (Green)

### Neutral Colors
- **Background**: `#FFFFFF` (White)
- **Card Background**: `#F8F8F8` (Light Gray)
- **Text Primary**: `#424242` (Dark Gray)
- **Text Secondary**: `#9E9E9E` (Gray)

### Status Colors
- **Success**: `#2DC98E` (Green)
- **Error**: `#FF7676` (Red)
- **Warning**: `#FFA726` (Orange)
- **Info**: `#42A5F5` (Blue)

---

## 📐 Spacing & Sizing

### Padding
- Screen padding: `20dp`
- Card padding: `16dp`
- Button padding: `12dp vertical, 24dp horizontal`

### Corner Radius
- Cards: `12dp`
- Buttons: `14dp`
- Input fields: `12dp`
- Camera button: `36dp` (circular)

### Font Sizes
- Title: `20sp` (Bold)
- Subtitle: `15sp` (SemiBold)
- Body: `14sp` (Regular)
- Caption: `12sp` (Regular)
- Amount: `20sp` (SemiBold)

### Icon Sizes
- Small: `18dp`
- Medium: `24dp`
- Large: `32dp`
- Camera button: `64dp`

---

## 🎯 Interactive States

### Nút "Scan"
- **Normal**: Outline với màu primary
- **Pressed**: Background primary với alpha 0.1
- **Disabled**: Gray với alpha 0.5

### Nút chụp ảnh
- **Normal**: Primary color, elevation 6dp
- **Pressed**: Scale 0.95, elevation 8dp

### Nút "Xác nhận"
- **Normal**: Filled primary
- **Pressed**: Darker primary
- **Disabled**: Gray

### Nút "Chụp lại"
- **Normal**: Outline
- **Pressed**: Background với alpha 0.1

---

## 📱 Responsive Design

### Portrait Mode (Default)
- Full width components
- Vertical scroll enabled
- Camera preview: 4:3 ratio

### Landscape Mode
- Split screen (camera + result)
- Horizontal layout for buttons
- Camera preview: 16:9 ratio

### Tablet
- Larger preview image
- Side-by-side layout
- Bigger buttons and text

---

## ♿ Accessibility

### Content Descriptions
- Camera button: "Chụp ảnh hóa đơn"
- Scan button: "Scan hóa đơn"
- Back button: "Quay lại"
- Confirm button: "Xác nhận kết quả"
- Retake button: "Chụp lại"

### Minimum Touch Target
- All buttons: 48dp x 48dp minimum

### Color Contrast
- Text on background: 4.5:1 minimum
- Icons on background: 3:1 minimum

### Screen Reader Support
- All images have content descriptions
- Form fields have labels
- Error messages are announced

---

**Note cho Designer:**
- Sử dụng Material Design 3 guidelines
- Maintain consistency với existing app design
- Test trên nhiều kích thước màn hình
- Đảm bảo accessibility standards
