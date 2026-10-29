
package com.example.model;

import java.math.BigDecimal;

/**
 * Model đại diện cho một món ăn trong bảng mon_an.
 * Dùng để chuyển dữ liệu truy vấn MySQL thành đối tượng Java và đưa kết quả lên GUI.
 */
public class MonAn {
    // Mã định danh duy nhất của món ăn.
    private int id;
    // Tên món ăn, dùng làm một trong các điều kiện tìm kiếm.
    private String tenMon;
    // Đường dẫn hoặc tên file hình ảnh món ăn nếu có.
    private String hinhAnh;
    // BigDecimal phù hợp lưu giá tiền vì cột MySQL dùng kiểu DECIMAL.
    private BigDecimal gia;
    // Khoảng thời gian phục vụ, ví dụ 06:00-10:00.
    private String thoiGianPhucVu;
    // Khóa ngoại đến loại món; dùng Integer vì giá trị có thể null.
    private Integer loaiMonId;

    // Constructor rỗng để tạo đối tượng trước khi gán dữ liệu.
    public MonAn() {
    }

    // Constructor đầy đủ để tạo món ăn từ các cột trong ResultSet.
    public MonAn(int id, String tenMon, String hinhAnh,
                 BigDecimal gia, String thoiGianPhucVu,
                 Integer loaiMonId) {
        this.id = id;
        this.tenMon = tenMon;
        this.hinhAnh = hinhAnh;
        this.gia = gia;
        this.thoiGianPhucVu = thoiGianPhucVu;
        this.loaiMonId = loaiMonId;
    }

    // Getter/setter cho phép các lớp DAO, Server và GUI truy cập thuộc tính.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    public String getThoiGianPhucVu() {
        return thoiGianPhucVu;
    }

    public void setThoiGianPhucVu(String thoiGianPhucVu) {
        this.thoiGianPhucVu = thoiGianPhucVu;
    }

    public Integer getLoaiMonId() {
        return loaiMonId;
    }

    public void setLoaiMonId(Integer loaiMonId) {
        this.loaiMonId = loaiMonId;
    }
}
