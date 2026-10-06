package com.example.foodhub.data

import com.example.foodhub.data.model.CartItem
import com.example.foodhub.data.model.Food
import com.example.foodhub.utils.FormatUtils

/**
 * Giỏ hàng lưu trong bộ nhớ (dùng chung cho toàn app).
 * Nhờ vậy khi chuyển tab / mở Activity khác thì số lượng trong giỏ không bị mất.
 */
object CartManager {

    private val items = mutableListOf<CartItem>()

    fun getItems(): List<CartItem> = items

    fun totalQuantity(): Int = items.sumOf { it.quantity }

    fun totalPrice(): Int = items.sumOf { it.total }

    fun isEmpty(): Boolean = items.isEmpty()

    fun addFood(food: Food) {
        val existing = items.find { it.foodId == food.id }
        if (existing != null) {
            existing.quantity++
        } else {
            items.add(
                CartItem(
                    foodId = food.id,
                    name = food.name,
                    restaurant = food.restaurant,
                    emoji = food.emoji,
                    price = FormatUtils.parsePrice(food.price),
                    quantity = 1
                )
            )
        }
    }

    /** Dùng cho "Đặt lại": thêm toàn bộ món của đơn cũ vào giỏ. */
    fun addItems(list: List<CartItem>) {
        list.forEach { old ->
            val existing = items.find { it.foodId == old.foodId }
            if (existing != null) {
                existing.quantity += old.quantity
            } else {
                items.add(old.copy())
            }
        }
    }

    fun increase(item: CartItem) {
        item.quantity++
    }

    fun decrease(item: CartItem) {
        if (item.quantity > 1) item.quantity--
    }

    fun remove(item: CartItem) {
        items.remove(item)
    }

    fun clear() {
        items.clear()
    }
}
