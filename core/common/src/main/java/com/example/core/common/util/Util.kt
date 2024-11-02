package com.example.core.common.util


import android.icu.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedTimeString() :String{
    val date = Date(this)
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatPrice(price:Double):String{
    return String.format(Locale.getDefault(), "%.2f", price)

}