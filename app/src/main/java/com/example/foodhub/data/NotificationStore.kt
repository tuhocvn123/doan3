package com.example.foodhub.data

import com.example.foodhub.data.model.AppNotification
import com.example.foodhub.data.model.NotificationType
import com.example.foodhub.data.model.Order

/**
 * Quản lý danh sách thông báo (lưu trong bộ nhớ, có sẵn vài thông báo mẫu).
 * OrderManager gọi các hàm onOrderXxx() mỗi khi đơn hàng đổi trạng thái
 * nên thông báo đơn hàng sẽ tự xuất hiện.
 */
object NotificationStore {

    private val list = mutableListOf<AppNotification>()
    private var lastId = 0L

    /** Màn hình đang hiển thị thông báo/badge gán callback này để được refresh. */
    var onChanged: (() -> Unit)? = null

    /** Được gọi khi có thông báo MỚI (không gọi cho dữ liệu mẫu), dùng để hiện banner nhỏ. */
    var onNewNotification: ((AppNotification) -> Unit)? = null

    init {
        seedSampleNotifications()
    }

    // ─── Truy vấn ──────────────────────────────────────
    fun getAll(type: NotificationType?): List<AppNotification> {
        val result = if (type == null) list else list.filter { it.type == type }
        return result.sortedWith(
            compareByDescending<AppNotification> { it.time }.thenByDescending { it.id }
        )
    }

    fun unreadCount(type: NotificationType? = null): Int =
        list.count { !it.isRead && (type == null || it.type == type) }

    // ─── Thêm / sửa / xóa ──────────────────────────────
    fun add(
        type: NotificationType,
        emoji: String,
        title: String,
        message: String,
        orderId: String? = null,
        promoCode: String? = null
    ) {
        val n = AppNotification(
            id = nextId(),
            type = type,
            emoji = emoji,
            title = title,
            message = message,
            time = System.currentTimeMillis(),
            isRead = false,
            orderId = orderId,
            promoCode = promoCode
        )
        list.add(n)
        onChanged?.invoke()
        onNewNotification?.invoke(n)
    }

    fun markRead(id: Long) {
        val n = list.find { it.id == id } ?: return
        if (n.isRead) return
        n.isRead = true
        onChanged?.invoke()
    }

    /** type = null nghĩa là đánh dấu tất cả. */
    fun markAllRead(type: NotificationType?) {
        var changed = false
        list.filter { type == null || it.type == type }.forEach {
            if (!it.isRead) {
                it.isRead = true
                changed = true
            }
        }
        if (changed) onChanged?.invoke()
    }

    fun delete(id: Long) {
        if (list.removeAll { it.id == id }) onChanged?.invoke()
    }

    /** type = null nghĩa là xóa tất cả. */
    fun deleteAll(type: NotificationType?) {
        val removed = list.removeAll { type == null || it.type == type }
        if (removed) onChanged?.invoke()
    }

    // ─── Thông báo đơn hàng (do OrderManager gọi) ──────
    fun onOrderPlaced(order: Order) {
        add(
            NotificationType.ORDER, "✅", "Đặt hàng thành công",
            "Đơn ${order.code} tại ${order.restaurant} đã được gửi đi. Vui lòng chờ nhà hàng xác nhận nhé!",
            orderId = order.id
        )
    }

    /** Gọi sau khi đơn vừa chuyển sang bước mới (xác nhận / chuẩn bị / giao / đã giao). */
    fun onOrderProgress(order: Order) {
        val code = order.code
        val shop = order.restaurant
        when (order.progressStep()) {
            1 -> add(
                NotificationType.ORDER, "🧾", "Nhà hàng đã xác nhận",
                "$shop đã nhận đơn $code của bạn.", orderId = order.id
            )
            2 -> add(
                NotificationType.ORDER, "🍳", "Đang chuẩn bị món",
                "$shop đang chuẩn bị ${order.totalQuantity} món cho đơn $code.", orderId = order.id
            )
            3 -> add(
                NotificationType.ORDER, "🛵", "Đơn hàng đang được giao",
                "Tài xế đang trên đường giao đơn $code đến ${order.address}. Bạn chú ý điện thoại nhé!",
                orderId = order.id
            )
            4 -> add(
                NotificationType.ORDER, "🎉", "Giao hàng thành công",
                "Đơn $code đã được giao. Chúc bạn ngon miệng!", orderId = order.id
            )
        }
    }

