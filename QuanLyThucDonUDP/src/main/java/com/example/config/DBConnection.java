package com.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp chịu trách nhiệm tạo kết nối từ ứng dụng Java đến cơ sở dữ liệu MySQL.
 * Tách cấu hình kết nối ra một nơi giúp các DAO dùng chung và dễ sửa thông tin DB.
 */
public class DBConnection {

    // Địa chỉ MySQL: localhost là máy hiện tại, 3306 là cổng mặc định,
    // quan_ly_thuc_don là tên database cần kết nối.
    private static final String URL =
            "jdbc:mysql://localhost:3306/quan_ly_thuc_don?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh";

    // Tài khoản MySQL dùng để đăng nhập; môi trường XAMPP thường dùng root.
    private static final String USER = "root";
    // Mật khẩu MySQL. Để rỗng nếu tài khoản root của bạn không đặt mật khẩu.
    private static final String PASSWORD = "";

    // Mở và trả về một kết nối mới để DAO thực hiện câu lệnh SQL.
    // SQLException được ném ra ngoài để nơi gọi có thể xử lý lỗi kết nối.
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}