
package com.example.model;

/**
 * Model đại diện cho một loại món ăn trong bảng loai_mon của MySQL.
 * Các thuộc tính tương ứng với các cột id, ten_loai và mo_ta.
 */
public class LoaiMon {
    // Mã định danh của loại món ăn.
    private int id;
    // Tên loại món, ví dụ: Món chính, Đồ uống.
    private String tenLoai;
    // Mô tả bổ sung cho loại món; có thể rỗng hoặc null.
    private String moTa;

    // Constructor rỗng để tạo đối tượng trước khi gán thuộc tính.
    public LoaiMon() {
    }

    // Constructor đầy đủ, thuận tiện tạo Model từ một dòng dữ liệu truy vấn.
    public LoaiMon(int id, String tenLoai, String moTa) {
        this.id = id;
        this.tenLoai = tenLoai;
        this.moTa = moTa;
    }

    // Getter/setter cho phép lớp khác đọc hoặc cập nhật các thuộc tính.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}
