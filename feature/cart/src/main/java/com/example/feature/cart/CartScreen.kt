package com.example.feature.cart


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.feature.component.ui.theme.CartColor
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onShowSnackbar: (msg: String) -> Unit,
    onNavToDetails: (productId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            onShowSnackbar(msg)
            viewModel.resetErrorMsg()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        if (uiState.carts.isEmpty()){
            Text("Cart is empty.")
        }

        Column {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.9f), verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(uiState.carts) { cart ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 12.dp
                        )
                    ) {
                        //chekcbox, image, title desc...

                        Row(
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxWidth()
                        ) {
                            //checkbox
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(), contentAlignment = Alignment.Center
                            ) {
                                Checkbox(
                                    checked = cart.productId in uiState.checkedItems.map { it.productId },
                                    onCheckedChange = {
                                        viewModel.onEvent(
                                            CartScreenEvent.ToggleCartCheck(cart.productId)
                                        )
                                    }
                                )
                            }

                            //image
                            Box(
                                modifier = Modifier
                                    .weight(2f)
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(2.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = cart.thumbnail,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { onNavToDetails(cart.productId) }
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(5f)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                //title
                                Text(cart.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                //desc
                                Text(
                                    cart.description,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }

                        }


                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(bottom = 5.dp, start = 5.dp, end = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            ) {
                                IconButton(onClick = {
                                    viewModel.onEvent(
                                        CartScreenEvent.RemoveCart(
                                            productId = cart.productId
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "",
                                        tint = Color.White
                                    )
                                }
                            }

                            //quantity bar
                            Row(
                                modifier = Modifier
                                    .border(
                                        border = BorderStroke(1.dp, color = CartColor),
                                        shape = CircleShape
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(
                                            CartScreenEvent.ChangeCartQuantity(
                                                productId = cart.productId,
                                                newQuantity = cart.quantity - 1
                                            )
                                        )

                                    }, enabled = cart.quantity > 1
                                ) {
                                    Icon(painter = painterResource(R.drawable.icon_decrease), "")
                                }
                                Text(cart.quantity.toString())
                                IconButton(onClick = {
                                    viewModel.onEvent(
                                        CartScreenEvent.ChangeCartQuantity(
                                            productId = cart.productId,
                                            newQuantity = cart.quantity + 1
                                        )
                                    )
                                }) {
                                    Icon(Icons.Default.Add, "")
                                }

                            }

                            //price
                            val price = cart.price.toString() + " $"
                            Text(price)
                        }

                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val totalPrice = uiState.totalPrice
                val formatedTotalPrice =
                    String.format(Locale.getDefault(), "%.2f", totalPrice) + " $"
                Text("total price : $formatedTotalPrice")


                Button(
                    onClick = {
                        viewModel.onEvent(CartScreenEvent.CreateOrder)
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .padding(top = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CartColor),
                    enabled = uiState.totalPrice > 0
                ) {
                    Text("checkout")
                }

            }
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreenTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Carts", fontWeight = FontWeight.Bold) }

    )

}