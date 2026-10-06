package com.example.foodhub.data

import com.example.foodhub.data.model.NotificationType

/**
 * Lưu thông tin cá nhân của người dùng trong bộ nhớ (dùng chung cho toàn app).
 * Nhờ vậy khi sửa ở trang "Thông tin người dùng" thì tên/avatar hiển thị
 * ở trang "Tôi" và các nơi khác cũng được cập nhật theo.
 */
object UserProfileManager {

    // Tên đăng nhập không cho đổi ở đây.
    var username: String = "foodee_uwfdcvsm"
        private set

    var name: String = "Trường Thanh Toàn"
        private set
    var phone: String = "+84 123 456 789"
        private set
    var email: String = "truong@email.com"
        private set
    var gender: String = ""
        private set
    var birthday: String = ""
        private set
    var job: String = ""
        private set

    /** Chữ cái đầu tên, dùng làm chữ trong vòng tròn avatar. */
    val avatarLetter: String
        get() = name.trim().firstOrNull()?.uppercase() ?: "T"

    /** Được gọi lại mỗi khi thông tin thay đổi, để các màn hình khác tự làm mới. */
    var onChanged: (() -> Unit)? = null

    fun update(
        name: String,
        phone: String,
        email: String,
        gender: String,
        birthday: String,
        job: String
    ) {
        this.name = name
        this.phone = phone
        this.email = email
        this.gender = gender
        this.birthday = birthday
        this.job = job
        NotificationStore.add(
            NotificationType.SYSTEM, "✏️", "Cập nhật thông tin thành công",
            "Thông tin cá nhân của bạn đã được lưu."
        )
        onChanged?.invoke()
    }
}
