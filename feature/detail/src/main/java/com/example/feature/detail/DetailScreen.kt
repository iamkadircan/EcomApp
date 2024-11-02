package com.example.feature.detail


import android.graphics.Paint.Align
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.feature.component.StarRatingBar
import com.example.feature.component.ui.theme.CartColor
import kotlinx.coroutines.launch

@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onShowSnackbar: (msg: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            onShowSnackbar(it)
            viewModel.resetSnackbarMessage()
        }
    }
    val scope = rememberCoroutineScope()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            uiState.product?.let { product ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f),
                    contentAlignment = Alignment.Center
                ) {
                    val thumbNail = mutableListOf(product.thumbnail)
                    val images = product.images
                    val allImages = thumbNail + images
                    val pagerState = rememberPagerState(pageCount = { allImages.size })

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = allImages[index],
                                contentDescription = "",
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterStart),
                                enabled = index != 0
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "",
                                )
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterEnd),
                                enabled = index != allImages.size - 1
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    "",

                                    )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.onEvent(DetailScreenEvent.ToggleFavorite(productId = product.id))
                        }, modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp)
                    ) {
                        val tint = if (product.isFavorite) Color.Red else Color.LightGray
                        val icon =
                            if (product.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                        Icon(icon, tint = tint, contentDescription = "")
                    }


                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    StarRatingBar(rating = product.rating)

                    var iconClicked by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (iconClicked) 1.2f else 1f,
                        animationSpec = tween(200),
                        finishedListener = {
                            if (iconClicked) {
                                iconClicked = false
                            }
                        }, label = ""
                    )

                    IconButton(
                        onClick = {
                            iconClicked = true
                            viewModel.onEvent(DetailScreenEvent.AddToCart(productId = product.id))
                        },
                        modifier = Modifier
                            .width(screenWidth * 0.3f)
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = CartColor
                        )
                    ) {
                        Text("Add To Cart")
                    }
                }

                Text(product.price.toString() + " $", fontWeight = FontWeight.Bold)
                Text(product.title, fontWeight = FontWeight.Bold)
                Text(product.description)
                Text(product.category)


            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenTopBar(onNavigateUp: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text("Details") }, navigationIcon = {
            IconButton(
                onClick = onNavigateUp
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "")
            }
        }
    )

}