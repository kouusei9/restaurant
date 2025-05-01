package com.kouusei.restaurant.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kouusei.restaurant.R
import com.kouusei.restaurant.data.api.entities.Address

@Composable
fun InlineAddressSelector(
    modifier: Modifier = Modifier,
    largeAddress: List<Address>,
    selectedLarge: Address?,
    onLargeSelect: (Address) -> Unit,
    middleAddress: List<Address>,
    selectedMiddle: Address?,
    onMiddleSelect: (Address) -> Unit,
    smallAddress: List<Address>,
    selectedSmall: Address?,
    onSmallSelect: (Address) -> Unit,
    onConfirm: (String) -> Unit = {},
    onReset: () -> Unit = {},
    onCanceled: () -> Unit = {},
    height: Dp,
    showList: List<Boolean>,
    showToggle: (Int) -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "エリア選択",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier
                    .wrapContentHeight()
            )

            Box(
                modifier = Modifier.clickable {
                    onCanceled()
                },
            ) {
                Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - 140.dp)
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            // 大地址选择
            AddressColumn(
                title = "地域",
                options = largeAddress,
                selected = selectedLarge,
                onSelect = {
                    onLargeSelect(it)
                },
                isShow = showList[0],
                onShowToggle = {
                    showToggle(0)
                }
            )

            // 中地址选择
            AnimatedVisibility(
                selectedLarge != null,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300),
                    expandFrom = Alignment.Top
                )
            ) {
                AddressColumn(
                    title = "エリア",
                    options = middleAddress,
                    selected = selectedMiddle,
                    onSelect = {
                        onMiddleSelect(it)
                    },
                    isShow = showList[1],
                    onShowToggle = {
                        showToggle(1)
                    }
                )
            }

            // 小地址选择
            AnimatedVisibility(
                selectedMiddle != null,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 300),
                    expandFrom = Alignment.Top
                )
            ) {
                AddressColumn(
//                    modifier = Modifier.weight(weightSmall),
                    title = "場所",
                    options = smallAddress,
                    selected = selectedSmall,
                    onSelect = {
                        onSmallSelect(it)
                    },
                    isShow = showList[2],
                    onShowToggle = {
                        showToggle(2)
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier
                    .clickable {
                        onReset()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Clear, contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(text = stringResource(R.string.filter_clear))
            }

            Button(
                onClick = {
                    onConfirm(
                        selectedSmall?.code ?: selectedMiddle?.code ?: selectedLarge?.code
                        ?: ""
                    )
                }
            ) {
                Text(text = stringResource(R.string.filter_confirm))
            }
        }
    }

}

@Composable
fun AddressColumn(
    modifier: Modifier = Modifier,
    title: String,
    options: List<Address>,
    selected: Address?,
    onSelect: (Address) -> Unit,
    isShow: Boolean = true,
    onShowToggle: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                onShowToggle()
            }) {
            Text(
                text = "$title : ${selected?.name?:""}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        if (isShow) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                options.forEach { address ->
                    val isSelected = selected == address
                    Spacer(
                        modifier = Modifier
                            .height(1.dp)
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable {
                                onSelect(address)
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = address.name, fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun AddressSelectorViewPreview() {
    InlineAddressSelector(
        largeAddress = listOf(
            Address("北海道", "hokkaido"),
            Address("青森県", "aomori"),
            Address("岩手県", "iwate"),
            Address("宮城県", "miyagi"),
        ),
        selectedLarge = Address("宮城県", "miyagi"),
        onLargeSelect = { /* TODO */ },
        middleAddress = listOf(
            Address("札幌市", "sapporo"),
            Address("函館市", "hakodate"),
            Address("旭川市", "asahikawa"),
            Address("小樽市", "otaru"),
        ),
        selectedMiddle = null,
        onMiddleSelect = { /* TODO */ },
        smallAddress = listOf(
            Address("中央区", "chuo"),
            Address("北区", "kita"),
            Address("東区", "higashi"),
            Address("白石区", "shiroishi"),
        ),
        selectedSmall = null,
        onSmallSelect = { /* TODO */ },
        height = 800.dp,
        onConfirm = { /* TODO */ },
        onReset = { /* TODO */ },
        onCanceled = { /* TODO */ },
        showList = listOf(
            true,
            false,
            false
        ),
        showToggle = { index ->
            // Handle the toggle action for the address list
            // You can update the showList state here based on the index
        },
    )
}