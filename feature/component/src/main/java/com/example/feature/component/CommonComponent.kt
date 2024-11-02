package com.example.feature.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.example.feature.component.R
import com.example.feature.component.ui.theme.StarColor

@Composable
fun StarRatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,

    ) {
    val fullStarIcon = Icons.Outlined.Star
    val halfStarIcon = painterResource(R.drawable.half_star)
    val starColor = StarColor
    val emptyStarColor = Color.LightGray

    val fullStarCount = rating.toInt()
    val hasHalfStar = (rating - fullStarCount) >= 0.5

    Row(
        horizontalArrangement = Arrangement.Start
    ) {
        Row {
            repeat(5) { index ->
                val currIndex = index + 1
                when {
                    currIndex <= fullStarCount -> {
                        Icon(imageVector = fullStarIcon, contentDescription = "", tint = starColor)
                    }

                    currIndex == fullStarCount + 1 && hasHalfStar -> {
                        Icon(halfStarIcon, contentDescription = "", tint = starColor)
                    }

                    else -> {
                        Icon(
                            imageVector = fullStarIcon,
                            contentDescription = "",
                            tint = emptyStarColor
                        )
                    }

                }
            }
        }
        Text(text = rating.toString(), textAlign = TextAlign.Start)
    }


}