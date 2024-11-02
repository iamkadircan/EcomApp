package com.example.feature.order


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.core.common.util.formatPrice
import com.example.core.common.util.toFormattedTimeString
import org.jetbrains.annotations.Async
import java.util.Locale

@Composable
fun OrderScreen(
    viewModel: OrderViewModel = hiltViewModel(),
    onNavigateToDetails: (productId: Int) -> Unit
) {

    val orders by viewModel.orders.collectAsStateWithLifecycle()

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.3f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier.weight(1.5f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text("Date: " + order.orderTime.toFormattedTimeString())
                            val ordersTotalPrice = order.orderItems.sumOf { it.itemsTotalPrice }
                            val totalPriceFormated =
                                String.format(Locale.getDefault(), "%.2f", ordersTotalPrice) + " $"
                            Text("Total Price : $totalPriceFormated")

                            Text("Status: ${order.orderStatus}")
                        }

                        LazyRow(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth()
                                .padding(start = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(order.orderItems) { orderItem ->
                                Card(
                                    modifier = Modifier
                                        .width(screenWidthDp * 0.7f)
                                        .fillMaxHeight()
                                        .padding(horizontal = 1.dp),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    shape = RoundedCornerShape(12.dp)

                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(end = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        AsyncImage(model = orderItem.thumbnail,
                                            contentDescription = "",
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(1.dp)
                                                .clickable {
                                                    onNavigateToDetails(orderItem.productId)
                                                })

                                        Column(
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                orderItem.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("quantity: ${orderItem.quantity}")
                                            Text("cost:${formatPrice(orderItem.itemsTotalPrice)} $")
                                        }
                                    }
                                }
                            }
                        }

                    }


                }
            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenTopBar(onNavigateUp: () -> Unit) {
    CenterAlignedTopAppBar(title = { Text("Orders") }, navigationIcon = {
        IconButton(
            onClick = onNavigateUp
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, "")
        }
    })

}