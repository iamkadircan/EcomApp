package com.example.feature.products


import android.graphics.Paint.Align
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.core.domain.model.DomainProduct
import com.example.feature.component.StarRatingBar
import com.example.feature.component.ui.theme.CartColor


@Composable
fun ProductScreen(
    viewModel: ProductViewModel = hiltViewModel(), onShowSnackbar: (msg: String) -> Unit,
    onNavToDetails: (productId: Int) -> Unit
) {


    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val cardHeight = (LocalConfiguration.current.screenHeightDp * 0.4).dp
    val gridState = rememberLazyGridState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItemCount = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= (totalItemCount - 5)
        }
    }



    LaunchedEffect(uiState.snackbarMessage, shouldLoadMore.value) {
        uiState.snackbarMessage?.let { msg ->
            onShowSnackbar(msg)
            viewModel.resetSnackbarMessage()
        }

        if (shouldLoadMore.value) {
            viewModel.loadMoreProducts()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        focusManager.clearFocus()
                        tryAwaitRelease()
                    }

                )
            },
        contentAlignment = Alignment.Center
    ) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.products) { product ->
                ProductScreenCard(
                    product = product,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    viewModel = viewModel,

                    onNavToDetails = onNavToDetails
                )
            }

            item(span = { GridItemSpan(2) }) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }
            }
        }


    }


}

@Composable
fun ProductScreenCard(
    modifier: Modifier = Modifier,
    product: DomainProduct,
    viewModel: ProductViewModel,
    onNavToDetails: (productId: Int) -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = product.thumbnail,
                    contentDescription = "", modifier = Modifier
                        .fillMaxSize()
                        .clickable { onNavToDetails(product.id) }
                )
                val icon =
                    if (product.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                val tint = if (product.isFavorite) Color.Red else Color.LightGray

                Icon(
                    contentDescription = "", imageVector = icon,
                    tint = tint,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 10.dp, top = 10.dp)
                        .clickable {
                            viewModel.onEvent(ProductScreenEvent.ToggleFav(productId = product.id))
                        }
                )

                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }

            Text(
                product.title,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
            )

            Text(
                product.description,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )

            // Star Rating Bar
            StarRatingBar(rating = product.rating, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 4.dp))

            val price = "${product.price} $"
            Text(
                price,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.weight(1f))

            var isClicked by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isClicked) 1.2f else 1f,
                animationSpec = tween(durationMillis = 200),
                finishedListener = {
                    if (isClicked) {
                        isClicked = false
                    }
                }, label = ""
            )

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        isClicked = true
                        viewModel.onEvent(ProductScreenEvent.AddToCart(productId = product.id))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CartColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    Text("Add To Cart")
                }
            }
        }
    }
}



@Composable
fun ProductScreenTopBar(viewModel: ProductViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery = uiState.searchQuery
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onEvent(ProductScreenEvent.UpdateSearchQuery(it)) },
            placeholder = { Text("Products") },
            maxLines = 1,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White),
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 5.dp)
                ) {
                    IconButton(onClick = { viewModel.onEvent(ProductScreenEvent.UpdateSearchQuery("")) }) {
                        Icon(Icons.Outlined.Clear, "")
                    }
                    Icon(Icons.Outlined.Search, "")
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            )
        )
    }
}