    fun onOrderCancelled(order: Order) {
        add(
            NotificationType.ORDER, "❌", "Đã hủy đơn hàng",
            "Đơn ${order.code} tại ${order.restaurant} đã được hủy theo yêu cầu của bạn.",
            orderId = order.id
        )
    }

    // ─── Nội bộ ────────────────────────────────────────
    private fun nextId(): Long {
        lastId++
        return lastId
    }

    /**
     * Dữ liệu mẫu. Mã đơn DH001..DH005 khớp với 5 đơn mẫu trong OrderManager
     * nên bấm vào thông báo sẽ mở đúng chi tiết đơn.
     */
    private fun seedSampleNotifications() {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour

        fun seed(
            type: NotificationType, emoji: String, title: String, message: String,
            time: Long, read: Boolean, orderId: String? = null, promoCode: String? = null
        ) {
            list.add(
                AppNotification(
                    id = nextId(), type = type, emoji = emoji, title = title,
                    message = message, time = time, isRead = read,
                    orderId = orderId, promoCode = promoCode
                )
            )
        }

        // Đơn hàng
        seed(
            NotificationType.ORDER, "🧾", "Nhà hàng đã xác nhận",
            "Quán Phở Tuyệt đã nhận đơn #DH001 của bạn.",
            now - 20 * minute, false, orderId = "DH001"
        )
        seed(
            NotificationType.ORDER, "🛵", "Đơn hàng đang được giao",
            "Tài xế đang trên đường giao đơn #DH002 đến 12 Nguyễn Huệ, Quận 1, TP.HCM. Bạn chú ý điện thoại nhé!",
            now - 15 * minute, false, orderId = "DH002"
        )
        seed(
            NotificationType.ORDER, "✅", "Đặt hàng thành công",
            "Đơn #DH001 tại Quán Phở Tuyệt đã được gửi đi. Vui lòng chờ nhà hàng xác nhận nhé!",
            now - 30 * minute, true, orderId = "DH001"
        )
        seed(
            NotificationType.ORDER, "🎉", "Giao hàng thành công",
            "Đơn #DH003 đã được giao. Chúc bạn ngon miệng!",
            now - day + 40 * minute, true, orderId = "DH003"
        )
        seed(
            NotificationType.ORDER, "🎉", "Giao hàng thành công",
            "Đơn #DH004 đã được giao. Chúc bạn ngon miệng!",
            now - 3 * day + 35 * minute, true, orderId = "DH004"
        )
        seed(
            NotificationType.ORDER, "❌", "Đã hủy đơn hàng",
            "Đơn #DH005 tại Mì Quảng Huế đã được hủy theo yêu cầu của bạn.",
            now - 5 * day + 10 * minute, true, orderId = "DH005"
        )

        // Khuyến mãi
        seed(
            NotificationType.PROMO, "🎁", "Giảm 30% cho đơn đầu tiên",
            "Nhập mã FIRST30 khi thanh toán để được giảm 30% cho đơn hàng đầu tiên của bạn.",
            now - 2 * hour, false, promoCode = "FIRST30"
        )
        seed(
            NotificationType.PROMO, "🚚", "Freeship đơn từ 50.000đ",
            "Miễn phí giao hàng cho mọi đơn từ 50.000đ. Đặt món ngay để không bỏ lỡ!",
            now - 6 * hour, false
        )
        seed(
            NotificationType.PROMO, "🍗", "Ưu đãi 20% combo KFC",
            "KFC Việt Nam giảm 20% cho các combo gà rán. Nhập mã COMBO20 để nhận ưu đãi.",
            now - 2 * day, true, promoCode = "COMBO20"
        )

        // Hệ thống
        seed(
            NotificationType.SYSTEM, "👤", "Hoàn thiện thông tin cá nhân",
            "Thêm giới tính, ngày sinh và nghề nghiệp ở mục Tôi để FoodHub gợi ý món phù hợp hơn.",
            now - 4 * day, true
        )
        seed(
            NotificationType.SYSTEM, "👋", "Chào mừng đến với FoodHub",
            "Cảm ơn bạn đã đăng ký tài khoản. Khám phá các món ngon quanh bạn ngay nhé!",
            now - 5 * day, true
        )
    }
}
