package com.example.foodhub.data

import android.os.Handler
import android.os.Looper
import com.example.foodhub.data.model.CartItem
import com.example.foodhub.data.model.Order
import com.example.foodhub.data.model.OrderStatus

/**
 * Quản lý danh sách đơn hàng (lưu trong bộ nhớ, có sẵn vài đơn mẫu).
 */
object OrderManager {

    // Đơn mới đặt sẽ tự chuyển trạng thái sau mỗi STEP_DELAY_MS để bạn xem thử luồng:
    // Đã đặt hàng -> Nhà hàng xác nhận -> Đang chuẩn bị -> Đang giao -> Đã giao
    // Muốn tắt thì đổi AUTO_ADVANCE = false
    private const val AUTO_ADVANCE = true
    private const val STEP_DELAY_MS = 15_000L

    private const val FREE_SHIP_FROM = 50_000
    private const val SHIP_FEE = 15_000

    private val orders = mutableListOf<Order>()
    private var counter = 0
    private val handler = Handler(Looper.getMainLooper())

    /** Activity đang hiển thị đơn hàng gán callback này để được refresh khi dữ liệu đổi. */
    var onChanged: (() -> Unit)? = null

    init {
        seedSampleOrders()
    }

    // ─── Truy vấn ──────────────────────────────────────
    fun getOrders(status: OrderStatus?): List<Order> {
        val list = if (status == null) orders else orders.filter { it.status == status }
        return list.sortedByDescending { it.createdAt }
    }

    fun findById(id: String): Order? = orders.find { it.id == id }

    fun shippingFeeFor(subtotal: Int): Int =
        if (subtotal >= FREE_SHIP_FROM) 0 else SHIP_FEE

    // ─── Tạo / hủy đơn ─────────────────────────────────
    /** Mỗi nhà hàng trong giỏ sẽ tạo thành 1 đơn hàng riêng. */
    fun createOrders(
        cartItems: List<CartItem>,
        receiverName: String,
        phone: String,
        address: String,
        paymentMethod: String
    ): List<Order> {
        val created = mutableListOf<Order>()
        val now = System.currentTimeMillis()

        cartItems.groupBy { it.restaurant }.forEach { (restaurant, list) ->
            val copies = list.map { it.copy() }
            val subtotal = copies.sumOf { it.total }
            val order = Order(
                id = nextId(),
                restaurant = restaurant,
                createdAt = now,
                items = copies,
                shippingFee = shippingFeeFor(subtotal),
                receiverName = receiverName,
                phone = phone,
                address = address,
                paymentMethod = paymentMethod,
                status = OrderStatus.PROCESSING,
                processStep = 0
            )
            orders.add(order)
            created.add(order)
            NotificationStore.onOrderPlaced(order)
            if (AUTO_ADVANCE) scheduleAdvance(order)
        }
        onChanged?.invoke()
        return created
    }

    fun cancel(orderId: String): Boolean {
        val order = findById(orderId) ?: return false
        if (!order.canCancel()) return false
        order.status = OrderStatus.CANCELLED
        NotificationStore.onOrderCancelled(order)
        onChanged?.invoke()
        return true
    }

    // ─── Nội bộ ────────────────────────────────────────
    private fun nextId(): String {
        counter++
        return "DH" + counter.toString().padStart(3, '0')
    }

    private fun scheduleAdvance(order: Order) {
        handler.postDelayed({
            when (order.status) {
                OrderStatus.PROCESSING -> {
                    if (order.processStep < 2) order.processStep++
                    else order.status = OrderStatus.SHIPPING
                }
                OrderStatus.SHIPPING -> order.status = OrderStatus.DELIVERED
                else -> return@postDelayed   // đã giao hoặc đã hủy thì dừng
            }
            // Đơn vừa sang bước mới -> báo cho người dùng
            NotificationStore.onOrderProgress(order)
            onChanged?.invoke()
            if (order.status != OrderStatus.DELIVERED) scheduleAdvance(order)
        }, STEP_DELAY_MS)
    }

    private fun seedSampleOrders() {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        val hour = 60 * minute
        val day = 24 * hour

        val name = "Trường Thanh Toàn"
        val phone = "0901 234 567"
        val address = "12 Nguyễn Huệ, Quận 1, TP.HCM"

        fun add(
            restaurant: String, time: Long, items: List<CartItem>,
            payment: String, status: OrderStatus, step: Int = 0
        ) {
            val subtotal = items.sumOf { it.total }
            orders.add(
                Order(
                    id = nextId(),
                    restaurant = restaurant,
                    createdAt = time,
                    items = items,
                    shippingFee = shippingFeeFor(subtotal),
                    receiverName = name,
                    phone = phone,
                    address = address,
                    paymentMethod = payment,
                    status = status,
                    processStep = step
                )
            )
        }

        add(
            "Quán Phở Tuyệt", now - 30 * minute,
            listOf(CartItem(1, "Phở bò đặc biệt", "Quán Phở Tuyệt", "🍜", 45_000, 2)),
            "Thanh toán khi nhận hàng (COD)", OrderStatus.PROCESSING, 1
        )
        add(
            "KFC Việt Nam", now - hour,
            listOf(CartItem(2, "Gà rán giòn cay", "KFC Việt Nam", "🍗", 55_000, 2)),
            "Ví MoMo", OrderStatus.SHIPPING
        )
        add(
            "Bánh mì Sài Gòn", now - day,
            listOf(
                CartItem(3, "Bánh mì thập cẩm", "Bánh mì Sài Gòn", "🥖", 25_000, 2),
                CartItem(6, "Nước chanh đá", "Bánh mì Sài Gòn", "🧃", 10_000, 1)
            ),
            "Thanh toán khi nhận hàng (COD)", OrderStatus.DELIVERED
        )
        add(
            "Cơm tấm Quỳnh", now - 3 * day,
            listOf(CartItem(4, "Cơm tấm sườn nướng", "Cơm tấm Quỳnh", "🍚", 35_000, 1)),
            "Chuyển khoản ngân hàng", OrderStatus.DELIVERED
        )
        add(
            "Mì Quảng Huế", now - 5 * day,
            listOf(CartItem(5, "Mì Quảng tôm thịt", "Mì Quảng Huế", "🍝", 40_000, 1)),
            "Ví MoMo", OrderStatus.CANCELLED
        )
    }
}
