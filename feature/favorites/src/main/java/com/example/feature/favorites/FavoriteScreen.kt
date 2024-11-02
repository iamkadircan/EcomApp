package com.example.feature.favorites


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.domain.model.DomainProduct
import com.example.feature.component.StarRatingBar
import com.example.feature.component.ui.theme.CartColor

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = hiltViewModel(),
    onShowSnackbar: (msg: String) -> Unit, onNavToDetailScreen: (productId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            onShowSnackbar(it)
            viewModel.resetErrorMsg()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (uiState.favoriteProducts.isEmpty()){
            Text("You don't have favorites yet.")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            items(uiState.favoriteProducts) { favItem ->
                FavItemCard(
                    viewModel = viewModel,
                    favItem = favItem,
                    onNavToDetailScreen = onNavToDetailScreen
                )
            }
        }


    }
}


@Composable
fun FavItemCard(
    viewModel: FavoriteViewModel,
    favItem: DomainProduct,
    onNavToDetailScreen: (productId: Int) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Görsel ve Favori İkonu
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f) // Görselin kare olmasını sağlar
                    .border(width = 1.dp, color = Color.LightGray)
                    .padding(4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                AsyncImage(
                    model = favItem.thumbnail,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onNavToDetailScreen(favItem.id) }
                )
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite Icon",
                    tint = Color.Red,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            viewModel.onEvent(FavoriteScreenEvent.ToggleFav(productId = favItem.id))
                        }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Sağ Taraf: Başlık, Açıklama, Yıldız Puanı, Fiyat ve Buton
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = favItem.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = favItem.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRatingBar(
                        rating = favItem.rating,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${favItem.price} $",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Green
                    )
                }

                // "Add To Cart" Butonu
                var isClicked by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isClicked) 1.2f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    finishedListener = {
                        if (isClicked) {
                            isClicked = false
                        }
                    }
                )

                Button(
                    onClick = {
                        viewModel.onEvent(FavoriteScreenEvent.AddToCart(favItem.id))
                        isClicked = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CartColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                ) {
                    Text("Add To Cart")
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreenTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Favorites", fontWeight = FontWeight.Bold) }
    )

}
